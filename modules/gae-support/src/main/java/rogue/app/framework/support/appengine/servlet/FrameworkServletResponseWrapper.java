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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Framework Class: Implements a servlet response wrapper.
 */
public class FrameworkServletResponseWrapper extends HttpServletResponseWrapper
{
    private static final String CLASS_NAME = FrameworkServletResponseWrapper.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public FrameworkServletResponseWrapper(HttpServletResponse response)
    {
        super(response);
    }

    @Override
    public void setStatus(int statusCode)
    {
        setStatus(statusCode, null);
    }

    @Override
    public void setStatus(int statusCode, String message)
    {
        final String METHOD_NAME = "setStatus(int)";

        // JSF Bug
        // c.f. http://java.net/jira/browse/JAVASERVERFACES-2680
        //
        if (400 <= statusCode && statusCode < 600)
        {

            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        "Faces did not send the error code, we're doing it instead: " + statusCode);
            try
            {
                if (message == null)
                {
                    sendError(statusCode);
                }
                else
                {
                    sendError(statusCode, message);
                }
            }
            catch (IOException e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to send the error code!", e);
            }
        }

        if (message == null)
        {
            super.setStatus(statusCode);
        }
        else
        {
            super.setStatus(statusCode, message);
        }
    }
}
