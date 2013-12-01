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

package rogue.app.framework.view.urisupport.listener;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.internal.util.ClassUtils;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.urisupport.internal.URIUtil;

import javax.faces.application.ViewHandler;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Context listener that initializes the URIResolver.
 */
public class URIResolverInitializer implements ServletContextListener
{
    /**
     * Context parameter key for identifying the URI prefixes that should be loaded by the
     */
    public static final String URI_PREFIXES = "rogue.app.framework.URI_PREFIXES";

    private static final String CLASS_NAME = URIResolverInitializer.class.getName();

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        final String METHOD_NAME = "contextInitialized";

        ServletContext context = sce.getServletContext();

        // Set the Faces servlet name
        String facesServletName = context.getInitParameter(URIUtil.FACES_SERVLET_NAME);
        if (facesServletName == null || facesServletName.isEmpty())
        {
            URIUtil.LOGGER.logp(Level.CONFIG, CLASS_NAME, METHOD_NAME,
                                "Unable to locate the Faces servlet name. Pretty URI support may not work as expected."
                                        + " Ensure that the context parameter: " + URIUtil.FACES_SERVLET_NAME
                                        + " has been defined in the web.xml and its value should be the name of the assigned to the Faces servlet.");
        }
        else
        {
            URIUtil.setFacesServletName(facesServletName);
        }

        // Initialize the facelet suffix.
        String suffix = context.getInitParameter(ViewHandler.FACELETS_SUFFIX_PARAM_NAME);
        if (suffix == null)
        {
            suffix = ViewHandler.DEFAULT_FACELETS_SUFFIX;
        }
        URIUtil.setFaceletSuffix(suffix);

        // Get the prefixes that should be processed.
        String prefixes = sce.getServletContext().getInitParameter(URI_PREFIXES);
        if (prefixes != null)
        {
            URIResolver resolver = URIResolver.getInstance();
            boolean processTypes = false;
            for (String p : prefixes.split(","))
            {
                p = p.trim();
                if (!p.isEmpty())
                {
                    resolver.addPrefix(p);
                    processTypes = true;
                }
            }

            if (!processTypes)
            {
                return;
            }

            URIUtil.LOGGER.logp(Level.CONFIG, CLASS_NAME, METHOD_NAME,
                                "URIAction support has been enabled on the following prefixes: " + prefixes);

            Iterator<Class<?>> itr = ClassUtils.getTypesAnnotatedWith(ControllerFor.class);
            while (itr.hasNext())
            {
                Class<?> clazz = itr.next();
                ControllerFor ann = clazz.getAnnotation(ControllerFor.class);
                String name = StringUtils.trim(ann.name());
                if (!name.isEmpty())
                {
                    resolver.addControllerName(name);
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        // Nothing much to do at the moment.
    }
}
