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

package rogue.app.framework.support.primefaces.render;

import org.joda.time.DateTime;
import org.primefaces.component.schedule.Schedule;

import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extension of the PrimeFaces schedule renderer
 */
// TODO: Remove once http://code.google.com/p/primefaces/issues/detail?id=4794 has been fixed.
public class ScheduleRenderer extends org.primefaces.component.schedule.ScheduleRenderer
{
    private static final String CLASS_NAME = ScheduleRenderer.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final String FIRST_DAY = "firstDay";
    private static final String SET_FIRST_DAY = "setFirstDayToInitial";

    @Override
    protected void encodeClientBehaviors(FacesContext context, ClientBehaviorHolder component) throws IOException
    {
        final String METHOD_NAME = "encodeClientBehaviors";

        ResponseWriter writer = context.getResponseWriter();

        // Add the extra attributes here.
        Schedule schedule = (Schedule) component;
        Map<String, Object> attrMap = schedule.getAttributes();
        if (attrMap.containsKey(FIRST_DAY))
        {
            // Verify that you can parse it as a number
            String value = attrMap.get(FIRST_DAY).toString();
            try
            {
                int n = Integer.parseInt(value);
                n = n % 7;
                writer.write("," + FIRST_DAY + ":" + n);
            }
            catch (NumberFormatException e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Invalid argument given for first day: " + value);
            }
        }
        else if (attrMap.containsKey(SET_FIRST_DAY) && Boolean.parseBoolean(attrMap.get(SET_FIRST_DAY).toString()))
        {
            DateTime dt = new DateTime(schedule.getInitialDate());
            writer.write("," + FIRST_DAY + ":" + dt.toString("e"));
        }
        super.encodeClientBehaviors(context, component);
    }
}
