// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.webauthn.gaedemo.servlets;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet class handling the http traffic for asset links.
 */
public class AssetLinksHttpServlet extends HttpServlet {

  private static final String PATH = "/well-known/assetlinks.json";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Do anything else that needs doing here
    if (request.getRequestURI().toLowerCase().contains(".json")) {
      response.setContentType("application/json");
    }

    // Read and return the resource from the non-hidden folder
    String respString = readResource(PATH);
//    response.getOutputStream().print(respString);
    response.getWriter().print(respString);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  private String readResource(String resName) throws IOException {
    InputStream in = getServletContext().getResourceAsStream(resName);
    return inputStreamToString(in);
  }

  private static String inputStreamToString(InputStream in) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    StringBuilder sb = new StringBuilder();
    String line;

    while ((line = br.readLine()) != null) {
      sb.append(line).append("\n");
    }
    return sb.toString();
  }

}
