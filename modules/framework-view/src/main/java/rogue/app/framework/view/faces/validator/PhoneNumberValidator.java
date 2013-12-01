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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@FacesValidator("user.phone.number")
public class PhoneNumberValidator implements Validator
{
    public static final String REGEX_PATTERN = "regex_pattern";
    public static final String DEFAULT_MOBILE_PATTERN = "^[0-9]{10}";

    private static final String CLASS_NAME = PhoneNumberValidator.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        final String METHOD_NAME = "validate";
        if (value instanceof String)
        {
            String pattern = (String) component.getAttributes().get(REGEX_PATTERN);

            String msg = "Specify a valid mobile number";
            if (UIInput.class.isAssignableFrom(component.getClass()))
            {
                msg = FacesUtils.getValidatorMessage((UIInput) component, msg);
            }

            if (pattern == null)
            {
                pattern = DEFAULT_MOBILE_PATTERN;
                LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                            "No regex pattern specified. Using the default 10digit pattern.");
            }
            if (!Pattern.matches(pattern, (String) value))
            {
                FacesMessage message =
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
                throw new ValidatorException(message);
            }
        }
    }
}
