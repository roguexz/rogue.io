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

import java.util.EventObject;

/**
 * A <code>ReturnEvent</code> encapsulates the value to be returned from a picker component.
 */
public class ReturnEvent extends EventObject
{
    private Object retValue;

    public ReturnEvent(Object source, Object retValue)
    {
        super(source);
        this.retValue = retValue;
    }

    public Object getReturnValue()
    {
        return retValue;
    }
}
