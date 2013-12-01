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

package rogue.app.framework.annotations;

import rogue.app.framework.model.AppObject;

import java.lang.annotation.*;

/**
 * Annotates a class indicating that it is a controller for the specified model class.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ControllerFor
{
    /**
     * The name of the controller. This is used by the URI support feature for linking up controllers.
     *
     * @return the name of the controller.
     */
    String name();

    /**
     * The <code>AppObject</code> class reference for which the annotated object is a controller of.
     */
    Class<? extends AppObject> model();

    /**
     * Classification of the controller, if more than one controller exists for the same model class.
     * <p/>
     * TODO: Not yet well defined. Ignore this.
     */
    String type() default "";
}
