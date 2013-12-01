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

package rogue.app.framework.view.faces.converter;

import rogue.app.framework.security.AccountStatus;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rogue
 *         AccountStatusConverter.java - 22/05/12 7:15 PM
 */
@FacesConverter("rogue.app.framework.security.AccountStatus")
public class AccountStatusConverter implements Converter, Serializable
{
    private static final String SRC_CLASS = AccountStatusConverter.class.getName();
    private static final Logger LOGGER = Logger.getLogger(SRC_CLASS);

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value)
    {
        final String METHOD_NAME = "getAsObject";
        if (value == null || value.trim().length() == 0)
        {
            return null;
        }

        try
        {
            int accStatus = Integer.parseInt(value);
            return AccountStatus.fromValue(accStatus, AccountStatus.PENDING_APPROVAL);
        }
        catch (NumberFormatException e)
        {
            LOGGER.logp(Level.WARNING, SRC_CLASS, METHOD_NAME, "Failed to get account status from value: " + value, e);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value)
    {
        if (value == null)
        {
            return null;
        }

        if (AccountStatus.class.isAssignableFrom(value.getClass()))
        {
            return Integer.toString(((AccountStatus) value).getValue());
        }
        else
        {
            throw new IllegalArgumentException(
                    "Object " + value + " if of type " + value.getClass().getName() +
                            "; Expected type: " + AccountStatus.class.getName());

        }
    }
}
