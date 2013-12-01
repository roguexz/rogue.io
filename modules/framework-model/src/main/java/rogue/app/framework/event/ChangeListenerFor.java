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

package rogue.app.framework.event;

import java.lang.annotation.*;

/**
 * Identifies a static method that wishes to be notified when an application object's value changes. The method
 * signature should be as follows:
 * <pre>
 *     {@literal @}ChangeListenerFor(sourceClass=<i>OneOfTheApplicationObjectClasses</i>)
 *      static void <i>someMethodName</i>(ChangeEvent event);
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChangeListenerFor
{
    /**
     * The type of objects that the currently annotated type is interested in.
     *
     * @return The type of objects that the currently annotated type is interested in.
     */
    Class<?> sourceClass();
}
