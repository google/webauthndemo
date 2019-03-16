// Copyright 2018 Google Inc.
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

import static org.apache.commons.codec.binary.Hex.decodeHex;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.webauthn.gaedemo.objects.CablePairingData;
import com.google.webauthn.gaedemo.storage.Credential;
import org.apache.commons.codec.DecoderException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet to update a credential.
 *
 * Only supports updating caBLE pairing data for now.
 */
public class UpdateCredential extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserService userService = UserServiceFactory.getUserService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long id = Long.valueOf(req.getParameter("id"));
        String irkHex = req.getParameter("irk");
        String lkHex = req.getParameter("lk");

        // Validate
        if (irkHex == null || lkHex == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing IRK or LK");
            return;
        }
        byte[] irk, lk;
        try {
            irk = decodeHex(irkHex.toCharArray());
            lk = decodeHex(lkHex.toCharArray());
        } catch (DecoderException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Cannot decode IRK/LK (use hex)");
            return;
        }
        if (irk.length != 32 || lk.length != 32) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "IRK and LK must both be 32 bytes long");
            return;
        }

        String currentUser = userService.getCurrentUser().getEmail();
        List<Credential> credentials = Credential.load(currentUser);
        for (Credential credential : credentials) {
            if (id == credential.id) {
                credential.setCablePairingData(new CablePairingData(1, irk, lk));
                credential.save(currentUser);
                resp.setContentType("application/json");
                resp.getWriter().println("{}");
                return;
            }
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
