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
import rogue.app.framework.annotations.ParentType;
import rogue.app.framework.i18n.model.City;
import rogue.app.framework.i18n.model.Country;
import rogue.app.framework.i18n.model.State;
import rogue.app.framework.patterns.None;
import rogue.app.framework.persistence.Cacheable;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.MemcachedScoped;
import rogue.app.framework.persistence.OrderByAttribute;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * Represents a City within the system.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "CityEntity.findAll",
                            query = "SELECT e FROM CityEntity e WHERE e.parentKey = :parentKey ORDER BY e.lowerCaseName ASC"),
                @NamedQuery(name = "CityEntity.findAll.count",
                            query = "SELECT COUNT(e) FROM CityEntity e WHERE e.parentKey = :parentKey"),
                @NamedQuery(name = "CityEntity.searchByName",
                            query = "SELECT e FROM CityEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName ASC"),
                @NamedQuery(name = "CityEntity.searchByName.count",
                            query = "SELECT COUNT(e) FROM CityEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName LIKE :queryString"),
                @NamedQuery(name = "CityEntity.findByName",
                            query = "SELECT e FROM CityEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName = :queryString"),
                @NamedQuery(name = "CityEntity.findByName.count",
                            query = "SELECT COUNT(e) FROM CityEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName = :queryString"),
//                @NamedQuery(name = "CityEntity.findByCountry",
//                            query = "SELECT e FROM CityEntity e WHERE e.countryKey = :queryString ORDER BY e.lowerCaseName ASC"),
//                @NamedQuery(name = "CityEntity.findByCountry.count",
//                            query = "SELECT COUNT(e) FROM CityEntity e WHERE e.countryKey = :queryString"),
                @NamedQuery(name = "CityEntity.findByAltName",
                            query = "SELECT e FROM CityEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseAltName = :queryString"),
                @NamedQuery(name = "CityEntity.findByAltName.count",
                            query = "SELECT COUNT(e) FROM CityEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseAltName = :queryString")
        }
)
@OrderByAttribute
@Cacheable(preferredScope = MemcachedScoped.class)
@ParentType(State.class)
@EntityImplementationFor(City.class)
public class CityEntity extends GeoRegionEntity<City, State, None> implements City
{
    private String alternateName;
    private String lowerCaseAltName;

    public CityEntity()
    {
        super();
    }

    public CityEntity(CityEntity baseResource)
    {
        super(baseResource);
    }

    @Override
    public String getAlternateName()
    {
        return alternateName;
    }

    @Override
    public void setAlternateName(String alternateName)
    {
        if (isMutable() && !Objects.equals(this.alternateName, alternateName))
        {
            this.alternateName = alternateName;
            markDirty();
        }
    }

    ///
    /// Non-entity implementations
    ///


    @Override
    public State getState()
    {
        return (State) getParent();
    }

    @Override
    public void setState(State state)
    {
        setParent(state);
    }

    @Override
    public Country getCountry()
    {
        State state = getState();
        return state != null ? state.getCountry() : null;
    }


    protected void prePersist()
    {
        super.prePersist();
        lowerCaseAltName = StringUtils.lowerCase(alternateName);
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
        if (!(o instanceof CityEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        CityEntity that = (CityEntity) o;

        if (alternateName != null ? !alternateName.equals(that.alternateName) : that.alternateName != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (alternateName != null ? alternateName.hashCode() : 0);
        return result;
    }
}
