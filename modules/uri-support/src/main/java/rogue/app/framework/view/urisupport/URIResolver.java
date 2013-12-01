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

package rogue.app.framework.view.urisupport;

import org.jboss.weld.literal.DefaultLiteral;
import rogue.app.framework.internal.util.CDIUtils;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The URI resolver is specifically designed to handle URIs that adhere to a fixed pattern. This represents the core
 * idea of using the same URI pattern across different web technologies. The result of the resolution identifies a
 * potential {@link URIAction} that needs to be further processed by the appropriate web technology.
 */
@ApplicationScoped
public class URIResolver
{
    private static final String CLASS_NAME = URIResolver.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Reconsider if you really want static cache or not.
    private Map<String, URIAction> cacheMap = new HashMap<>(20);

    private static Set<String> controllerNames = new HashSet<>();
    private static Set<String> prefixes = new HashSet<>();

    @Inject
    @Any
    private Instance<URIResolutionProvider> providers;

    public URIResolver()
    {
    }

    /**
     * Get the instance of URIResolver that is configured for the application.
     *
     * @return the instance of URIResolver that is configured for the application.
     */
    public static URIResolver getInstance()
    {
        StateHolder holder = ScopedStateHolder.getStateHolder(ApplicationScoped.class);
        URIResolver resolver = holder.get(CLASS_NAME);
        if (resolver == null)
        {
            resolver = CDIUtils.lookupBean(URIResolver.class, ApplicationScoped.class, DefaultLiteral.INSTANCE);

            if (resolver == null)
            {
                LOGGER.logp(Level.INFO, CLASS_NAME, "getInstance",
                            "Unable to lookup instance of resolver from CDI scopes. Returning a default instance.");
                return new URIResolver();
            }

            holder.put(CLASS_NAME, resolver);
        }

        return resolver;
    }

    /**
     * Add a prefix to the set of prefix names that should be searched.
     *
     * @param prefixName the prefix to add.
     */
    public void addPrefix(String prefixName)
    {
        prefixes.add(prefixName);
    }

    /**
     * Remove a prefix from the set of prefix names that should be searched.
     *
     * @param prefixName the prefix to remove.
     */
    public void removePrefix(String prefixName)
    {
        prefixes.remove(prefixName);
    }

    /**
     * Get an array of prefixes that are recognized by this resolver.
     *
     * @return an array of prefixes that are recognized by this resolver.
     */
    public String[] getPrefixes()
    {
        return prefixes.toArray(new String[prefixes.size()]);
    }

    /**
     * Add a name to the set of controllers that should be searched.
     *
     * @param name the name to add.
     */
    public void addControllerName(String name)
    {
        controllerNames.add(name);
    }

    /**
     * Remove a name from the set of controller names that should be searched.
     *
     * @param name the name to remove.
     */
    public void removeControllerName(String name)
    {
        controllerNames.remove(name);
    }

    /**
     * Get an array of controller names that are recognized by this resolver.
     *
     * @return an array of controller names that are recognized by this resolver.
     */
    public Collection<String> getControllerNames()
    {
        return controllerNames;
    }

    /**
     * Resolve the given URI to a possible URIAction.
     *
     * @param uri the uri to resolve.
     * @return a possible URIAction or null if the URI cannot be resolved.
     */
    public URIAction resolveURI(String uri)
    {
        if (uri == null || uri.equals("/"))
        {
            return null;
        }

        URIAction uriAction = cacheMap.get(uri);

        if (uriAction == null && !cacheMap.containsKey(uri))
        {
            if (uri.startsWith("/"))
            {
                uri = uri.substring(1);
            }

            int idx = uri.indexOf("/");
            String rootContext;
            if (idx != -1)
            {
                rootContext = uri.substring(0, idx);
                uri = uri.substring(idx);
            }
            else
            {
                rootContext = uri;
                uri = "";
            }
            URIResolutionContext uriContext = new URIResolutionContext(rootContext);
            uriAction = internalResolveURI(uriContext, uri);

            cacheMap.put(uri, uriAction);
        }

        return uriAction;
    }

    /**
     * Resolve the given action to a potential controller.
     *
     * @param action the URIAction to resolve.
     * @return the AppObjectController that obtained by evaluating the given action.
     */
    public AppObjectController<?, ?> resolveAction(URIAction action)
    {
        if (action != null)
        {
            URIResolutionContext context = new URIResolutionContext(action.getContext());
            return internalResolveAction(context, action);
        }
        return null;
    }

    /**
     * Iterate over the available controllers and attempt to resolve the URI.
     */
    private URIAction internalResolveURI(URIResolutionContext context, String uri)
    {
        if (providers != null)
        {
            URIAction action;
            for (URIResolutionProvider resolver : providers)
            {
                action = resolver.resolveURI(context, uri);

                if (context.isResolved())
                {
                    return action;
                }
            }
        }
        return null;
    }

    /**
     * Iterate over the available controllers and attempt to resolve the action.
     */
    private AppObjectController<?, ?> internalResolveAction(URIResolutionContext context, URIAction action)
    {
        if (providers != null)
        {
            AppObjectController<?, ?> controller;
            for (URIResolutionProvider resolver : providers)
            {
                controller = resolver.resolveAction(context, action);

                if (context.isResolved())
                {
                    return controller;
                }
            }
        }
        return null;
    }

}
