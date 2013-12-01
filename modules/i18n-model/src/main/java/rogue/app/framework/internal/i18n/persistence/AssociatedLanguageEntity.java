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
import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.persistence.PersistentEntity;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * Framework implementation for handling association of languages with a geographical region.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "AssociatedLanguageEntity.getByRefKey",
                                  query = "SELECT e FROM AssociatedLanguageEntity e WHERE e.appObjRefKey = :appObjRefKey"),
                      @NamedQuery(name = "AssociatedLanguageEntity.getByRefKey.count",
                                  query = "SELECT COUNT(e) FROM AssociatedLanguageEntity e WHERE e.appObjRefKey = :appObjRefKey")
              })
@EntityImplementationFor(AssociatedLanguage.class)
public class AssociatedLanguageEntity<T extends PersistentObject<T>> extends PersistentEntity<AssociatedLanguage<T>>
        implements AssociatedLanguage<T>
{
    private Key appObjRefKey;
    private String appObjRefClassName;
    private Key languageKey;

    public AssociatedLanguageEntity()
    {
        super();
    }

    public AssociatedLanguageEntity(AssociatedLanguageEntity<T> entity)
    {
        super(entity);
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

    ///
    /// Non-entity implementations
    ///

    @Override
    public T getAppObject()
    {
        return (T) PersistenceUtils.getAppObject(getAppObjRefClassName(), getAppObjRefKey());
    }

    @Override
    public void setAppObject(T appObject)
    {
        setAppObjRefKey(PersistenceUtils.getEntityKey(appObject));
        setAppObjRefClassName(appObject == null ? null : appObject.getClass().getName());
    }

    @Override
    public Language getLanguage()
    {
        return PersistenceUtils.getAppObject(Language.class, getLanguageKey());
    }

    @Override
    public void setLanguage(Language language)
    {
        setLanguageKey(PersistenceUtils.getEntityKey(language));
    }


    ///
    /// Equals and hashCode
    ///


}
