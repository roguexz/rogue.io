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

package rogue.app.framework.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static rogue.app.framework.view.PageType.GENERIC;

/**
 * Annotation for identifying a Page and its associated meta information. This annotation is primarily used by the
 * view layer entity managers.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageMetadata
{
    /**
     * The display name of the page.
     *
     * @return the display name of the page.
     */
    String displayName() default "";

    /**
     * The faces navigation-outcome.
     *
     * @return the faces navigation-outcome.
     */
    String outcome() default "";

    /**
     * The type of the page.
     *
     * @return the type of the page.
     */
    PageType pageType() default GENERIC;

    /**
     * The path to the page.
     */
    String path();

    /**
     * Is this a full page or a page fragment.
     */
    boolean fragment() default false;

    /**
     * They view action with which this page entry is associated.
     */
    ActionType[] type() default ActionType.VIEW;

}
