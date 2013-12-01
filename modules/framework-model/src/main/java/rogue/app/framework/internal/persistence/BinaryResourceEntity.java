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

package rogue.app.framework.internal.persistence;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.model.BinaryResource;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.*;
import rogue.app.framework.persistence.annotations.ParentRef;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Entity implementation of <code>BinaryResource</code>.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "BinaryResourceEntity.findAllNameSpaces",
                                  query = "SELECT DISTINCT e.nameSpace FROM BinaryResourceEntity e"),
                      @NamedQuery(name = "BinaryResourceEntity.findAllNameSpaces.count",
                                  query = "SELECT COUNT(DISTINCT e.nameSpace) FROM BinaryResourceEntity e"),
                      @NamedQuery(name = "BinaryResourceEntity.findResources",
                                  query = "SELECT e FROM BinaryResourceEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "BinaryResourceEntity.findResources.count",
                                  query = "SELECT COUNT(e.name) FROM BinaryResourceEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "BinaryResourceEntity.getNames",
                                  query = "SELECT e.name FROM BinaryResourceEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "BinaryResourceEntity.getNames.count",
                                  query = "SELECT COUNT(e.name) FROM BinaryResourceEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "BinaryResourceEntity.searchByName",
                                  query = "SELECT e FROM BinaryResourceEntity e WHERE e.lowerCaseName LIKE :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "BinaryResourceEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM BinaryResourceEntity e WHERE e.lowerCaseName LIKE :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "BinaryResourceEntity.findByName",
                                  query = "SELECT e FROM BinaryResourceEntity e WHERE e.lowerCaseName = :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey"),
                      @NamedQuery(name = "BinaryResourceEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM BinaryResourceEntity e WHERE e.lowerCaseName = :queryString AND e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey"),
                      @NamedQuery(name = "BinaryResourceEntity.findResourcesByType",
                                  query = "SELECT e FROM BinaryResourceEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.resourceType = :resourceType"),
                      @NamedQuery(name = "BinaryResourceEntity.findResourcesByType.count",
                                  query = "SELECT COUNT(e) FROM BinaryResourceEntity e WHERE e.nameSpace = :nameSpace AND e.appObjRefKey = :appObjRefKey AND e.resourceType = :resourceType"),

              })
@Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(BinaryResource.class)
public class BinaryResourceEntity extends PersistentEntity<BinaryResource> implements BinaryResource
{
    private static final Map<String, String> mimeMappings = new HashMap<>(4);

    static
    {
        mimeMappings.put("jpg", "image/jpeg");
        mimeMappings.put("jpeg", "image/jpeg");
        mimeMappings.put("png", "image/png");
        mimeMappings.put("gif", "image/gif");
    }


    private Blob blob;
    private String name;
    private String description;
    private String nameSpace;
    private String resourceType;

    // Attributes for association reference.
    @ParentRef
    private Key appObjRefKey;
    private String appObjRefClassName;

    private String lowerCaseName;

    @Transient
    private String fileExtension;
    @Transient
    private String mimeType;

    public BinaryResourceEntity()
    {
        super();
    }

    public BinaryResourceEntity(BinaryResourceEntity baseResource)
    {
        super(baseResource);
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
    public Blob getBlob()
    {
        return blob;
    }

    @Override
    public void setBlob(Blob blob)
    {
        if (isMutable())
        {
            this.blob = blob;
            markDirty();
        }
    }

    public String getResourceType()
    {
        return resourceType;
    }

    public void setResourceType(String resourceType)
    {
        if (isMutable() && !Objects.equals(this.resourceType, resourceType))
        {
            this.resourceType = resourceType;
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

    @Override
    protected void prePersist()
    {
        this.lowerCaseName = StringUtils.lowerCase(name);
        super.prePersist();
    }

    ///
    /// Non-entity methods
    ///

    @SuppressWarnings("unchecked")
    @Override
    public PersistentObject getAssociatedResource()
    {
        return PersistenceUtils.getAppObject(getAppObjRefClassName(), getAppObjRefKey());
    }

    @Override
    public void setAssociatedResource(PersistentObject appObject)
    {
        setAppObjRefKey(PersistenceUtils.getEntityKey(appObject));
        setAppObjRefClassName(appObject == null ? null : appObject.getClass().getName());
    }

    @Override
    public String getFileExtension()
    {
        if (name != null && fileExtension == null)
        {
            int idx = name.lastIndexOf(".");
            fileExtension = name.substring(idx + 1);
        }
        return fileExtension;
    }

    @Override
    public String getMimeType()
    {
        if (name != null && mimeType == null)
        {
            mimeType = mimeMappings.get(getFileExtension());
            if (mimeType == null)
            {
                // Generic catch all.
                mimeType = "image";
            }
        }
        return mimeType;
    }

    @Override
    public String toString()
    {
        return name;
    }

    ///
    /// Overrides for equals & hashCode
    ///

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BinaryResourceEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        BinaryResourceEntity that = (BinaryResourceEntity) o;

        if (appObjRefClassName != null ? !appObjRefClassName.equals(that.appObjRefClassName) :
            that.appObjRefClassName != null)
        {
            return false;
        }
        if (appObjRefKey != null ? !appObjRefKey.equals(that.appObjRefKey) : that.appObjRefKey != null)
        {
            return false;
        }
        if (blob != null ? !blob.equals(that.blob) : that.blob != null)
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
        if (nameSpace != null ? !nameSpace.equals(that.nameSpace) : that.nameSpace != null)
        {
            return false;
        }
        if (resourceType != null ? !resourceType.equals(that.resourceType) : that.resourceType != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (blob != null ? blob.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (nameSpace != null ? nameSpace.hashCode() : 0);
        result = 31 * result + (resourceType != null ? resourceType.hashCode() : 0);
        result = 31 * result + (appObjRefKey != null ? appObjRefKey.hashCode() : 0);
        result = 31 * result + (appObjRefClassName != null ? appObjRefClassName.hashCode() : 0);
        return result;
    }
}
