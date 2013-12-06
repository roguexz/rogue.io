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

package rogue.app.framework.view.urisupport.internal;

import rogue.app.framework.view.urisupport.URIAction;

import java.util.logging.Logger;

/**
 * Utility class for the URI support feature.
 */
public final class URIUtil
{
    /**
     * The context parameter name that identifies the name of the Faces Servlet to which dispatching will occur.
     */
    public static final String FACES_SERVLET_NAME = "rogue.app.framework.view.FACES_SERVLET_NAME";

    /**
     * The JDK logger that is used by all other classes within this module. The logger is scoped to the package of this
     * class.
     */
    public static final Logger LOGGER = Logger.getLogger(URIAction.class.getPackage().getName());

    private static String faceletSuffix;
    private static String facesServletName;

    private URIUtil()
    {
    }

    /**
     * Get the Facelets suffix that is configured for this application.
     *
     * @return the Facelets suffix that is configured for this application.
     */
    public static String getFaceletSuffix()
    {
        return faceletSuffix;
    }

    /**
     * Set the Facelets suffix that is configured for this application.
     *
     * @param faceletSuffix the Facelets suffix that is configured for this application.
     */
    public static void setFaceletSuffix(String faceletSuffix)
    {
        URIUtil.faceletSuffix = faceletSuffix;
    }

    /**
     * Get the name of the Faces servlet that is configured for this application.
     *
     * @return the name of the Faces servlet that is configured for this application.
     */
    public static String getFacesServletName()
    {
        return facesServletName;
    }

    /**
     * Set the name of the Faces servlet that is configured for this application.
     *
     * @param facesServletName the name of the Faces servlet that is configured for this application.
     */
    public static void setFacesServletName(String facesServletName)
    {
        URIUtil.facesServletName = facesServletName;
    }
}
