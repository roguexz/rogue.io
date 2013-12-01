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

package rogue.app.framework.view.urisupport.filter;

import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.urisupport.internal.URIUtil;
import rogue.app.framework.view.urisupport.faces.context.URIExternalContext;

import javax.faces.application.ResourceHandler;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * A servlet filter to enable extension-less URIs in the application.
 */
public class URISupportFilter implements Filter
{
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String uri = httpReq.getRequestURI();

        // Trim the context path from the uri
        ServletContext context = filterConfig.getServletContext();
        String path = context.getContextPath();
        if (!Objects.equals(uri, path) && uri.startsWith(path))
        {
            uri = uri.substring(path.length());
        }

        if (!uri.contains(ResourceHandler.RESOURCE_IDENTIFIER) && URIUtil.getFaceletSuffix() != null && !uri
                .endsWith(URIUtil.getFaceletSuffix()))
        {
            // Check with URIResolver if this is a potential action or not.
            URIAction uriAction = URIResolver.getInstance().resolveURI(uri);
            if (uriAction != null)
            {
                // Set the serlvetPath & pathInfo overrides
                // request.setAttribute(URIExternalContext.OVERRIDE_SERVLET_PATH, uri);
                request.setAttribute(URIExternalContext.OVERRIDE_PATH_INFO, uri);

                // Dispatch to Faces servlet
                if (URIUtil.getFacesServletName() != null)
                {
                    RequestDispatcher rd = context.getNamedDispatcher(URIUtil.getFacesServletName());
                    if (rd != null)
                    {
                        rd.forward(request, response);
                        return;
                    }
                }
            }
            else
            {
                String forwardedURI = null;
                // Check if adding the default suffix will do the job.

                URL url = context.getResource(uri + URIUtil.getFaceletSuffix());
                if (url != null)
                {
                    forwardedURI = uri + URIUtil.getFaceletSuffix();
                }

                if (forwardedURI == null)
                {
                    StringBuilder builder = new StringBuilder(uri);
                    if (!uri.endsWith("/"))
                    {
                        builder.append("/");
                    }
                    builder.append("index.xhtml");

                    url = context.getResource(builder.toString());
                    if (url != null)
                    {
                        forwardedURI = builder.toString();
                    }
                }

                if (forwardedURI != null)
                {
                    // Dispatch
                    RequestDispatcher rd = context.getRequestDispatcher(forwardedURI);
                    if (rd != null)
                    {
                        rd.forward(request, response);
                        return;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
        // Do nothing.
    }
}
