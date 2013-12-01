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

package rogue.webapp.common.resolver;

import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.AppObjectControllerCollection;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolutionContext;
import rogue.app.framework.view.urisupport.URIResolutionProvider;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.util.FacesUtils;

import java.util.Objects;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * URI resolver specifically designed to handle URIs that adhere to the following pattern.
 * <p/>
 * <pre>
 *
 *     uri = /{prefix}/pattern
 *     pattern = /{name}
 *             = /{name}/create
 *             = /{name}/delete
 *
 *             = /{name}/{id}
 *             = /{name}/{id}/update
 *             = /{name}/{id}/delete
 *
 *             = /{name}/{id}/pattern
 *
 *
 *                [prefix]
 *                   |
 *         [c1|c2|c3|c4|c5|c6|c7] <-----------
 *        /    |         |        \           |
 *       /     |         |         \          |
 *      /      |         |          \         |
 * [view]  [create]  [delete]      [id]-------
 *   |        |         |        /   |  \
 *  [*]      [*]       [*]      /    |   \
 *                             /     |    \
 *                            /      |     \
 *                      [update]   [view]  [delete]
 *                          |        |         |
 *                         [*]      [*]       [*]
 *
 * e.g., /app/users/rogue/attributes & /api/users/rogue/attributes
 *   where,
 *       'app' & 'api' - are the prefixes
 *       'users' & 'attributes' are controller names
 *       'rogue' is the id of an object under users.
 * </pre>
 */
public class AppControllerResolver implements URIResolutionProvider
{
    private static final String CLASS_NAME = AppControllerResolver.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public AppControllerResolver()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URIAction resolveURI(URIResolutionContext context, String uri)
    {
        final String METHOD_NAME = "resolveURI";
        if (uri == null)
        {
            return null;
        }

        URIAction uriAction = null;

        // 1. Split the uri on '/'
        String[] tokens = uri.split("/");

        int idx = 0;

        if (uri.startsWith("/"))
        {
            // ignore the first token
            idx++;
        }

        if (idx < tokens.length)
        {
            while (idx < tokens.length)
            {
                // 2 - The following scenario exist
                // a) The first token belongs to the container names list
                // b) The first token represents a view action after an id
                if (URIResolver.getInstance().getControllerNames().contains(tokens[idx]))
                {
                    String controllerName = tokens[idx++];
                    // The default action type is view.
                    ActionType actionType = ActionType.VIEW;
                    String id = null;

                    // 3.2 - we now the following scenarios
                    // a) There are no more tokens, in which case the action would be view
                    // b) The token could belong to action suffixes
                    // c) The token can represent an id
                    if (idx == tokens.length)
                    {
                        actionType = ActionType.VIEW;
                    }
                    else if (ActionType.ACTIONS.contains(tokens[idx]))
                    {
                        String type = tokens[idx++];
                        try
                        {
                            actionType = ActionType.lookup(type);
                        }
                        catch (IllegalArgumentException e)
                        {
                            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
                                        "Internal Error: List of actions contains search string, but evaluating it to enum has failed: " + type);
                            break;
                        }
                    }
                    else if (tokens[idx].matches("\\w+"))
                    {
                        id = tokens[idx++];
                    }
                    else
                    {
                        if (LOGGER.isLoggable(Level.FINE))
                        {
                            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unknown uri pattern: " + tokens[idx]);
                        }
                        break;
                    }

                    uriAction = uriAction == null ? new AppControllerAction(context.getRootContext(), controllerName) :
                                new AppControllerAction(controllerName, uriAction);
                    uriAction.setActionType(actionType);
                    uriAction.setId(id);
                }
                else if (ActionType.ACTIONS.contains(tokens[idx]))
                {
                    if (uriAction != null)
                    {
                        String type = tokens[idx++];
                        try
                        {
                            uriAction.setActionType(ActionType.lookup(type));
                        }
                        catch (IllegalArgumentException e)
                        {
                            LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
                                        "Internal Error: List of actions contains search string, but evaluating it to enum has failed: " + type);
                            break;
                        }

                        if (idx != tokens.length)
                        {
                            StringBuilder builder = new StringBuilder(100);
                            for (int i = idx; i < tokens.length; i++)
                            {
                                builder.append(tokens[i]).append((i + 1) != tokens.length ? "/" : "");
                            }
                            if (LOGGER.isLoggable(Level.FINE))
                            {
                                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                            "Reached a logical end in the evaluation, but more tokens found: "
                                                    + builder.toString());
                            }
                        }

                        break;
                    }
                    else
                    {
                        if (LOGGER.isLoggable(Level.FINE))
                        {
                            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                        "Invalid pattern. Not expecting a view action before a controller has been set! Found: " + tokens[idx]);
                        }
                        break;
                    }
                }
                else
                {
                    if (LOGGER.isLoggable(Level.FINE))
                    {
                        LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                    "Not a recognized uri pattern. Returning null: " + uri);
                    }
                    break;
                }
            }
        }
        else
        {
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "URI prefix not recognized: " + tokens[0]);
            }
        }

        if (uriAction != null)
        {
            context.setResolved(true);
        }
        return uriAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppObjectController<?, ?> resolveAction(URIResolutionContext uriContext, URIAction action)
    {
        final String METHOD_NAME = "resolveAction";

        if (!(action instanceof AppControllerAction))
        {
            return null;
        }

        String context = action.getContext();
        Object obj = FacesUtils.resolveObject("#{" + context + "}");
        if (obj != null)
        {
            if (obj instanceof AppObjectControllerCollection)
            {
                AppObjectControllerCollection controllerCollection = (AppObjectControllerCollection) obj;

                Stack<URIAction> stack = new Stack<>();
                URIAction ua = action;
                while (ua != null)
                {
                    stack.push(ua);
                    ua = ua.getParent();
                }

                AppObjectController resolvedController = null;
                while (!stack.isEmpty())
                {
                    ua = stack.pop();

                    if (resolvedController == null)
                    {
                        resolvedController = controllerCollection.getController(ua.getControllerName());
                    }
                    else
                    {
                        resolvedController = resolvedController.getChildController(ua.getControllerName());
                    }

                    if (ua.getId() != null)
                    {
                        Object key = resolvedController.parseId(ua.getId());
                        if (key != null)
                        {
                            // Check if the controller has already specified its currently selected resource.
                            AppObject resource = resolvedController.getSelectedResource();
                            if (resource == null || !Objects.equals(resource.getIdAsString(), ua.getId()))
                            {
                                resource = resolvedController.findById(key);
                                resolvedController.setSelectedResource(resource);
                            }
                        }
                        else
                        {
                            if (LOGGER.isLoggable(Level.FINE))
                            {
                                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                            "Controller was unable to resolve the id. Aborting processing. Controller: "
                                                    + resolvedController.getClass().getName() + "; id: " + ua.getId());
                            }
                        }
                    }
                }

                if (resolvedController == null)
                {
                    if (LOGGER.isLoggable(Level.FINE))
                    {
                        LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to resolve given action.");
                    }
                    return null;
                }

                // Now resolve the final action.
                switch (ua.getActionType())
                {
                    case CREATE:
                    {
                        // Verify if this is a post back or not. In the case of a post back, a new object should already
                        // be present. The way we identify a new object is by the absence of an id. I probably should
                        // define a method in the controller that queries and verifies if an object is a new object or
                        // not.
                        AppObject resource = resolvedController.getSelectedResource();
                        if (resource == null || resource.getId() != null)
                        {
                            // setup a new instance on the controller.
                            AppObjectController parentController = resolvedController.getParentController();
                            AppObject parent = parentController != null ? parentController.getSelectedResource() : null;
                            AppObject object = parent != null ? resolvedController.getNewInstance(parent) :
                                               resolvedController.getNewInstance();
                            resolvedController.setSelectedResource(object);
                        }
                        break;
                    }
                    case UPDATE:
                    {
                        AppObject resource = resolvedController.getSelectedResource();
                        if (resource == null)
                        {
                            break;
                        }

                        if (!resource.isMutable())
                        {
                            resource = resolvedController.getMutableInstance(resource);
                            resolvedController.setSelectedResource(resource);
                        }
                        break;
                    }
                    case DELETE:
                        // Do nothing for now
                        break;
                    case VIEW:
                        AppObject resource = resolvedController.getSelectedResource();
                        if (resource == null)
                        {
                            // Should something be done here?
                            break;
                        }
                }

                uriContext.setResolved(true);
                return resolvedController;
            }
            else
            {
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                "Resolved object is not an instance of " + AppObjectControllerCollection.class
                                        .getName() + ". Found: " + obj.getClass().getName());
                }
            }
        }
        else
        {
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to resolve bean with name: " + context);
            }
        }
        return null;
    }

    /**
     * Extension of the URIAction class that is leveraged by the {@link AppControllerResolver}.
     */
    public static final class AppControllerAction extends URIAction
    {
        public AppControllerAction(String context, String controllerName)
        {
            super(context, controllerName);
        }

        public AppControllerAction(String controllerName, URIAction parent)
        {
            super(controllerName, parent);
        }
    }
}
