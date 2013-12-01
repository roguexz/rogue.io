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
import rogue.app.framework.annotations.ChildType;
import rogue.app.framework.i18n.model.Country;
import rogue.app.framework.i18n.model.State;
import rogue.app.framework.patterns.None;
import rogue.app.framework.persistence.Cacheable;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.MemcachedScoped;
import rogue.app.framework.persistence.OrderByAttribute;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * Represents a country within the system.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "CountryEntity.findAll",
                            query = "SELECT e FROM CountryEntity e ORDER BY e.lowerCaseName"),
                @NamedQuery(name = "CountryEntity.findAll.count", query = "SELECT COUNT(e) FROM CountryEntity e"),
                @NamedQuery(name = "CountryEntity.searchByName",
                            query = "SELECT e FROM CountryEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName"),
                @NamedQuery(name = "CountryEntity.searchByName.count",
                            query = "SELECT COUNT(e) FROM CountryEntity e WHERE e.lowerCaseName LIKE :queryString"),
                @NamedQuery(name = "CountryEntity.findByName",
                            query = "SELECT e FROM CountryEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "CountryEntity.findByName.count",
                            query = "SELECT COUNT(e) FROM CountryEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "CountryEntity.findByCode",
                            query = "SELECT e FROM CountryEntity e WHERE e.lowerCaseCode = :queryString"),
                @NamedQuery(name = "CountryEntity.findByCode.count",
                            query = "SELECT COUNT(e) FROM CountryEntity e WHERE e.lowerCaseCode = :queryString"),
        }
)
@OrderByAttribute
@Cacheable(preferredScope = MemcachedScoped.class)
@ChildType(State.class)
@EntityImplementationFor(Country.class)
public class CountryEntity extends GeoRegionEntity<Country, None, State> implements Country
{
    @Column(nullable = false)
    private String code;
    private Integer iddCode;
    private String lowerCaseCode;

    public CountryEntity()
    {
        super();
    }

    public CountryEntity(CountryEntity baseResource)
    {
        super(baseResource);
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public void setCode(String code)
    {
        if (isMutable() && !Objects.equals(this.code, code))
        {
            this.code = code;
            markDirty();
        }
    }

    @Override
    public Integer getIDDCode()
    {
        return iddCode;
    }

    @Override
    public void setIDDCode(Integer code)
    {
        if (isMutable() && iddCode != code)
        {
            this.iddCode = code;
            markDirty();
        }
    }

    protected void prePersist()
    {
        super.prePersist();
        code = StringUtils.trimToNull(code);
        lowerCaseCode = StringUtils.lowerCase(code);
    }


    //
    // Equals & hashCode
    //

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CountryEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        CountryEntity that = (CountryEntity) o;

        if (code != null ? !code.equals(that.code) : that.code != null)
        {
            return false;
        }
        if (iddCode != null ? !iddCode.equals(that.iddCode) : that.iddCode != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (iddCode != null ? iddCode.hashCode() : 0);
        return result;
    }
}
