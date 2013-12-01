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

package rogue.app.framework.internal.i18n.persistence;

import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.annotations.ChildType;
import rogue.app.framework.annotations.ParentType;
import rogue.app.framework.i18n.model.GeoRegion;
import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.internal.util.ClassUtils;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.persistence.PersistentEntity;
import rogue.app.framework.persistence.annotations.ParentRef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.*;

/**
 * The base class for GeoRegion implementations.
 */
@Entity
@MappedSuperclass
public abstract class GeoRegionEntity<T extends GeoRegion<T, P, C>, P, C> extends PersistentEntity<T>
        implements GeoRegion<T, P, C>, Attributes
{
    private static final String GET_CHILDREN_QUERY =
            "SELECT e FROM %s e WHERE e.parentKey = :parentKey ORDER BY e.lowerCaseName ASC";
    private static final String GET_CHILD_COUNT_QUERY = "SELECT COUNT(e) FROM %s e WHERE e.parentKey = :parentKey";

    @Column(nullable = false)
    private String name;
    private Key languageKey;
    @ParentRef
    private Key parentKey;
    private boolean enabled;
    private String lowerCaseName;

    @Transient
    private Class<? extends PersistentObject> parentClass;
    @Transient
    private Class<? extends PersistentEntity> childClass;

    public GeoRegionEntity()
    {
        super();
        initHierarchy();
    }

    public GeoRegionEntity(GeoRegionEntity<T, P, C> entity)
    {
        super(entity);
        initHierarchy();
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

    public Key getParentKey()
    {
        return parentKey;
    }

    public void setParentKey(Key parentKey)
    {
        if (isMutable() && !Objects.equals(this.parentKey, parentKey))
        {
            this.parentKey = parentKey;
            markDirty();
        }
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        if (isMutable() && this.enabled != enabled)
        {
            this.enabled = enabled;
            markDirty();
        }
    }

    public Key getLanguageKey()
    {
        return languageKey;
    }

    public void setLanguageKey(Key languageKey)
    {
        if (isMutable() && !Objects.equals(this.languageKey, languageKey))
        {
            this.languageKey = languageKey;
            markDirty();
        }
    }

    @Override
    public List<? extends Language> getLanguages()
    {
        throw new UnsupportedOperationException("Operation not implemented.");
    }

    @Override
    public void addLanguage(Language language, boolean markAsDefault)
    {
        throw new UnsupportedOperationException("Operation not implemented.");
    }

    @Override
    public void removeLanguage(Language language)
    {
        throw new UnsupportedOperationException("Operation not implemented.");
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

    @Override
    public Language getLanguage()
    {
        return PersistenceUtils.getAppObject(Language.class, getLanguageKey());
    }

    @Override
    public void setLanguage(Language language)
    {
        setLanguageKey(PersistenceUtils.getEntityKey(language));
        addLanguage(language, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public P getParent()
    {
        return (P) PersistenceUtils.getAppObject(parentClass, getParentKey());
    }

    @Override
    public void setParent(P parent)
    {
        if (parent == null || (parentClass != null && parentClass.isAssignableFrom(parent.getClass())))
        {
            if (parent instanceof PersistentEntity)
            {
                setParentKey(PersistenceUtils.getEntityKey((PersistentEntity) parent));
            }
            else
            {
                throw new AppRuntimeException(
                        getClass().getSimpleName() + " does not support parent objects that are not instances of " +
                                PersistentEntity.class.getName());
            }
        }
        else
        {
            throw new AppRuntimeException(
                    getClass().getSimpleName() + " does not support setting of a parent object of type: " +
                            parent.getClass().getName());
        }
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public List<C> getChildren()
    {
        List<C> children = null;
        if (childClass != null && getId() != null)
        {
            String query = String.format(GET_CHILDREN_QUERY, childClass.getSimpleName());
            Map<String, Object> params = new HashMap<>(1);
            params.put("parentKey", getId());
            children = JpaController.getController(childClass).executeQueryListResult(query, 0, -1, params);
        }

        if (children == null)
        {
            children = Collections.emptyList();
        }
        return children;
    }

    @Override
    public int getChildCount()
    {
        Long count = null;
        if (childClass != null && getId() != null)
        {
            String query = String.format(GET_CHILD_COUNT_QUERY, childClass.getSimpleName());
            Map<String, Object> params = new HashMap<>(1);
            params.put("parentKey", getId());
            count = (Long) JpaController.getController(childClass).executeQuerySingleResult(query, params);
        }

        if (count == null)
        {
            count = 0L;
        }
        return count.intValue();
    }


    ///
    /// Equals & hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GeoRegionEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        GeoRegionEntity that = (GeoRegionEntity) o;

        if (enabled != that.enabled)
        {
            return false;
        }
        if (languageKey != null ? !languageKey.equals(that.languageKey) : that.languageKey != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (parentKey != null ? !parentKey.equals(that.parentKey) : that.parentKey != null)
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
        result = 31 * result + (languageKey != null ? languageKey.hashCode() : 0);
        result = 31 * result + (parentKey != null ? parentKey.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    private void initHierarchy()
    {
        ParentType p = ClassUtils.getAnnotation(getClass(), ParentType.class);
        if (p != null && p.value() != null)
        {
            parentClass = (Class<? extends PersistentObject>) p.value();
        }

        ChildType c = ClassUtils.getAnnotation(getClass(), ChildType.class);
        if (c != null && c.value() != null)
        {
            childClass = JpaController.getController((Class<? extends PersistentObject>) c.value()).getEntityClass();
        }
    }
}
