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

package rogue.app.framework.view.faces.validator;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.util.FacesUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UniqueNameValidator<T extends PersistentObject<T>> implements Validator, Serializable
{
    private String methodName = null;
    private String queryName = null;
    private String paramName = null;
    private EntityFacesController<T> controller;
    private Map<String, Object> additionalParams;

    public UniqueNameValidator(String methodName, String queryName, String paramName,
                               EntityFacesController<T> controller)
    {
        this(methodName, queryName, paramName, controller, null);
    }

    public UniqueNameValidator(String methodName, String queryName, String paramName,
                               EntityFacesController<T> controller,
                               Map<String, Object> additionalParams)
    {
        this.methodName = methodName;
        if (queryName == null)
        {
            throw new IllegalArgumentException("Query name cannot be null.");
        }

        if (paramName == null)
        {
            throw new IllegalArgumentException("Parameter name cannot be null.");
        }

        if (controller == null)
        {
            throw new IllegalArgumentException("Entity controller cannot be null.");
        }

        this.queryName = queryName;
        this.paramName = paramName;
        this.controller = controller;
        this.additionalParams = additionalParams;
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        if (value != null && value instanceof String)
        {
            T obj = controller.getSelectedResource();

            if (obj != null)
            {
                if (methodName != null)
                {
                    try
                    {
                        Method m = obj.getClass().getMethod(methodName, new Class[0]);
                        if (m != null)
                        {
                            Object oldValue = m.invoke(obj);
                            if (oldValue instanceof String)
                            {
                                if (Objects.equals(StringUtils.lowerCase((String) value),
                                                   StringUtils.lowerCase((String) oldValue)))
                                {
                                    return;
                                }
                            }
                            else if (Objects.equals(value, oldValue))
                            {
                                return;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                Map<String, Object> queryParams = new HashMap<>(4);
                queryParams.put(paramName, StringUtils.lowerCase((String) value));

                if (additionalParams != null)
                {
                    queryParams.putAll(additionalParams);
                }

                Long count = controller.getDelegate().executeNamedQuerySingleResult(queryName, queryParams);
                if (count != null && count != 0)
                {
                    String msg = String.format("An item with the name '%s' already exists", value);
                    if (UIInput.class.isAssignableFrom(component.getClass()))
                    {
                        msg = FacesUtils.getValidatorMessage((UIInput) component, msg);
                    }
                    throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
                }
            }
        }
    }
}
