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

package rogue.app.framework.i18n.view.el;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.i18n.AppResourceBundle;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.beans.FeatureDescriptor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EL resolver for loading {@link AppResourceBundle application resource bundles}.
 * <p/>
 * E.g.,
 * <pre>
 *     #{appBundle['UIBundle']}
 * </pre>
 */
public class AppResourceBundleResolver extends ELResolver
{
    private static final String CLASS_NAME = AppResourceBundleResolver.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * The name of the implicit variable for loading {@link AppResourceBundle app resource bundles}.
     */
    public static final String APP_RESOURCE_BUNDLE_EL = "appBundle";

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        if (base == null && APP_RESOURCE_BUNDLE_EL.equals(property))
        {
            context.setPropertyResolved(true);
            return this;
        }
        else if (base instanceof AppResourceBundleResolver && property instanceof String)
        {
            String bundleName = (String) property;

            // Look for a resource bundle by the given name.

            final String METHOD_NAME = "getAppBundle";
            String KEY = METHOD_NAME + "_" + bundleName;

            StateHolder stateHolder = ScopedStateHolder.getStateHolder(RequestScoped.class);
            Map<String, Object> bundleMap;
            bundleMap = stateHolder.get(KEY);

            if (bundleMap == null && !StringUtils.isEmpty(bundleName))
            {
                Locale locale = Locale.ENGLISH;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                if (facesContext != null)
                {
                    UIViewRoot viewRoot = facesContext.getViewRoot();
                    if (viewRoot != null)
                    {
                        locale = viewRoot.getLocale();
                    }
                }

                try
                {
                    bundleMap = new AppResourceBundle(bundleName, locale);
                }
                catch (MissingResourceException ex)
                {
                    LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Failed to load resource", ex);
                    bundleMap = Collections.emptyMap();
                }

                stateHolder.put(KEY, bundleMap);
            }
            context.setPropertyResolved(true);
            return bundleMap;

        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        if (APP_RESOURCE_BUNDLE_EL.equals(base))
        {
            context.setPropertyResolved(true);
            return getClass();
        }
        else if (base instanceof AppResourceBundleResolver && property instanceof String)
        {
            context.setPropertyResolved(true);
            return AppResourceBundle.class;
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value)
    {
        // Do nothing. We are providing read only access.
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        return true;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        return null;
    }
}
