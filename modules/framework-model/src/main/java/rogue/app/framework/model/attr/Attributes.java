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

package rogue.app.framework.model.attr;

import java.util.List;

/**
 * Identifies that the implementing object has support for attributes. This interface does not define customizations.
 * All operations will be performed on the base attribute definition.
 */
public interface Attributes
{
    public static final String SYSTEM_NAMESPACE = "/system";

    /**
     * Get a list of attribute names associated with this object.
     *
     * @return a list of attribute names associated with this object. This method will return an empty list if no
     *         attributes were found.
     */
    public List<String> getAttributeNames();

    /**
     * Returns true if an attribute by the given name is defined, false otherwise.
     *
     * @param name name of the attribute
     * @return true if an attribute by the given name is defined, false otherwise.
     */
    public boolean isAttributeDefined(String name);

    /**
     * Get a named attribute associated with the current object.
     *
     * @param name the name of the attribute.
     * @return a named attribute associated with the current object or null if no such attribute was found.
     */
    public String getAttribute(String name);

    /**
     * Set an attribute on the current object. If an attribute with the same name already exists, then this method
     * will overwrite it.
     *
     * @param name  name of the attribute.
     * @param value value of the attribute.
     */
    public void setAttribute(String name, String value);

    /**
     * Remove a named attribute, if it exists.
     *
     * @param name the name of the attribute.
     */
    public void removeAttribute(String name);
}
