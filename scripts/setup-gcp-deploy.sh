#!/usr/bin/env bash
#
# Copyright 2026 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Automates GCP project API enablement, IAM role assignment, and Cloud Build
# GitHub Trigger creation (deploy-to-staging and deploy-to-prod) for deploying
# WebAuthnDemo to Google App Engine without exporting service account keys.
#
# Usage:
#   ./scripts/setup-gcp-deploy.sh [options]
#
# Examples:
#   ./scripts/setup-gcp-deploy.sh --account agektmr@google.com

set -euo pipefail

PROJECT_ID="try-webauthn"
GCLOUD_ACCOUNT=""
REGION="us-central"
SA_NAME="github-actions-deployer"
REPO_OWNER="google"
REPO_NAME="webauthndemo"
SKIP_TRIGGERS=false

usage() {
  cat <<EOF
Usage: $(basename "$0") [options]

Optional:
  --project <PROJECT_ID>          Google Cloud Project ID (default: try-webauthn)
  --account <EMAIL>               gcloud account to use (e.g. agektmr@google.com)
  --region <REGION>               App Engine region (default: us-central)
  --sa-name <NAME>                Deployer service account name (default: github-actions-deployer)
  --repo-owner <OWNER>            GitHub repository owner (default: google)
  --repo-name <NAME>              GitHub repository name (default: webauthndemo)
  --skip-triggers                 Only configure GCP APIs and IAM; do not create Cloud Build triggers
  -h, --help                      Show this help message
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --project)
      PROJECT_ID="$2"
      shift 2
      ;;
    --account)
      GCLOUD_ACCOUNT="$2"
      shift 2
      ;;
    --region)
      REGION="$2"
      shift 2
      ;;
    --sa-name)
      SA_NAME="$2"
      shift 2
      ;;
    --repo-owner)
      REPO_OWNER="$2"
      shift 2
      ;;
    --repo-name)
      REPO_NAME="$2"
      shift 2
      ;;
    --skip-triggers)
      SKIP_TRIGGERS=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

GCLOUD_ARGS=("--project=${PROJECT_ID}")
if [[ -n "$GCLOUD_ACCOUNT" ]]; then
  GCLOUD_ARGS+=("--account=${GCLOUD_ACCOUNT}")
fi

TMP_TOKEN_FILE=""
cleanup() {
  [[ -n "$TMP_TOKEN_FILE" ]] && rm -f "$TMP_TOKEN_FILE"
}
trap cleanup EXIT

echo "==> [1/7] Verifying access to GCP project '${PROJECT_ID}'..."
if ! PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" "${GCLOUD_ARGS[@]}" --format="value(projectNumber)" 2>/dev/null); then
  if command -v stubby >/dev/null 2>&1; then
    echo "    Standard gcloud auth blocked or expired; exchanging LOAS (gcert) credential via stubby..."
    TMP_TOKEN_FILE=$(mktemp /tmp/webauthndemo-gcloud-token.XXXXXX)
    chmod 600 "$TMP_TOKEN_FILE"
    if stubby call blade:sso CorpLogin.Exchange \
      "target { scope: GAIA_USER name: \"${USER}@google.com\" } target_credential { type: OAUTH2_TOKEN oauth2_attributes { scope: \"https://www.googleapis.com/auth/cloud-platform\" } }" \
      | awk -F'"' '/oauth2_token:/ {print $2}' > "$TMP_TOKEN_FILE" && [[ -s "$TMP_TOKEN_FILE" ]]; then
      GCLOUD_ARGS+=("--access-token-file=${TMP_TOKEN_FILE}")
    fi
  fi
fi

if ! PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" "${GCLOUD_ARGS[@]}" --format="value(projectNumber)"); then
  echo "Error: Unable to access project '${PROJECT_ID}'." >&2
  if [[ -n "$GCLOUD_ACCOUNT" ]]; then
    echo "Please authenticate with: gcloud auth login --account=${GCLOUD_ACCOUNT} (or run gcert)" >&2
  else
    echo "Please check your active account with 'gcloud auth list', run 'gcert', or pass --account <EMAIL>." >&2
  fi
  exit 1
fi
echo "    Project Number: ${PROJECT_NUMBER}"

echo "==> [2/7] Enabling required Google Cloud APIs..."
APIS=(
  "appengine.googleapis.com"
  "cloudbuild.googleapis.com"
  "artifactregistry.googleapis.com"
  "firestore.googleapis.com"
  "iam.googleapis.com"
  "iamcredentials.googleapis.com"
  "storage.googleapis.com"
  "storage-component.googleapis.com"
  "logging.googleapis.com"
)
gcloud services enable "${APIS[@]}" "${GCLOUD_ARGS[@]}"

echo "==> [3/7] Checking App Engine application..."
if ! gcloud app describe "${GCLOUD_ARGS[@]}" >/dev/null 2>&1; then
  echo "    Creating App Engine application in region '${REGION}'..."
  gcloud app create --region="$REGION" "${GCLOUD_ARGS[@]}"
else
  echo "    App Engine application already exists."
fi

SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"
SA_RESOURCE="projects/${PROJECT_ID}/serviceAccounts/${SA_EMAIL}"
echo "==> [4/7] Ensuring deployer service account '${SA_EMAIL}' exists..."
if ! gcloud iam service-accounts describe "$SA_EMAIL" "${GCLOUD_ARGS[@]}" >/dev/null 2>&1; then
  gcloud iam service-accounts create "$SA_NAME" \
    --display-name="Cloud Build App Engine Deployer" \
    --description="Service account for deploying WebAuthnDemo via Cloud Build Triggers" \
    "${GCLOUD_ARGS[@]}"
else
  echo "    Service account '${SA_EMAIL}' already exists."
fi

echo "==> [5/7] Granting IAM roles to deployer service account '${SA_EMAIL}'..."
DEPLOYER_ROLES=(
  "roles/appengine.appAdmin"
  "roles/appengine.deployer"
  "roles/cloudbuild.builds.editor"
  "roles/iam.serviceAccountUser"
  "roles/storage.admin"
  "roles/artifactregistry.writer"
  "roles/logging.logWriter"
)

for ROLE in "${DEPLOYER_ROLES[@]}"; do
  echo "    Binding ${ROLE} -> serviceAccount:${SA_EMAIL}"
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SA_EMAIL}" \
    --role="$ROLE" \
    --condition=None \
    "${GCLOUD_ARGS[@]}" >/dev/null
done

echo "==> [6/7] Verifying runtime & Cloud Build service account permissions and staging bucket..."
APPSPOT_SA="${PROJECT_ID}@appspot.gserviceaccount.com"
COMPUTE_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"
RUNTIME_ROLES=(
  "roles/datastore.user"
  "roles/storage.objectAdmin"
  "roles/logging.logWriter"
  "roles/artifactregistry.writer"
)

for RUNTIME_SA in "$APPSPOT_SA" "$COMPUTE_SA"; do
  if gcloud iam service-accounts describe "$RUNTIME_SA" "${GCLOUD_ARGS[@]}" >/dev/null 2>&1; then
    for ROLE in "${RUNTIME_ROLES[@]}"; do
      gcloud projects add-iam-policy-binding "$PROJECT_ID" \
        --member="serviceAccount:${RUNTIME_SA}" \
        --role="$ROLE" \
        --condition=None \
        "${GCLOUD_ARGS[@]}" >/dev/null
    done
  fi
done

STAGING_BUCKET="gs://staging.${PROJECT_ID}.appspot.com"
if ! gcloud storage buckets describe "$STAGING_BUCKET" "${GCLOUD_ARGS[@]}" >/dev/null 2>&1; then
  echo "    Creating staging bucket '${STAGING_BUCKET}'..."
  gcloud storage buckets create "$STAGING_BUCKET" "${GCLOUD_ARGS[@]}" || true
else
  echo "    Staging bucket '${STAGING_BUCKET}' exists."
fi

if [[ "$SKIP_TRIGGERS" == "true" ]]; then
  echo "==> [7/7] Skipping Cloud Build trigger creation (--skip-triggers)."
  echo "Done! GCP project '${PROJECT_ID}' is ready for App Engine deployment."
  exit 0
fi

echo "==> [7/7] Ensuring Cloud Build GitHub triggers ('deploy-to-staging' and 'deploy-to-prod') exist..."
create_or_check_trigger() {
  local trigger_name="$1"
  local branch_pattern="$2"
  local build_config="$3"
  local description="$4"

  if gcloud builds triggers describe "$trigger_name" "${GCLOUD_ARGS[@]}" >/dev/null 2>&1; then
    echo "    Trigger '${trigger_name}' already exists."
    return 0
  fi

  echo "    Creating trigger '${trigger_name}' (${branch_pattern} -> ${build_config})..."
  if ! gcloud builds triggers create github \
    --name="$trigger_name" \
    --description="$description" \
    --repo-owner="$REPO_OWNER" \
    --repo-name="$REPO_NAME" \
    --branch-pattern="$branch_pattern" \
    --build-config="$build_config" \
    --service-account="$SA_RESOURCE" \
    "${GCLOUD_ARGS[@]}"; then
    echo "" >&2
    echo "Error: Failed to create Cloud Build trigger '${trigger_name}'." >&2
    echo "Please connect the GitHub repository '${REPO_OWNER}/${REPO_NAME}' to Cloud Build first at:" >&2
    echo "  https://console.cloud.google.com/cloud-build/triggers;region=global/connect?project=${PROJECT_NUMBER}" >&2
    echo "Then re-run: ./scripts/setup-gcp-deploy.sh" >&2
    exit 1
  fi
}

create_or_check_trigger \
  "deploy-to-staging" \
  "^staging$" \
  "cloudbuild-staging.yaml" \
  "Deploy WebAuthnDemo staging (dev.yaml) on push to staging branch"

create_or_check_trigger \
  "deploy-to-prod" \
  "^prod$" \
  "cloudbuild-prod.yaml" \
  "Deploy WebAuthnDemo production (prod.yaml) on push to prod branch"

echo "Done! Cloud Build triggers 'deploy-to-staging' and 'deploy-to-prod' are configured on '${PROJECT_ID}'."
