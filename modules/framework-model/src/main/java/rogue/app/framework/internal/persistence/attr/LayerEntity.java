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

package rogue.app.framework.internal.persistence.attr;

import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Layer;
import rogue.app.framework.persistence.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * JPA implementation of the Layer interface.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "LayerEntity.findAll",
                                  query = "SELECT e FROM LayerEntity e ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "LayerEntity.findAll.count",
                                  query = "SELECT COUNT(e) FROM LayerEntity e ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "LayerEntity.searchByName",
                                  query = "SELECT e FROM LayerEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "LayerEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM LayerEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "LayerEntity.findByName",
                                  query = "SELECT e FROM LayerEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "LayerEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM LayerEntity e WHERE e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "LayerEntity.findByBaseRef",
                                  query = "SELECT e FROM LayerEntity e WHERE e.baseRefKey= :baseRefKey"),
                      @NamedQuery(name = "LayerEntity.findByBaseRef.count",
                                  query = "SELECT COUNT(e) FROM LayerEntity e WHERE e.baseRefKey = :baseRefKey")
              })
@OrderByAttribute
@Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(Layer.class)
public class LayerEntity extends NestedPersistentEntity<Layer> implements Layer
{
    @Column(nullable = false)
    private String name;
    private String lowerCaseName;
    private String description;
    private Key baseRefKey;
    private String baseRefClass;

    public LayerEntity()
    {
        super();
    }

    public LayerEntity(LayerEntity entity)
    {
        super(entity);
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

    public Key getBaseRefKey()
    {
        return baseRefKey;
    }

    public void setBaseRefKey(Key baseRefKey)
    {
        if (isMutable() && !Objects.equals(this.baseRefKey, baseRefKey))
        {
            this.baseRefKey = baseRefKey;
            markDirty();
        }
    }

    public String getBaseRefClass()
    {
        return baseRefClass;
    }

    public void setBaseRefClass(String baseRefClass)
    {
        if (isMutable() && !Objects.equals(this.baseRefClass, baseRefClass))
        {
            this.baseRefClass = baseRefClass;
            markDirty();
        }
    }

    ///
    /// Non-entity implementations
    ///

    @Override
    public PersistentObject<?> getBaseReference()
    {
        return PersistenceUtils.getAppObject(getBaseRefClass(), getBaseRefKey());
    }

    @Override
    public void setBaseReference(PersistentObject<?> reference)
    {
        if (isMutable())
        {
            setBaseRefClass(reference == null ? null : reference.getClass().getName());
            setBaseRefKey(PersistenceUtils.getEntityKey(reference));
        }
    }

    @Override
    protected void prePersist()
    {
        super.prePersist();
        this.lowerCaseName = StringUtils.lowerCase(getName());
    }

    ///
    /// Overrides for equals & hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof LayerEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        LayerEntity that = (LayerEntity) o;

        if (baseRefClass != null ? !baseRefClass.equals(that.baseRefClass) : that.baseRefClass != null)
        {
            return false;
        }
        if (baseRefKey != null ? !baseRefKey.equals(that.baseRefKey) : that.baseRefKey != null)
        {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null)
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
        result = 31 * result + (baseRefKey != null ? baseRefKey.hashCode() : 0);
        result = 31 * result + (baseRefClass != null ? baseRefClass.hashCode() : 0);
        return result;
    }
}
