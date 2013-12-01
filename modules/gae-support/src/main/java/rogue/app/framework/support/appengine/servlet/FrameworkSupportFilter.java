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

package rogue.app.framework.support.appengine.servlet;

import com.sun.faces.context.FacesFileNotFoundException;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.primefaces.webapp.MultipartRequest;
import org.primefaces.webapp.filter.FileUploadFilter;
import rogue.app.framework.support.fileupload.InMemoryFileItemFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>FrameworkSupportFilter</code> should be the first filter in the sequence of filters. It takes care of a
 * few workarounds when running in the Dev & Production mode.
 */
public class FrameworkSupportFilter extends FileUploadFilter
{
    private static final String CLASS_NAME = FrameworkSupportFilter.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public void init(FilterConfig config) throws ServletException
    {
        super.init(config);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
                         FilterChain chain) throws ServletException, IOException
    {
        final String METHOD_NAME = "doFilter";

        HttpServletRequest request = new FrameworkServletRequestWrapper((HttpServletRequest) req);
        HttpServletResponse response = new FrameworkServletResponseWrapper((HttpServletResponse) resp);

        try
        {
            boolean isMultiPart = ServletFileUpload.isMultipartContent(request);

            if (isMultiPart)
            {
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.fine("Parsing file upload request");
                }

                FileItemFactory fileItemFactory = new InMemoryFileItemFactory();

                ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
                MultipartRequest multipartRequest = new MultipartRequest(request, servletFileUpload);

                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.fine(
                            "File upload request parsed successfully, continuing with filter chain with a wrapped multipart request");
                }

                chain.doFilter(multipartRequest, response);
            }
            else
            {
                chain.doFilter(request, response);
            }
        }
        catch (FacesFileNotFoundException ex)
        {
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        "Failed to locate requested page: " + request.getRequestURI());
            response.sendError(404);
        }
        catch (Exception ex)
        {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Exception occurred", ex);
            // Redirect user to the error page.
            response.sendError(500);
        }
        catch (Error error)
        {
            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Error occurred", error);
            // Redirect user to the error page.
            response.sendError(500);
        }
    }
}
