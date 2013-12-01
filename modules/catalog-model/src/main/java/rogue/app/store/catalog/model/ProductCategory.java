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
 * A product category.
 */
public interface ProductCategory
        extends PersistentObject<ProductCategory>, NestedResource<ProductCategory>, NamedResource
{
    /**
     * Identify if the category has been enabled or not.
     *
     * @return true if the category is enabled, false otherwise.
     */
    public boolean isEnabled();

    /**
     * Specify if the category should be enabled or not.
     *
     * @param enabled true if the category is enabled, false otherwise.
     */
    public void setEnabled(boolean enabled);

    /**
     * Get a list of products that belong to this category.
     *
     * @return a list of products belonging to this category.
     */
    public List<Product> getProducts();

}
