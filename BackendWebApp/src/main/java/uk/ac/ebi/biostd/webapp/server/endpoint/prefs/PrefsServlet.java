/**
 * Copyright 2014-2017 Functional Genomics Development Team, European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * @author Mikhail Gostev <gostev@gmail.com>
 **/

package uk.ac.ebi.biostd.webapp.server.endpoint.prefs;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import uk.ac.ebi.biostd.util.FileUtil;
import uk.ac.ebi.biostd.webapp.server.config.BackendConfig;
import uk.ac.ebi.biostd.webapp.server.config.ConfigurationException;
import uk.ac.ebi.biostd.webapp.server.endpoint.ServiceServlet;
import uk.ac.ebi.biostd.webapp.server.security.Session;

@Slf4j
@WebServlet(urlPatterns = "/prefs/*")
public class PrefsServlet extends ServiceServlet {

    private static final long serialVersionUID = 1L;
    private static final String OP_PARAMETER = "op";
    private static final String NAME_PARAMETER_PREFIX = "name";
    private static final String VALUE_PARAMETER_PREFIX = "value";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response, Session sess)
            throws ServletException, IOException {
        String opstr = request.getParameter(OP_PARAMETER);

        if (opstr == null) {
            String pi = request.getPathInfo();

            if (pi != null && pi.length() > 1) {
                opstr = pi.substring(1);
            }
        }

        if (Op.RECAPTCHA_KEY.name().equalsIgnoreCase(opstr)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");

            String keyPub = BackendConfig.getRecaptchaPublicKey();
            String keyPriv = BackendConfig.getRecaptchaPrivateKey();

            if (keyPub == null || keyPub.length() == 0 || keyPriv == null || keyPriv.length() == 0) {
                response.getWriter().print("FAIL recaptcha in not configured");
            } else {
                response.getWriter().print("OK " + keyPub);
            }
            return;
        }

        if (!BackendConfig.isWebConfigEnabled()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().print("FAIL web based configuration is disabled");
            return;
        }

        if (sess == null || sess.isAnonymous()) {
            if (BackendConfig.getServiceManager() != null && BackendConfig.getServiceManager().getUserManager() != null
                    && BackendConfig.getServiceManager().getUserManager().getUsersNumber() != 0) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain");
                response.getWriter().print("FAIL User not logged in");
                return;
            }
        } else if (!sess.getUser().isSuperuser()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain");
            response.getWriter().print("FAIL only superuser can access configuration");
            return;
        }

        Op op = null;

        if (opstr != null) {
            for (Op o : Op.values()) {
                if (opstr.equalsIgnoreCase(o.name())) {
                    op = o;
                    break;
                }
            }
        }

        if (op == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter()
                    .print("FAIL " + OP_PARAMETER + " is not defined or has invalid value ( valid: get, set)");
            return;
        }

        if (op == Op.GET) {
            Map<String, String> map = BackendConfig.getConfigurationManager().getPreferences();

            JSONObject jobj = new JSONObject();

            try {
                for (Map.Entry<String, String> me : map.entrySet()) {
                    jobj.put(me.getKey(), me.getValue());
                }
            } catch (JSONException e) {
            }

            response.setContentType("application/json");
            response.getWriter().append(jobj.toString());
            return;
        }

        if (op == Op.SET) {
            if (request.getContentType().startsWith("application/json")) {
                request.getReader();

                String body = FileUtil.readStream(request.getReader());

                JSONObject jobj = null;

                try {
                    jobj = new JSONObject(body);

                    Map<String, String> map = new HashMap<>();

                    for (Iterator<String> it = jobj.keys(); it.hasNext(); ) {
                        String key = it.next();

                        map.put(key, jobj.getString(key));
                    }

                    BackendConfig.getConfigurationManager().setPreferences(map);

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/plain");
                    response.getWriter().print("OK configuration changed");
                } catch (JSONException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    response.getWriter().print("FAIL invalid JSON contents");
                    return;
                }
            } else if (request.getContentType().startsWith("application/x-www-form-urlencoded")) {
                Map<String, String> map = new HashMap<>();

                Enumeration<String> names = request.getParameterNames();

                while (names.hasMoreElements()) {
                    String nm = names.nextElement();

                    if (!nm.startsWith(NAME_PARAMETER_PREFIX)) {
                        continue;
                    }

                    String pName = request.getParameter(nm);

                    String sfx = nm.substring(NAME_PARAMETER_PREFIX.length());

                    map.put(pName, request.getParameter(VALUE_PARAMETER_PREFIX + sfx));
                }

                BackendConfig.getConfigurationManager().setPreferences(map);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain");
                response.getWriter().print("OK configuration changed");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().print("FAIL content type is not supported: " + request.getContentType());
                return;
            }
        }

        if (op == Op.RELOADCONFIG) {
            try {
                BackendConfig.getConfigurationManager().loadConfiguration();

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain");
                response.getWriter().print("OK configuration has been reloaded successfuly");
                return;

            } catch (ConfigurationException e) {
                log.error("Configuration reload failed: " + e.getMessage());

                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain");
                response.getWriter().print("FAIL configuration reload failed: " + e.getMessage());
                return;
            }
        }


    }

    public enum Op {
        GET,
        SET,
        RELOADCONFIG,
        RECAPTCHA_KEY
    }
}
