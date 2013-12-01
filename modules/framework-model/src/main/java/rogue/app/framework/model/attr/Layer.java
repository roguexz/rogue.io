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

import rogue.app.framework.model.NamedResource;
import rogue.app.framework.model.NestedResource;
import rogue.app.framework.model.PersistentObject;

/**
 * Identifies a layer at which attribute customizations may be done. Layer objects can be classified in to two types:
 * <ul>
 * <li>
 * Standalone Layers - A layer whose information is captured via its name & description e.g., APPLICATION, USER, etc.
 * </li>
 * <li>
 * Dependent Layers - A layer that represents an existing application object, e.g., AppLocale based layer.
 * Information about the referenced object is captured via the object's {@link #getBaseReference() basereference}
 * member.
 * </li>
 * </ul>
 */
public interface Layer extends PersistentObject<Layer>, NestedResource<Layer>, NamedResource
{
    /**
     * Get the base app object reference, if this layer is based on another application object.
     *
     * @return the base app object reference
     */
    public PersistentObject<?> getBaseReference();

    /**
     * Set the base app object reference.
     *
     * @param reference the base app object reference
     */
    public void setBaseReference(PersistentObject<?> reference);

}
