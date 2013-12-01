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

import rogue.app.framework.view.util.FacesUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FacesValidator("user.email.validator")
public class EmailValidator implements Validator
{
    private Pattern pattern;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public EmailValidator()
    {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        if (value instanceof String)
        {
            String[] emailIds = {(String) value};

            final String MULTIPLE_EMAIL_IDS = "multiple.email.ids";
            final String SEPARATOR_CHAR = "separator.char";
            boolean multipleAddresses = false;
            if (Boolean.parseBoolean(Objects.toString(component.getAttributes().get(MULTIPLE_EMAIL_IDS))))
            {
                String separatorChar = Objects.toString(component.getAttributes().get(SEPARATOR_CHAR));
                emailIds = emailIds[0].split(separatorChar);
                multipleAddresses = true;
            }
            else if (Boolean.parseBoolean(Objects.toString(
                    component.getNamingContainer().getAttributes().get(MULTIPLE_EMAIL_IDS))))
            {
                String separatorChar =
                        Objects.toString(component.getNamingContainer().getAttributes().get(SEPARATOR_CHAR));
                emailIds = emailIds[0].split(separatorChar);
                multipleAddresses = true;
            }

            for (String s : emailIds)
            {
                Matcher matcher = pattern.matcher(s);
                if (!matcher.matches())
                {
                    String msg = String.format("%s is not a valid email address.", s);
                    if (UIInput.class.isAssignableFrom(component.getClass()))
                    {
                        if (!multipleAddresses)
                        {
                            msg = FacesUtils.getValidatorMessage((UIInput) component, msg);
                        }
                        else
                        {
                            // in the case of a ajax update, the component would have already stored a previous message.
                            // but for the scenario of multiple email id support, we need to refresh the validation
                            // error message.
                            ((UIInput) component).setValidatorMessage(msg);
                        }
                    }
                    FacesMessage message =
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                    throw new ValidatorException(message);
                }
            }
        }
    }
}
