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

import com.google.appengine.api.search.Field;
import rogue.app.framework.model.NamedResource;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.search.SearchField;
import rogue.app.framework.search.SearchIndex;

import java.util.List;

/**
 * Represents a product within the system.
 */
@SearchIndex(name = "Products")
public interface Product extends PersistentObject<Product>, NamedResource, Attributes
{
    /**
     * Get the category to which this product belongs to.
     *
     * @return the category to which this product belongs to.
     */
    public ProductCategory getCategory();

    /**
     * Set the category to which this product belongs to.
     *
     * @param category the category to which this product belongs to.
     */
    public void setCategory(ProductCategory category);

    /**
     * Get the brand to which this product belongs to.
     *
     * @return the brand to which this product belongs to.
     */
    public Brand getBrand();

    /**
     * Set the brand to which this product belongs to.
     *
     * @param brand the brand to which this product belongs to.
     */
    public void setBrand(Brand brand);

    /**
     * Get whether this product has been enabled or not.
     *
     * @return true if this brand is enabled, false otherwise.
     */
    @SearchField(name = "enabled", type = Field.FieldType.TEXT)
    public boolean isEnabled();

    /**
     * Set whether this product has been enabled or not.
     *
     * @param enabled true if this brand is enabled, false otherwise.
     */
    public void setEnabled(boolean enabled);


    /**
     * Get a list of tags associated with this product.
     *
     * @return a list of tags associated with this product.
     */
    public List<String> getTags();

    /**
     * Set a list of tags associated with this product.
     *
     * @param tags a list of tags associated with this product.
     */
    public void setTags(List<String> tags);

    /**
     * Get the price associated with this product.
     *
     * @return the price associated with this product.
     */
    public float getPrice();

    /**
     * Set the price associated with this product.
     *
     * @param value the price associated with this product.
     */
    public void setPrice(float value);

    /**
     * Get the minimum quantity that needs to be ordered. The default is 1.
     *
     * @return the minimum quantity that needs to be ordered. The default is 1.
     */
    public int getMinimumQuantity();

    /**
     * Set the minimum quantity that needs to be ordered.
     *
     * @param minimumQuantity the minimum quantity that needs to be ordered.
     */
    public void setMinimumQuantity(int minimumQuantity);

    /**
     * Get the lead time in days for procuring this product. The default is 0.
     *
     * @return the lead time in days for procuring this product.
     */
    public int getLeadTime();

    /**
     * Set the lead time in days for procuring this product.
     *
     * @param leadTime the lead time in days for procuring this product.
     */
    public void setLeadTime(int leadTime);

}
