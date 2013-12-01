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
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Layer;
import rogue.app.framework.persistence.*;
import rogue.app.framework.persistence.annotations.ParentRef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * JPA implementation of the Attribute interface.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "AttributeEntity.findAllNameSpaces",
                                  query = "SELECT DISTINCT e.nameSpace FROM AttributeEntity e"),
                      @NamedQuery(name = "AttributeEntity.findAllNameSpaces.count",
                                  query = "SELECT COUNT(DISTINCT e.nameSpace) FROM AttributeEntity e"),
                      @NamedQuery(name = "AttributeEntity.findAttributes",
                                  query = "SELECT e FROM AttributeEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.findAttributes.count",
                                  query = "SELECT COUNT(e.name) FROM AttributeEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.findAttributes.alLayers",
                                  query = "SELECT e FROM AttributeEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.findAttributes.allLayers.count",
                                  query = "SELECT COUNT(e.name) FROM AttributeEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.getNames",
                                  query = "SELECT e.name FROM AttributeEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.getNames.count",
                                  query = "SELECT COUNT(e.name) FROM AttributeEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.searchByName",
                                  query = "SELECT e FROM AttributeEntity e WHERE e.lowerCaseName LIKE :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM AttributeEntity e WHERE e.lowerCaseName LIKE :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "AttributeEntity.findByName",
                                  query = "SELECT e FROM AttributeEntity e WHERE e.lowerCaseName = :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey"),
                      @NamedQuery(name = "AttributeEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM AttributeEntity e WHERE e.lowerCaseName = :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.layerKey = :layerKey"),
                      @NamedQuery(name = "AttributeEntity.findByName.allLayers",
                                  query = "SELECT e FROM AttributeEntity e WHERE e.lowerCaseName = :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey"),
                      @NamedQuery(name = "AttributeEntity.findByName.allLayers.count",
                                  query = "SELECT COUNT(e) FROM AttributeEntity e WHERE e.lowerCaseName = :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey"),
              })
@OrderByAttribute
@Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(Attribute.class)
public class AttributeEntity extends PersistentEntity<Attribute> implements Attribute
{
    @Column(nullable = false)
    private String nameSpace;
    @Column(nullable = false)
    private String name;
    private String lowerCaseName;
    private String description;
    private String value;

    @ParentRef
    private Key appObjRefKey;
    private String appObjRefClassName;
    private Key layerKey;

    public AttributeEntity()
    {
    }

    public AttributeEntity(AttributeEntity entity)
    {
        super(entity);
    }

    @Override
    public String getNameSpace()
    {
        return nameSpace;
    }

    @Override
    public void setNameSpace(String nameSpace)
    {
        if (isMutable() && !Objects.equals(this.nameSpace, nameSpace))
        {
            this.nameSpace = nameSpace;
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
    public String getValue()
    {
        return value;
    }

    @Override
    public void setValue(String value)
    {
        if (isMutable() && !Objects.equals(this.value, value))
        {
            this.value = value;
            markDirty();
        }
    }

    public Key getAppObjRefKey()
    {
        return appObjRefKey;
    }

    public void setAppObjRefKey(Key appObjRefKey)
    {
        if (isMutable() && !Objects.equals(this.appObjRefKey, appObjRefKey))
        {
            this.appObjRefKey = appObjRefKey;
            markDirty();
        }
    }

    public String getAppObjRefClassName()
    {
        return appObjRefClassName;
    }

    public void setAppObjRefClassName(String appObjRefClassName)
    {
        if (isMutable() && !Objects.equals(this.appObjRefClassName, appObjRefClassName))
        {
            this.appObjRefClassName = appObjRefClassName;
            markDirty();
        }
    }

    public Key getLayerKey()
    {
        return layerKey;
    }

    public void setLayerKey(Key layerKey)
    {
        if (isMutable() && !Objects.equals(this.layerKey, layerKey))
        {
            this.layerKey = layerKey;
            markDirty();
        }
    }

    ///
    /// Non-entity methods
    ///

    @Override
    protected void prePersist()
    {
        super.prePersist();
        this.lowerCaseName = StringUtils.lowerCase(getName());
    }

    @Override
    public PersistentObject<?> getAppObject()
    {
        return PersistenceUtils.getAppObject(getAppObjRefClassName(), getAppObjRefKey());
    }

    @Override
    public void setAppObject(PersistentObject<?> appObject)
    {
        setAppObjRefKey(PersistenceUtils.getEntityKey(appObject));
        setAppObjRefClassName(appObject == null ? null : appObject.getClass().getName());
    }

    @Override
    public Layer getCustomizationLayer()
    {
        return PersistenceUtils.getAppObject(Layer.class, getLayerKey());
    }

    @Override
    public void setCustomizationLayer(Layer layer)
    {
        setLayerKey(PersistenceUtils.getEntityKey(layer));
    }

    @Override
    public String toString()
    {
        return getName();
    }

    ///
    /// Overrides for equals and hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AttributeEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        AttributeEntity that = (AttributeEntity) o;

        if (appObjRefClassName != null ? !appObjRefClassName.equals(that.appObjRefClassName) :
            that.appObjRefClassName != null)
        {
            return false;
        }
        if (appObjRefKey != null ? !appObjRefKey.equals(that.appObjRefKey) : that.appObjRefKey != null)
        {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null)
        {
            return false;
        }
        if (layerKey != null ? !layerKey.equals(that.layerKey) : that.layerKey != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (nameSpace != null ? !nameSpace.equals(that.nameSpace) : that.nameSpace != null)
        {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (nameSpace != null ? nameSpace.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (appObjRefKey != null ? appObjRefKey.hashCode() : 0);
        result = 31 * result + (appObjRefClassName != null ? appObjRefClassName.hashCode() : 0);
        result = 31 * result + (layerKey != null ? layerKey.hashCode() : 0);
        return result;
    }
}
