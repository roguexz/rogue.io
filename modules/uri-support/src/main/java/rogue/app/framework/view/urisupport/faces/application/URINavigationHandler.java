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

import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.urisupport.internal.URIUtil;

import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.ConfigurableNavigationHandlerWrapper;
import javax.faces.application.NavigationCase;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewAction;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;
import javax.faces.flow.FlowHandler;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Navigation handler for the URI support module.
 */
public class URINavigationHandler extends ConfigurableNavigationHandlerWrapper
{
    private static final String CLASS_NAME = URINavigationHandler.class.getName();
    private static final Logger LOGGER = URIUtil.LOGGER;
    private static final Pattern REDIRECT_EQUALS_TRUE = Pattern.compile("(.*)(faces-redirect=true)(.*)");
    private static final Pattern INCLUDE_VIEW_PARAMS_EQUALS_TRUE = Pattern.compile("(.*)(includeViewParams=true)(.*)");


    private ConfigurableNavigationHandler wrapped;

    public URINavigationHandler(ConfigurableNavigationHandler wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ConfigurableNavigationHandler getWrapped()
    {
        return wrapped;
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome)
    {
        final String METHOD_NAME = "getNavigationCase";
        URIAction action = URIResolver.getInstance().resolveURI(outcome);
        if (action != null)
        {
            if (LOGGER.isLoggable(Level.FINEST))
            {
                LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                            "Constructing a navigation case pointing to a URIAction: " + action.toString());
            }
            return new URINavigationCase(action, context.getViewRoot().getViewId(), null, outcome, null,
                                         action.toString(), null, true, false);
        }
        return wrapped.getNavigationCase(context, fromAction, outcome);
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome)
    {
        final String METHOD_NAME = "handleNavigation";
        NavigationCase navigationCase = getNavigationCase(context, fromAction, outcome);
        if (navigationCase instanceof URINavigationCase)
        {
            URINavigationCase unc = (URINavigationCase) navigationCase;
            URIAction action = unc.getURIAction();

            if (LOGGER.isLoggable(Level.FINEST))
            {
                LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                            "Handling navigation for URIAction: " + action.toString());
            }

            boolean isRedirect = unc.isRedirect();
            boolean isIncludeViewParams = unc.isIncludeViewParams();

            ExternalContext extContext = context.getExternalContext();
            ViewHandler viewHandler = context.getApplication().getViewHandler();
            Flash flash = extContext.getFlash();
            boolean isUIViewActionBroadcastAndViewIdsDiffer = false;
            if (UIViewAction.isProcessingBroadcast(context))
            {
                flash.setKeepMessages(true);
                String viewIdBefore = context.getViewRoot().getViewId();
                viewIdBefore = (null == viewIdBefore) ? "" : viewIdBefore;
                String viewIdAfter = outcome;
                viewIdAfter = (null == viewIdAfter) ? "" : viewIdAfter;
                isUIViewActionBroadcastAndViewIdsDiffer = !viewIdBefore.equals(viewIdAfter);
            }
            if (isRedirect || isUIViewActionBroadcastAndViewIdsDiffer)
            {

                // perform a 302 redirect.
                String redirectUrl = viewHandler.getRedirectURL(context, outcome, null, isIncludeViewParams);
                try
                {
                    if (LOGGER.isLoggable(Level.FINE))
                    {
                        LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                    "Redirecting to path {0} for outcome {1} and viewId {2}",
                                    new Object[]{redirectUrl, outcome, outcome});
                    }
                    // encode the redirect to ensure session state
                    // is maintained
                    updateRenderTargets(context, outcome);
                    flash.setRedirect(true);
                    extContext.redirect(redirectUrl);
                }
                catch (java.io.IOException ioe)
                {
                    if (LOGGER.isLoggable(Level.FINE))
                    {
                        LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "jsf.redirect_failed_error", redirectUrl);
                    }
                    throw new FacesException(ioe.getMessage(), ioe);
                }
                context.responseComplete();
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Response complete for {0}", outcome);
                }
            }
            else
            {
                UIViewRoot newRoot = viewHandler.createView(context, outcome);
                updateRenderTargets(context, outcome);
                context.setViewRoot(newRoot);
                FlowHandler flowHandler = context.getApplication().getFlowHandler();
                if (null != flowHandler)
                {
                    // TODO: Figure this part out.
                    LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                                "Flow handler is not null in URIAction case. This case needs to be handled.");
//                    flowHandler.transition(context,
//                                           caseStruct.currentFlow, caseStruct.newFlow,
//                                           caseStruct.facesFlowCallNode, outcome);
                }
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Set new view in FacesContext for {0}", outcome);
                }
            }
            clearViewMapIfNecessary(context, outcome);

        }
        else
        {
            wrapped.handleNavigation(context, fromAction, outcome);
        }
    }

    private void updateRenderTargets(FacesContext context, String newId)
    {
        if (context.getViewRoot() == null || !context.getViewRoot().getViewId().equals(newId))
        {
            PartialViewContext pctx = context.getPartialViewContext();
            if (!pctx.isRenderAll())
            {
                pctx.setRenderAll(true);
            }
        }
    }

    /**
     * Calls <code>clear()</code> on the ViewMap (if available) if the view
     * ID of the UIViewRoot differs from <code>newId</code>
     */
    private void clearViewMapIfNecessary(FacesContext context, String newId)
    {
        UIViewRoot root = context.getViewRoot();

        if (root != null && !root.getViewId().equals(newId))
        {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null)
            {
                viewMap.clear();
            }
        }
    }
}
