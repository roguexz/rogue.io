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

import org.jboss.weld.literal.DefaultLiteral;
import rogue.app.framework.internal.util.CDIUtils;
import rogue.app.framework.internal.view.servlet.WebContext;
import rogue.app.framework.search.SearchIndexer;
import rogue.app.framework.security.UserContext;
import rogue.app.framework.view.faces.bean.AuthenticationManager;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.ResourceHandler;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet filter that takes care of setting up basic features like security, processing search queues, etc. Make sure
 * that this filter is run after the Weld listener.
 */
public class AppConfigFilter implements Filter
{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        WebContext.init((HttpServletRequest) request, (HttpServletResponse) response);

        // Setup the user context
        setupUserContext((HttpServletRequest) request, (HttpServletResponse) response);

        // process the chain
        filterChain.doFilter(request, response);

        // Clear out pending requests in the SearchIndexer queue.
        SearchIndexer.processQueue();

        // clear out the UserContext
        UserContext.clearContext();

        WebContext.clear();

        if (request.getAttribute("destroy.session") != null)
        {
            HttpSession session = ((HttpServletRequest) request).getSession();
            if (session != null)
            {
                session.invalidate();
            }
        }
    }

    @Override
    public void destroy()
    {
        // Do nothing.
    }

    private void setupUserContext(HttpServletRequest request, HttpServletResponse response)
    {
        String uri = request.getRequestURI();
        if (uri != null && !(uri.startsWith(ResourceHandler.RESOURCE_IDENTIFIER) || uri.startsWith("/resources/")))
        {
            AuthenticationManager manager = getAuthenticationManager();
            if (manager != null)
            {
                manager.initUserContext(request.getRemoteUser());
            }
        }
    }

    private AuthenticationManager getAuthenticationManager()
    {
        return CDIUtils.lookupBean(AuthenticationManager.class, SessionScoped.class, DefaultLiteral.INSTANCE);
    }
}
