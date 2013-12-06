/*
 * Copyright 2013, Rogue.IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rogue.app.framework.view.servlet;

import org.brickred.socialauth.util.SocialAuthUtil;
import org.jboss.weld.literal.DefaultLiteral;
import rogue.app.framework.internal.util.CDIUtils;
import rogue.app.framework.view.faces.bean.AuthenticationManager;
import rogue.app.framework.view.util.SiteUtils;

import javax.enterprise.context.SessionScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for handling OAuth callbacks. The application expects this servlet mapped to <code>/OAuthCallback</code>.
 */
public class OAuthCallbackServlet extends HttpServlet
{
    private static final String CLASS_NAME = OAuthCallbackServlet.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        final String METHOD_NAME = "processRequest";
        AuthenticationManager manager =
                CDIUtils.lookupBean(AuthenticationManager.class, SessionScoped.class, DefaultLiteral.INSTANCE);

        if (manager != null)
        {
            manager.processSocialAuthCallback(SocialAuthUtil.getRequestParametersMap(request));
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to acquire authentication manager!");
        }
        response.sendRedirect(SiteUtils.getBaseSiteURL());
    }
}
