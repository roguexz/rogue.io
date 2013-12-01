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

/**
 * Identifies that the implementing object has support for attribute customization.
 */
public interface CustomizedAttributes extends Attributes
{
    /**
     * Returns true if the specified named attribute exists and has been customized at the given layer.
     *
     * @param name  the name of the attribute.
     * @param layer the customization layer.
     * @return true if the specified named attribute exists and has been customized at the given layer.
     */
    public boolean isAttributeCustomized(String name, Layer layer);

    /**
     * Get a named attribute, customized at the specified layer. If a value does not exist at the specified layer,
     * then the parent layer is checked (recursively), till a value is found.
     *
     * @param name  the name of the attribute.
     * @param layer the layer where to start looking from.
     * @return the named attribute, or null if no such attribute was found.
     */
    public String getAttribute(String name, Layer layer);

    /**
     * Set an attribute at the given customization layer.
     *
     * @param name  name of the attribute.
     * @param value value of the attribute.
     * @param layer the customization layer at which the value should be persisted.
     */
    public void setAttribute(String name, String value, Layer layer);

    /**
     * Remove a named attribute, at the specified customization layer, if available.
     *
     * @param name  the name of the attribute.
     * @param layer the customization layer.
     */
    public void removeAttribute(String name, Layer layer);
}
