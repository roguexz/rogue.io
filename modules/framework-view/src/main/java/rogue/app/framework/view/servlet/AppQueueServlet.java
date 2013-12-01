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


import org.apache.commons.lang.StringUtils;
import rogue.app.framework.view.util.AppFunctions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Base implementation for all the queues within the system.
 */
public abstract class AppQueueServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        processRequest(req, resp);
    }

    /**
     * Method invoked on doGet & doPost.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @throws ServletException if an error occurs.
     * @throws IOException      if an error occurs.
     */
    protected abstract void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;

    /**
     * Convenience method to get hold of application properties.
     *
     * @param key                  the property key.
     * @param raiseExceptionIfNull set it to true if an exception should be raised if the value is null.
     * @return the value.
     * @throws ServletException if the value was null and raiseExceptionIfNull parameter was set to true.
     */
    protected String getApplicationProperty(String key, boolean raiseExceptionIfNull) throws ServletException
    {
        String value = AppFunctions.getApplicationProperty(key, null);
        if (StringUtils.isEmpty(value) && raiseExceptionIfNull)
        {
            throw new ServletException(
                    "Unable to process task. Value for " + key + " was null. Please specify a valid value.");
        }
        return value;
    }

}
