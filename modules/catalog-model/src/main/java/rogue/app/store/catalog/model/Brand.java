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

package rogue.app.store.catalog.model;

import rogue.app.framework.model.NamedResource;
import rogue.app.framework.model.NestedResource;
import rogue.app.framework.model.PersistentObject;

import java.util.List;

/**
 * Represents a multi-level brand with associated products.
 */
public interface Brand extends PersistentObject<Brand>, NestedResource<Brand>, NamedResource
{
    /**
     * Get whether this brand has been enabled or not.
     *
     * @return true if this brand is enabled, false otherwise.
     */
    public Boolean getEnabled();

    /**
     * Set whether this brand has been enabled or not.
     *
     * @param enabled true if this brand is enabled, false otherwise.
     */
    public void setEnabled(Boolean enabled);

    /**
     * Get an immutable list of products.
     *
     * @return the list of products.
     */
    public List<Product> getProducts();
}
