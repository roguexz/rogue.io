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

package rogue.app.framework.search;

import com.google.appengine.api.search.Field;

import java.lang.annotation.*;

/**
 * Annotation associated with methods of <code>AppObject</code>s that are {@link SearchIndex searchable}. Any method
 * having this annotation will be invoked and stored in the search document.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SearchField
{
    /**
     * The name of the field.
     *
     * @return name of the field.
     */
    String name();

    /**
     * The type of data represented by this field.
     *
     * @return type of data represented by this field.
     */
    Field.FieldType type();
}
