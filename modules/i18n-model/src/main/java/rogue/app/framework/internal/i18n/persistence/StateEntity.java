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
import rogue.app.framework.annotations.ParentType;
import rogue.app.framework.i18n.model.City;
import rogue.app.framework.i18n.model.Country;
import rogue.app.framework.i18n.model.State;
import rogue.app.framework.persistence.Cacheable;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.MemcachedScoped;
import rogue.app.framework.persistence.OrderByAttribute;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

@Entity
@Cacheable(preferredScope = MemcachedScoped.class)
@NamedQueries({
                      @NamedQuery(name = "StateEntity.findAll",
                                  query = "SELECT e FROM StateEntity e WHERE e.parentKey = :parentKey ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "StateEntity.findAll.count",
                                  query = "SELECT COUNT(e) FROM StateEntity e WHERE e.parentKey = :parentKey"),
                      @NamedQuery(name = "StateEntity.searchByName",
                                  query = "SELECT e FROM StateEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName ASC"),
                      @NamedQuery(name = "StateEntity.searchByName.count",
                                  query = "SELECT COUNT(e) FROM StateEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName LIKE :queryString"),
                      @NamedQuery(name = "StateEntity.findByName",
                                  query = "SELECT e FROM StateEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "StateEntity.findByName.count",
                                  query = "SELECT COUNT(e) FROM StateEntity e WHERE e.parentKey = :parentKey AND e.lowerCaseName = :queryString"),
                      @NamedQuery(name = "StateEntity.findByCode",
                                  query = "SELECT e FROM StateEntity e WHERE e.lowerCaseCode = :queryString"),
                      @NamedQuery(name = "StateEntity.findByCode.count",
                                  query = "SELECT COUNT(e) FROM StateEntity e WHERE e.lowerCaseName = :queryString")
              })
@OrderByAttribute
@ParentType(Country.class)
@ChildType(City.class)
@EntityImplementationFor(State.class)
public class StateEntity extends GeoRegionEntity<State, Country, City> implements State
{
    private String code;
    private String lowerCaseCode;

    public StateEntity()
    {
        super();
    }

    public StateEntity(StateEntity entity)
    {
        super(entity);
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
    public Country getCountry()
    {
        return (Country) getParent();
    }

    @Override
    public void setCountry(Country country)
    {
        setParent(country);
    }

    @Override
    protected void prePersist()
    {
        this.lowerCaseCode = StringUtils.lowerCase(getCode());
        super.prePersist();
    }
}
