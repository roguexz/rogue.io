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

package rogue.app.framework.view.faces.event;

import org.primefaces.component.dialog.Dialog;
import org.primefaces.context.RequestContext;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper bean for use with the return event processing..
 */
@Named("ReturnEventProcessor")
@ApplicationScoped
public class ReturnEventProcessor
{
    /**
     * Key for storing the client ids of the components that should updated once a selection is made.
     */
    public static final String UPDATE_COMPONENT_CLIENT_IDS = "update_client_ids";

    /**
     * Key for storing the return value as an attribute on the UIComponent that invokes the action listener.
     */
    public static final String RETURN_VALUE = "RETURN_VALUE";

    /**
     * Composite component attribute for identifying the return listener to invoke.
     */
    public static final String ATTR_RETURN_LISTENER = "returnListener";

    /**
     * Composite component attribute identifying the object on which the {@link #ATTR_METHOD method} should be invoked.
     */
    public static final String ATTR_TARGET = "target";

    /**
     * Composite component attribute identifying the method withing the {@link #ATTR_TARGET target object} that implements
     * the signature as defined by the {@link ReturnEventListener} interface.
     */
    public static final String ATTR_METHOD = "method";

    private static final String CLASS_NAME = ReturnEventProcessor.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Action listener invoked by the dialog picker action.
     * <p/>
     * TODO: Describe this in greater detail.
     *
     * @param event the action event.
     */
    public void invokeReturnEventListener(ActionEvent event)
    {
        final String METHOD_NAME = "invokeReturnEventListener";
        LOGGER.entering(CLASS_NAME, METHOD_NAME);
        boolean invocationSucceeded = false;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIComponent compositeComponent = UIComponent.getCompositeComponentParent(event.getComponent());
        if (compositeComponent != null)
        {
            Map<String, Object> ccAttrs = compositeComponent.getAttributes();
            Object returnValue = event.getComponent().getAttributes().get(RETURN_VALUE);
            ReturnEvent returnEvent = new ReturnEvent(event.getSource(), returnValue);

            Object returnListener = ccAttrs.get(ATTR_RETURN_LISTENER);
            if (returnListener != null)
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                            "Evaluating action based on return listener attribute.");

                ELContext elContext = facesContext.getELContext();
                MethodExpression methodExpression = null;


                if (returnListener instanceof MethodExpression)
                {
                    methodExpression = (MethodExpression) returnListener;
                }
                else
                {
                    // Try the value expression route. This should never really occur.
                    ValueExpression expr = compositeComponent.getValueExpression(ATTR_RETURN_LISTENER);
                    if (expr != null)
                    {
                        String expressionString = expr.getExpressionString();
                        if (expressionString != null)
                        {
                            methodExpression = facesContext.getApplication().getExpressionFactory()
                                                           .createMethodExpression(elContext, expressionString, null,
                                                                                   new Class[]{ReturnEvent.class});

                        }
                    }
                }

                // Execute method expression
                if (methodExpression != null)
                {
                    methodExpression.invoke(elContext, new Object[]{returnEvent});
                    invocationSucceeded = true;
                }
                else
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Return listener method expression was null.");
                }
            }
            else
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                            "Evaluating action based on target & method attributes.");

                Object target = ccAttrs.get(ATTR_TARGET);
                String methodName = (String) ccAttrs.get(ATTR_METHOD);
                if (target != null && methodName != null)
                {
                    try
                    {
                        Method methodRef = target.getClass().getMethod(methodName, ReturnEvent.class);
                        methodRef.setAccessible(true);
                        methodRef.invoke(target, returnEvent);
                        methodRef.setAccessible(false);

                        invocationSucceeded = true;
                    }
                    catch (NoSuchMethodException e)
                    {
                        LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                    String.format("Unable to find method %s(ReturnEvent) in target class %s",
                                                  methodName, target.getClass()));
                    }
                    catch (InvocationTargetException | IllegalAccessException e)
                    {
                        LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                    String.format("Unable to invoke method %s(ReturnEvent) on target object %s",
                                                  methodName, target));
                    }
                }
                else
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                String.format(
                                        "Target object or method name are null [Object: %s , Method: %s]. Cannot execute return listener.",
                                        target, methodName));
                }
            }
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Composite component is null.");
        }

        if (invocationSucceeded)
        {
            // Update the required components.
            Dialog dialog = getParentDialog(event.getComponent());
            if (dialog != null)
            {
                String clientIds = (String) dialog.getAttributes().get(UPDATE_COMPONENT_CLIENT_IDS);

                if (clientIds != null)
                {
                    RequestContext.getCurrentInstance().update(Arrays.asList(clientIds.split(",")));
                }
            }
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        "Unable to invoke return action. Increase logging to fine to see the cause.");
        }

        LOGGER.exiting(CLASS_NAME, METHOD_NAME);
    }

    public String getParentDialogWidgetVariable()
    {
        final String KEY = "DlgFwk_dialog_key";
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIComponent currentComponent = UIComponent.getCurrentComponent(facesContext);
        String widgetVar = null;
        if (currentComponent != null)
        {
            Map<String, Object> attributes = currentComponent.getAttributes();
            widgetVar = (String) currentComponent.getAttributes().get(KEY);

            if (widgetVar == null && !attributes.containsKey(KEY))
            {
                Dialog dialog = getParentDialog(currentComponent);
                widgetVar = dialog != null ? dialog.resolveWidgetVar() : null;
                currentComponent.getAttributes().put(KEY, widgetVar);
            }
        }
        return widgetVar;
    }

    private Dialog getParentDialog(UIComponent component)
    {
        Dialog parent = null;
        while (component != null)
        {
            component = component.getParent();

            if (component instanceof Dialog)
            {
                parent = (Dialog) component;
                break;
            }
        }
        return parent;
    }
}
