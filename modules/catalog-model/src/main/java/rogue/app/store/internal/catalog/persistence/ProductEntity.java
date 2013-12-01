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

import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.persistence.*;
import rogue.app.store.catalog.model.Brand;
import rogue.app.store.catalog.model.Product;
import rogue.app.store.catalog.model.ProductCategory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;
import java.util.Objects;

/**
 * Represents a ProductEntity.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "ProductEntity.findAll",
                                  query = "SELECT e FROM ProductEntity e ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductEntity.findAll.count",
                                  query = "SELECT COUNT(e) FROM ProductEntity e"),
                      @NamedQuery(name = "ProductEntity.searchByName",
                                  query = "SELECT e FROM ProductEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM ProductEntity e WHERE e.lowerCaseName LIKE :queryString"),
                      @NamedQuery(name = "ProductEntity.findByName",
                                  query = "SELECT e FROM ProductEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "ProductEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM ProductEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "ProductEntity.findById",
                                  query = "SELECT e from ProductEntity e WHERE e.id in :includeList"),
                      @NamedQuery(name = "ProductEntity.findByCategory",
                                  query = "SELECT e FROM ProductEntity e WHERE e.productCategoryKey = :queryString ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductEntity.findByCategory.count",
                                  query = "SELECT COUNT(e) FROM ProductEntity e WHERE e.productCategoryKey = :queryString"),
                      @NamedQuery(name = "ProductEntity.site.findByEnabled",
                                  query = "SELECT e FROM ProductEntity e WHERE e.enabled=:enabled ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductEntity.site.findByEnabled.count",
                                  query = "SELECT COUNT(e) FROM ProductEntity e WHERE e.enabled=:enabled"),
                      @NamedQuery(name = "ProductEntity.site.findByCategories",
                                  query = "SELECT e FROM ProductEntity e WHERE e.productCategoryKey IN (:keyList) AND e.enabled=:enabled ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "ProductEntity.site.findByCategories.count",
                                  query = "SELECT COUNT(e) FROM ProductEntity e WHERE e.productCategoryKey IN (:keyList) AND e.enabled=:enabled ORDER BY e.lowerCaseName"),

              })
@OrderByAttribute
@rogue.app.framework.persistence.Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(Product.class)
public class ProductEntity extends PersistentEntity<Product> implements Product
{
    @Column(nullable = false)
    private Key brandKey;
    @Column(nullable = false)
    private String name;
    private String lowerCaseName;
    private String description;
    private Boolean enabled;
    private Key productCategoryKey;
    private List<String> tags;

    private float price;
    private int minimumQuantity;
    private int leadTime;

    public ProductEntity()
    {
        super();
    }

    public ProductEntity(ProductEntity baseResource)
    {
        super(baseResource);
    }

    public Key getBrandKey()
    {
        return brandKey;
    }

    public void setBrandKey(Key brandKey)
    {
        if (isMutable() && !Objects.equals(this.brandKey, brandKey))
        {
            this.brandKey = brandKey;
            markDirty();
        }
    }

    public Key getProductCategoryKey()
    {
        return productCategoryKey;
    }

    public void setProductCategoryKey(Key productCategoryKey)
    {
        if (isMutable() && !Objects.equals(this.productCategoryKey, productCategoryKey))
        {
            this.productCategoryKey = productCategoryKey;
            markDirty();
        }
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
        if (isMutable() && !Objects.equals(this.enabled, enabled))
        {
            this.enabled = enabled;
            markDirty();
        }
    }

    @Override
    public List<String> getTags()
    {
        return null; //tags;
    }

    @Override
    public void setTags(List<String> tags)
    {
        //this.tags = tags;
    }

    @Override
    public float getPrice()
    {
        return price;
    }

    @Override
    public void setPrice(float price)
    {
        if (isMutable() && this.price != price)
        {
            this.price = price;
            markDirty();
        }
    }

    @Override
    public int getMinimumQuantity()
    {
        if (minimumQuantity == 0)
        {
            minimumQuantity = 1;
        }

        return minimumQuantity;
    }

    @Override
    public void setMinimumQuantity(int minimumQuantity)
    {
        if (minimumQuantity == 0)
        {
            minimumQuantity = 1; // You cannot set min-qty to 0
        }

        if (isMutable() && this.minimumQuantity != minimumQuantity)
        {
            this.minimumQuantity = minimumQuantity;
            markDirty();
        }
    }

    @Override
    public int getLeadTime()
    {
        return leadTime;
    }

    @Override
    public void setLeadTime(int leadTime)
    {
        if (isMutable() && this.leadTime != leadTime)
        {
            this.leadTime = leadTime;
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
    public Brand getBrand()
    {
        return PersistenceUtils.getAppObject(Brand.class, getBrandKey());
    }

    @Override
    public void setBrand(Brand brand)
    {
        Key key = PersistenceUtils.getEntityKey(brand);
        if (isMutable() && !Objects.equals(this.brandKey, key))
        {
            setBrandKey(key);
            markDirty();
        }
    }

    @Override
    public ProductCategory getCategory()
    {
        return PersistenceUtils.getAppObject(ProductCategory.class, getProductCategoryKey());
    }

    @Override
    public void setCategory(ProductCategory category)
    {
        setProductCategoryKey(PersistenceUtils.getEntityKey(category));
    }

    @Override
    public String toString()
    {
        return name;
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
        if (!(o instanceof ProductEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        ProductEntity that = (ProductEntity) o;

        if (leadTime != that.leadTime)
        {
            return false;
        }
        if (minimumQuantity != that.minimumQuantity)
        {
            return false;
        }
        if (Float.compare(that.price, price) != 0)
        {
            return false;
        }
        if (brandKey != null ? !brandKey.equals(that.brandKey) : that.brandKey != null)
        {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (enabled != null ? !enabled.equals(that.enabled) : that.enabled != null)
        {
            return false;
        }
        if (lowerCaseName != null ? !lowerCaseName.equals(that.lowerCaseName) : that.lowerCaseName != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (productCategoryKey != null ? !productCategoryKey.equals(that.productCategoryKey) :
            that.productCategoryKey != null)
        {
            return false;
        }
        if (tags != null ? !tags.equals(that.tags) : that.tags != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (brandKey != null ? brandKey.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (lowerCaseName != null ? lowerCaseName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);
        result = 31 * result + (productCategoryKey != null ? productCategoryKey.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (price != +0.0f ? Float.floatToIntBits(price) : 0);
        result = 31 * result + minimumQuantity;
        result = 31 * result + leadTime;
        return result;
    }
}
