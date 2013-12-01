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
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.MemcachedScoped;
import rogue.app.framework.persistence.NestedPersistentEntity;
import rogue.app.framework.persistence.OrderByAttribute;
import rogue.app.store.catalog.model.Brand;
import rogue.app.store.catalog.model.Product;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Entity for <code>Brand</code>.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "BrandEntity.findAll",
                                  query = "SELECT e FROM BrandEntity e ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "BrandEntity.findAll.count",
                                  query = " SELECT COUNT(e) FROM BrandEntity e"),
                      @NamedQuery(name = "BrandEntity.findSubBrands",
                                  query = "SELECT e FROM BrandEntity e WHERE e.parentKey = :parentKey ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "BrandEntity.findSubBrands.count",
                                  query = "SELECT COUNT(e) FROM BrandEntity e WHERE e.parentKey = :parentKey"),
                      @NamedQuery(name = "BrandEntity.searchByName",
                                  query = "SELECT e FROM BrandEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName"),
                      @NamedQuery(name = "BrandEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM BrandEntity e WHERE e.lowerCaseName LIKE :queryString"),
                      @NamedQuery(name = "BrandEntity.findByName",
                                  query = "SELECT e FROM BrandEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "BrandEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM BrandEntity e WHERE e.lowerCaseName = :queryString")
              })
@OrderByAttribute
@rogue.app.framework.persistence.Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(Brand.class)
public class BrandEntity extends NestedPersistentEntity<Brand> implements Brand
{
    @Column(nullable = false)
    private String name;
    private String lowerCaseName;
    private String description;
    private Boolean enabled = Boolean.TRUE;

    public BrandEntity()
    {
        super();
    }

    public BrandEntity(BrandEntity baseResource)
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
        if (this.isMutable() && !Objects.equals(this.name, name))
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
        if (this.isMutable() && !Objects.equals(this.description, description))
        {
            this.description = description;
            markDirty();
        }
    }

    @Override
    public Boolean getEnabled()
    {
        return enabled;
    }

    @Override
    public void setEnabled(Boolean enabled)
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

    @Transient
    private List<Product> productsList;

    @Override
    public List<Product> getProducts()
    {
        // TODO: Get this working!
        return productsList;
    }

    @Override
    public String toString()
    {
        return getName();
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
        if (!(o instanceof BrandEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        BrandEntity that = (BrandEntity) o;

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
