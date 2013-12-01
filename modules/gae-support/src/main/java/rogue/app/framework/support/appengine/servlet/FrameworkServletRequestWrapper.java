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

import com.google.appengine.api.utils.SystemProperty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Framework Class: Implements a servlet request wrapper.
 */
public class FrameworkServletRequestWrapper extends HttpServletRequestWrapper
{
    private static final String CLASS_NAME = FrameworkServletRequestWrapper.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public FrameworkServletRequestWrapper(HttpServletRequest request)
    {
        super(request);
    }

    /**
     * Implements a fix for an <a href='http://code.google.com/p/googleappengine/issues/detail?id=8415'>issue</a> with
     * the dev server environment.
     */
    @Override
    public String getHeader(String name)
    {
        LOGGER.entering(CLASS_NAME, "getHeader(String)", name);

        String header = null;
        if (isProductionEnvironment() || !"If-Modified-Since".equals(name))
        {
            header = super.getHeader(name);
        }

        LOGGER.exiting(CLASS_NAME, "getHeader(String)", header);
        return header;
    }

    /**
     * Implements a fix for an <a href='http://code.google.com/p/googleappengine/issues/detail?id=8415'>issue</a> with
     * the dev server environment.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Enumeration getHeaderNames()
    {
        LOGGER.entering(CLASS_NAME, "getHeaderNames()");
        try
        {
            if (isProductionEnvironment())
            {
                return super.getHeaderNames();
            }

            Enumeration headerNames;
            Enumeration<?> enu;
            List<String> names;
            String name;

            names = new ArrayList<>();
            enu = super.getHeaderNames();

            while (enu.hasMoreElements())
            {
                name = enu.nextElement().toString();
                if (!"If-Modified-Since".equals(name))
                {
                    names.add(name);
                }
            }
            headerNames = Collections.enumeration(names);

            return headerNames;
        }
        finally
        {
            LOGGER.exiting(CLASS_NAME, "getHeaderNames()");
        }
    }

    private boolean isProductionEnvironment()
    {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
    }
}
