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
import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.persistence.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * Entity implementation of the <code>Language</code> interface.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "LanguageEntity.findAll",
                            query = "SELECT e FROM LanguageEntity e ORDER BY e.lowerCaseName"),
                @NamedQuery(name = "LanguageEntity.findAll.count", query = "SELECT COUNT(e) FROM LanguageEntity e"),
                @NamedQuery(name = "LanguageEntity.searchByName",
                            query = "SELECT e FROM LanguageEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName"),
                @NamedQuery(name = "LanguageEntity.searchByName.count",
                            query = "SELECT COUNT(e) FROM LanguageEntity e WHERE e.lowerCaseName LIKE :queryString"),
                @NamedQuery(name = "LanguageEntity.findByName",
                            query = "SELECT e FROM LanguageEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "LanguageEntity.findByName.count",
                            query = "SELECT COUNT(e) FROM LanguageEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "LanguageEntity.findByCode",
                            query = "SELECT e FROM LanguageEntity e WHERE e.code2 = :queryString"),
                @NamedQuery(name = "LanguageEntity.findByCode.count",
                            query = "SELECT COUNT(e) FROM LanguageEntity e WHERE e.code2 = :queryString"),
                @NamedQuery(name = "LanguageEntity.findByCode3",
                            query = "SELECT e FROM LanguageEntity e WHERE e.code3 = :queryString"),
                @NamedQuery(name = "LanguageEntity.findByCode3.count",
                            query = "SELECT COUNT(e) FROM LanguageEntity e WHERE e.code3 = :queryString")
        }
)
@OrderByAttribute
@Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(Language.class)
public class LanguageEntity extends PersistentEntity<Language> implements Language
{
    @Column(nullable = false)
    private String name;
    private String code2;
    private String code3;
    private String lowerCaseName;

    public LanguageEntity()
    {
        super();
    }

    public LanguageEntity(LanguageEntity baseResource)
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
    public String getCode()
    {
        return code2;
    }

    @Override
    public void setCode(String code)
    {
        if (isMutable() && !Objects.equals(this.code2, code))
        {
            this.code2 = code;
            markDirty();
        }
    }

    @Override
    public String getCode3()
    {
        return code3;
    }

    @Override
    public void setCode3(String code3)
    {
        if (isMutable() && !Objects.equals(this.code3, code3))
        {
            this.code3 = code3;
            markDirty();
        }
    }

    protected void prePersist()
    {
        super.prePersist();
        lowerCaseName = StringUtils.lowerCase(name);
    }


    @Override
    public String toString()
    {
        return name;
    }

    ///
    /// Equals & hashcode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof LanguageEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        LanguageEntity that = (LanguageEntity) o;

        if (code2 != null ? !code2.equals(that.code2) : that.code2 != null)
        {
            return false;
        }
        if (code3 != null ? !code3.equals(that.code3) : that.code3 != null)
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
        result = 31 * result + (code2 != null ? code2.hashCode() : 0);
        result = 31 * result + (code3 != null ? code3.hashCode() : 0);
        return result;
    }
}
