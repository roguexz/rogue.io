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

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.i18n.model.AppLocale;
import rogue.app.framework.i18n.model.TranslationBundle;
import rogue.app.framework.internal.persistence.attr.AttributesManager;
import rogue.app.framework.model.attr.CustomizedAttributes;
import rogue.app.framework.model.attr.Layer;
import rogue.app.framework.model.attr.LayerUtils;
import rogue.app.framework.persistence.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.List;
import java.util.Objects;

/**
 * JPA implementation of the {@link rogue.app.framework.i18n.model.TranslationBundle} interface.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "TranslationBundleEntity.findAll",
                                  query = "SELECT e FROM TranslationBundleEntity e ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "TranslationBundleEntity.findAll.count",
                                  query = "SELECT COUNT(e) FROM TranslationBundleEntity e ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "TranslationBundleEntity.searchByName",
                                  query = "SELECT e FROM TranslationBundleEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "TranslationBundleEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM TranslationBundleEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "TranslationBundleEntity.findByName",
                                  query = "SELECT e FROM TranslationBundleEntity e WHERE e.lowerCaseName = :queryString ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "TranslationBundleEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM TranslationBundleEntity e WHERE e.lowerCaseName = :queryString ORDER BY e.lowerCaseName ASC")
              })
@Cacheable(preferredScope = MemcachedScoped.class)
@OrderByAttribute
@EntityImplementationFor(TranslationBundle.class)
public class TranslationBundleEntity extends PersistentEntity<TranslationBundle>
        implements TranslationBundle, CustomizedAttributes
{
    @Column(nullable = false)
    private String name;
    private String lowerCaseName;
    private String description;

    public TranslationBundleEntity()
    {
        super();
    }

    public TranslationBundleEntity(TranslationBundleEntity entity)
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

    @Override
    protected void prePersist()
    {
        super.prePersist();
        this.lowerCaseName = StringUtils.lowerCase(getName());
    }

    ///
    /// Non-entity methods
    ///

    @Override
    public List<String> getKeys()
    {
        return getAttributeNames();
    }

    @Override
    public int getKeyCount()
    {
        AttributesManager delegate = ((AttributesManager) getCustomizedAttributesDelegate());
        return delegate.getAttributeCount();
    }

    @Override
    public boolean containsKey(String key)
    {
        return isAttributeDefined(key);
    }

    @Override
    public String getValue(String key)
    {
        return getAttribute(key);
    }

    @Override
    public String getValue(String key, AppLocale locale)
    {
        Layer l = LayerUtils.getLayerForObject(locale);
        return isAttributeCustomized(key, l) ? getAttribute(key, l) : getAttribute(key);
    }

    @Override
    public void addEntry(String key, String value, String description)
    {
        AttributesManager delegate = ((AttributesManager) getCustomizedAttributesDelegate());
        delegate.setAttribute(key, value, description, null);
    }

    @Override
    public void removeEntry(String key)
    {
        removeAttribute(key);
    }

    @Override
    public void addEntry(String key, String value, AppLocale locale)
    {
        AttributesManager delegate = ((AttributesManager) getCustomizedAttributesDelegate());
        delegate.setAttribute(key, value, null, LayerUtils.getLayerForObject(locale));
    }

    @Override
    public void removeEntry(String key, AppLocale locale)
    {
        removeAttribute(name, LayerUtils.getLayerForObject(locale));
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
        if (!(o instanceof TranslationBundleEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        TranslationBundleEntity that = (TranslationBundleEntity) o;

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
        return result;
    }
}
