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

package rogue.app.store.internal.catalog.persistence;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.persistence.*;
import rogue.app.store.catalog.model.Product;
import rogue.app.store.catalog.model.ProductCategory;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a ProductEntity category - Beer / Whiskey / Vodka / etc.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "ProductCategoryEntity.findAll",
                                  query = "SELECT e FROM ProductCategoryEntity e ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductCategoryEntity.findAll.count",
                                  query = "SELECT COUNT(e) FROM ProductCategoryEntity e"),
                      @NamedQuery(name = "ProductCategoryEntity.searchByName",
                                  query = "SELECT e FROM ProductCategoryEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductCategoryEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM ProductCategoryEntity e WHERE e.lowerCaseName LIKE :queryString"),
                      @NamedQuery(name = "ProductCategoryEntity.findByName",
                                  query = "SELECT e FROM ProductCategoryEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "ProductCategoryEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM ProductCategoryEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "ProductCategoryEntity.findEnabledCategories",
                                  query = "SELECT e FROM ProductCategoryEntity e WHERE e.parentKey = :queryString AND e.enabled = true ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductCategoryEntity.findEnabledCategories.count",
                                  query = "SELECT COUNT(e) FROM ProductCategoryEntity e WHERE e.parentKey = :queryString AND e.enabled = true")
              })
@OrderByAttribute
@rogue.app.framework.persistence.Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(ProductCategory.class)
public class ProductCategoryEntity extends NestedPersistentEntity<ProductCategory> implements ProductCategory
{
    private String name;
    private String description;
    private Boolean enabled;
    private String lowerCaseName;

    public ProductCategoryEntity()
    {
        super();
    }

    public ProductCategoryEntity(ProductCategoryEntity baseResource)
    {
        super(baseResource);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        if (isMutable() && !Objects.equals(this.name, name))
        {
            this.name = name;
            markDirty();
        }
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        if (isMutable() && !Objects.equals(this.description, description))
        {
            this.description = description;
            markDirty();
        }
    }

    @Override
    public boolean isEnabled()
    {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        if (this.isMutable() && !Objects.equals(this.enabled, enabled))
        {
            this.enabled = enabled;
            markDirty();
        }
    }

    protected void prePersist()
    {
        super.prePersist();
        lowerCaseName = StringUtils.lowerCase(name);
    }

    ///
    /// Non-entity implementations
    ///

    @Override
    public List<Product> getProducts()
    {
        final Map<String, Object> map = new HashMap<>(2);
        map.put("queryString", getId());
        map.put("enabled", true);
        return JpaController.getController(Product.class)
                            .executeNamedQueryListResult("ProductEntity.site.findByCategory", 0, -1, map);
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    ///
    /// Override of Equals & hashCode
    ///


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ProductCategoryEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        ProductCategoryEntity that = (ProductCategoryEntity) o;

        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (enabled != null ? !enabled.equals(that.enabled) : that.enabled != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);
        return result;
    }
}
