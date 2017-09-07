package com.google.webauthn.gaedemo.endpoints;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import java.util.List;
import javax.inject.Named;


/**
 * An endpoint class for handling FIDO2 requests.
 *
 * Google Cloud Endpoints generate APIs and client libraries from API backend, to simplify client
 * access to data from other applications.
 * https://cloud.google.com/endpoints/docs/frameworks/java/get-started-frameworks-java
 */
@Api(
  name = "fido2RequestHandler",
  version = "v1",
  scopes = {Constants.EMAIL_SCOPE, Constants.OPENID_SCOPE},
  clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID},
  audiences = {Constants.ANDROID_AUDIENCE},
  namespace =
      @ApiNamespace(ownerName = "www.google.com", ownerDomain = "www.google.com")
)
public class Fido2RequestHandler {

  @ApiMethod(name = "getRegistrationRequest")
  public List<String> getRegistrationRequest(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    return null;
  }

  @ApiMethod(name = "processRegistrationResponse")
  public List<String> processRegistrationResponse(
      @Named("responseData") String responseData, User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    return null;
  }

  @ApiMethod(name = "getSignRequest")
  public List<String> getSignRequest(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    return null;
  }

  @ApiMethod(name = "processSignResponse")
  public List<String> processSignResponse(
      @Named("responseData") String responseData, User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }


    return null;
  }

  @ApiMethod(name = "getAllSecurityKeys", path = "getAllSecurityKeys")
  public String[] getAllSecurityKeys(User user) throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    return null;
  }

  @ApiMethod(name = "removeSecurityKey")
  public String[] removeSecurityKey(User user, @Named("publicKey") String publicKey)
      throws OAuthRequestException {
    if (user == null) {
      throw new OAuthRequestException("User is not authenticated");
    }

    return null;
  }

}
