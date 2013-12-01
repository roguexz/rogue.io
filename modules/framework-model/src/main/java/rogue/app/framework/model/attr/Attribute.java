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

import rogue.app.framework.model.PersistentObject;

/**
 * Identifies attributes that can be associated with various application objects. These attributes can be
 * <i>customized</i> to have a different value for a different <i>layer</i>. For example,
 * <pre>
 *     System administrator sets the default timezone to be UTC+0. This value can be stored at the system layer.
 *     A user can update this value to his choice and this would then be stored at the user layer.
 * </pre>
 */
public interface Attribute extends PersistentObject<Attribute>
{
    /**
     * Get the name space associated with this attribute.
     *
     * @return the name space associated with this attribute.
     */
    public String getNameSpace();

    /**
     * Set the name space associated with this attribute.
     *
     * @param nameSpace the name space associated with this attribute.
     */
    public void setNameSpace(String nameSpace);

    /**
     * Get the name of this attribute.
     *
     * @return the name of this attribute.
     */
    public String getName();

    /**
     * Set the name of this attribute.
     *
     * @param name the name of this attribute.
     */
    public void setName(String name);

    /**
     * Get the value of this attribute.
     *
     * @return the value of this attribute.
     */
    public String getValue();

    /**
     * Set the value of this attribute.
     *
     * @param value the value of this attribute.
     */
    public void setValue(String value);

    /**
     * Get the description associated with this attribute. This is an optional field and is not mandatory.
     *
     * @return the description associated with this attribute.
     */
    public String getDescription();

    /**
     * Set the description associated with this attribute.
     *
     * @param description the description associated with this attribute.
     */
    public void setDescription(String description);

    /**
     * Get the application object with which this attribute is associated.
     *
     * @return the application object with which this attribute is associated.
     */
    public PersistentObject<?> getAppObject();

    /**
     * Set the application object with which this attribute is associated.
     *
     * @param appObject the application object with which this attribute is associated.
     */
    public void setAppObject(PersistentObject<?> appObject);

    /**
     * Get the customization layer associated with this attribute.
     *
     * @return the customization layer associated with this attribute.
     */
    public Layer getCustomizationLayer();

    /**
     * Set the customization layer associated with this attribute.
     *
     * @param layer the customization layer associated with this attribute.
     */
    public void setCustomizationLayer(Layer layer);
}
