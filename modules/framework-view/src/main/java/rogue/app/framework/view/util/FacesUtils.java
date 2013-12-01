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

package rogue.app.framework.view.util;


import org.apache.commons.lang.StringUtils;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import java.util.logging.Level;
import java.util.logging.Logger;


// TODO: Do I still need this class?
public class FacesUtils
{
    private static final String CLASS_NAME = FacesUtils.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static <T> T resolveObject(String expression)
    {
        final String METHOD_NAME = "resolveObject";
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to acquire faces context. Returning null");
            return null;
        }

        Application app = ctx.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = ctx.getELContext();
        ValueExpression valueExp =
                elFactory.createValueExpression(elContext, expression,
                                                Object.class);
        return (T) valueExp.getValue(elContext);
    }

    /**
     * Get the validator message set on the component. If the validator message is empty, then this method will set
     * the given message as the component's validator message and also return the same.
     * <p/>
     * Strangely, in 2.1.16 the UIInput kept returning an empty value for validatorMessage which meant that my message
     * was never being displayed! The composite component stuff has quite a few bugs :( ... or I totally don't
     * understand it.
     *
     * @param inputComponent the UIInput component.
     * @param defaultMessage the message to set, if nothing was present.
     * @return the validator message set on the component.
     */
    public static String getValidatorMessage(UIInput inputComponent, String defaultMessage)
    {
        String msg = defaultMessage;
        if (inputComponent != null)
        {
            msg = inputComponent.getValidatorMessage();
            if (StringUtils.isEmpty(msg))
            {
                msg = defaultMessage;
                inputComponent.setValidatorMessage(defaultMessage);
            }
        }
        return msg;
    }

    public static void addErrorMessage(String msg, Exception ex)
    {
        if (msg == null)
        {
            msg = ex.getLocalizedMessage();
        }
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, ex.getLocalizedMessage());
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String summary, String detail)
    {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
}
