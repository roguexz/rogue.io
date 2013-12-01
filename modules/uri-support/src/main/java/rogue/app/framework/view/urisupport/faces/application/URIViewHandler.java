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

package rogue.app.framework.view.urisupport.faces.application;

import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.view.PageInfo;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.faces.AbstractFacesController;
import rogue.app.framework.view.faces.component.ControllerContextHolder;
import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.urisupport.internal.URIUtil;

import javax.faces.application.ResourceHandler;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Faces view handler for supporting pretty URLs.
 */
public class URIViewHandler extends ViewHandlerWrapper
{
    private static final String CLASS_NAME = URIViewHandler.class.getName();
    private static final Logger LOGGER = URIUtil.LOGGER;

    public static final String FRAMEWORK_PAGE_TO_INCLUDE = "framework_page_to_include";
    public static final String FRAMEWORK_PAGE_TO_RENDER = "framework_page_to_render";
    // TODO: Figure out how this should be refactored elegantly.
    public static final String ADMIN_PAGE_TEMPLATE = "/framework/templates/admin/page-template.xhtml";

    private ViewHandler wrapped;

    public URIViewHandler(ViewHandler wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ViewHandler getWrapped()
    {
        return wrapped;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId)
    {
        URIAction action = URIResolver.getInstance().resolveURI(viewId);
        if (action != null)
        {
            if (setupControllerContext(context, action))
            {
                ViewDeclarationLanguage vdl = getViewDeclarationLanguage(context, viewId);
                return vdl.createView(context, viewId);
            }
            else
            {
                try
                {
                    viewId = ADMIN_PAGE_TEMPLATE;
                    context.responseComplete();
                    context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Page Not Found");
                }
                catch (IOException e)
                {
                    LOGGER.logp(Level.FINEST, CLASS_NAME, "createView", "Failed setting 404 error", e);
                }
            }
        }
        return super.createView(context, viewId);
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId)
    {
        URIAction action = URIResolver.getInstance().resolveURI(viewId);
        if (action != null)
        {
            if (setupControllerContext(context, action))
            {
                ViewDeclarationLanguage vdl = getViewDeclarationLanguage(context, viewId);
                return vdl.restoreView(context, viewId);
            }
            else
            {
                try
                {
                    viewId = ADMIN_PAGE_TEMPLATE;
                    context.responseComplete();
                    context.getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, "Page Not Found");
                }
                catch (IOException e)
                {
                    LOGGER.logp(Level.FINEST, CLASS_NAME, "createView", "Failed setting 404 error", e);
                }
            }
        }
        return super.restoreView(context, viewId);
    }

    @Override
    public String deriveViewId(FacesContext context, String input)
    {
        // The default implementation tries to verify if a physical file exists or not.
        // For the case of URI actions, we need to override this method.
        return URIResolver.getInstance().resolveURI(input) != null ? input : super.deriveViewId(context, input);
    }

    @Override
    public String deriveLogicalViewId(FacesContext context, String input)
    {
        // The default implementation tries to verify if a physical file exists or not.
        // For the case of URI actions, we need to override this method.
        return URIResolver.getInstance().resolveURI(input) != null ? input : super.deriveLogicalViewId(context, input);
    }

    @Override
    public String getActionURL(FacesContext context, String viewId)
    {
        return beautifyURL(context, super.getActionURL(context, viewId), viewId);
    }

    @Override
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters,
                                 boolean includeViewParams)
    {
        return beautifyURL(context, super.getRedirectURL(context, viewId, parameters, includeViewParams), viewId);
    }

    @Override
    public String getBookmarkableURL(FacesContext context, String viewId, Map<String, List<String>> parameters,
                                     boolean includeViewParams)
    {
        return beautifyURL(context, super.getBookmarkableURL(context, viewId, parameters, includeViewParams), viewId);
    }

    @Override
    public String getResourceURL(FacesContext context, String path)
    {
        int idx = path.indexOf(ResourceHandler.RESOURCE_IDENTIFIER);
        if (idx > 0)
        {
            String ori = path;
            // trim
            path = path.substring(idx);

            String suffix = URIUtil.getFaceletSuffix();
            idx = path.indexOf('?');
            if (idx != -1)
            {
                path = path.substring(0, idx) + suffix + path.substring(idx);
            }
            else
            {
                path = path + suffix;
            }

            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, "getResourceURL",
                            "Cleaned up resource url from " + ori + " to " + path);
            }
        }
        return super.getResourceURL(context, path);
    }

    /**
     * Remove the extension mapping from the URL.
     *
     * @param targetURL the input URL
     * @param viewId    the current view Id
     * @return a prettified URL
     */
    private String beautifyURL(FacesContext context, String targetURL, String viewId)
    {
        String currentViewId = context.getViewRoot().getViewId();
        if (targetURL.startsWith(currentViewId + viewId))
        {
            // Remove one level
            targetURL = targetURL.substring(currentViewId.length());
        }

        String suffix = URIUtil.getFaceletSuffix();
        if (targetURL != null && suffix != null)
        {
            int idx = targetURL.indexOf(suffix);
            int qMark = targetURL.indexOf("?");
            if (idx != -1 && (qMark == -1 || idx < qMark))
            {
                targetURL = targetURL.substring(0, idx) + targetURL.substring(idx + suffix.length());
            }
        }
        return targetURL;
    }

    /**
     * Resolve the controller and store it on the controllerContext. Also store the page fragment location in the
     * request scope so that it can be included in the view.
     *
     * @param context the FacesContext instance
     * @param action  the URIAction that needs to be handled.
     * @return true if the resolution was successful, false otherwise.
     */
    private boolean setupControllerContext(FacesContext context, URIAction action)
    {
        final String METHOD_NAME = "setupControllerContext";
        if (action != null)
        {
            AppObjectController controller = URIResolver.getInstance().resolveAction(action);

            if (controller == null && action.getViewId() != null)
            {
                context.getExternalContext().getRequestMap()
                       .put(FRAMEWORK_PAGE_TO_RENDER, action.getViewId());
                if (LOGGER.isLoggable(Level.FINEST))
                {
                    LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                                "Controller resolved to null. Rendering the viewId that has been set instead: " + action
                                        .getViewId());
                }
                return true;
            }
            else if (controller instanceof AbstractFacesController)
            {
                AbstractFacesController viewController = (AbstractFacesController) controller;
                PageType pageType = getPageType(action);
                PageInfo pageInfo = viewController.getPageInfo(pageType, action.getActionType());
                if (pageInfo != null)
                {
                    if (pageInfo.isFragment())
                    {
                        context.getExternalContext().getRequestMap()
                               .put(FRAMEWORK_PAGE_TO_INCLUDE, pageInfo.getPagePath());
                        context.getExternalContext().getRequestMap()
                               .put(FRAMEWORK_PAGE_TO_RENDER, ADMIN_PAGE_TEMPLATE);

                        if (LOGGER.isLoggable(Level.FINEST))
                        {
                            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                                        "Found a fragment. Rendering the admin page template for fragment: "
                                                + pageInfo.getPagePath());
                        }

                    }
                    else
                    {
                        context.getExternalContext().getRequestMap()
                               .put(FRAMEWORK_PAGE_TO_RENDER, pageInfo.getPagePath());

                        if (LOGGER.isLoggable(Level.FINEST))
                        {
                            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                                        "Rendering page: " + pageInfo.getPagePath());
                        }
                    }

                    if (LOGGER.isLoggable(Level.FINEST))
                    {
                        LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                                    "Pushing controller: " + viewController);
                    }

                    ControllerContextHolder.getInstance().pushController(viewController);

                    return true;
                }
                else
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                "Unable to get page information for action: " + action.toString());
                }
            }
            else
            {
                if (controller != null)
                {
                    LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                                "Resolved controller is not an instance of " + AbstractFacesController.class.getName()
                                        + ". Do not know how to handle it. This is possibly an oversight of a design change. This should be fixed. Found: "
                                        + controller.getClass().getName());

                }
                else
                {
                    if (LOGGER.isLoggable(Level.FINEST))
                    {
                        LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Controller resolved to null." +
                                " Possibly because the controller is not part of the bean that got resolved: " + action);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Get the page type that is appropriate for the given action.
     *
     * @param action the action to resolve.
     * @return the page type that is appropriate for the given action.
     */
    private PageType getPageType(URIAction action)
    {
        PageType type = PageType.GENERIC;
        switch (action.getActionType())
        {
            case CREATE:
            case UPDATE:
                type = PageType.ITEM_DETAILS;
                break;
            case DELETE:
                type = PageType.GENERIC;
                break;
            case VIEW:
                type = action.getId() == null ? PageType.ITEMS_LISTING : PageType.ITEM_DETAILS;
        }

        return type;
    }
}
