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

package rogue.app.framework.model;

import com.google.appengine.api.search.Field;
import rogue.app.framework.search.SearchField;

/**
 * Identifies a named resource. Most objects within real life scenarios have a name and a description. This is a utility
 * interface that helps reduce the amount of code to be written with every other class.
 */
public interface NamedResource
{
    /**
     * Get the name of this resource.
     *
     * @return the name of this resource.
     */
    @SearchField(name = "name", type = Field.FieldType.TEXT)
    public String getName();

    /**
     * Set the name of this resource.
     *
     * @param name the name of this resource.
     */
    public void setName(String name);

    /**
     * Get the description of this resource.
     *
     * @return the description of this resource.
     */
    @SearchField(name = "description", type = Field.FieldType.TEXT)
    public String getDescription();

    /**
     * Set the description of this resource.
     *
     * @param description the description of this resource.
     */
    public void setDescription(String description);
}
