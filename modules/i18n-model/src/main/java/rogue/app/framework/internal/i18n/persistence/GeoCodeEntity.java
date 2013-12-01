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
import rogue.app.framework.i18n.model.GeoCode;
import rogue.app.framework.persistence.*;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

@Entity
@NamedQueries(
        {
                @NamedQuery(name = "GeoCodeEntity.findAll",
                            query = "SELECT e FROM GeoCodeEntity e ORDER BY e.zipCode"),
                @NamedQuery(name = "GeoCodeEntity.findAll.count", query = "SELECT COUNT(e) FROM GeoCodeEntity e"),
                @NamedQuery(name = "GeoCodeEntity.searchByName",
                            query = "SELECT e FROM GeoCodeEntity e WHERE e.lowerCaseName LIKE :queryString ORDER BY e.lowerCaseName, e.zipCode"),
                @NamedQuery(name = "GeoCodeEntity.searchByName.count",
                            query = "SELECT COUNT(e) FROM GeoCodeEntity e WHERE e.lowerCaseName LIKE :queryString"),
                @NamedQuery(name = "GeoCodeEntity.findByName",
                            query = "SELECT e FROM GeoCodeEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "GeoCodeEntity.findByName.count",
                            query = "SELECT COUNT(e) FROM GeoCodeEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "GeoCodeEntity.findByZipCode",
                            query = "SELECT e FROM GeoCodeEntity e WHERE e.zipCode = :queryString"),
                @NamedQuery(name = "GeoCodeEntity.findByZipCode.count",
                            query = "SELECT COUNT(e) FROM GeoCodeEntity e WHERE e.zipCode = :queryString")
        }
)
@OrderByAttribute
@Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(GeoCode.class)
public class GeoCodeEntity extends PersistentEntity<GeoCode> implements GeoCode
{

    private String country;
    private String region1;
    private String region2;
    private String region3;
    private String region4;
    private long zipCode;
    private String name;
    private float latitude;
    private float longitude;

    private String lowerCaseName;

    public GeoCodeEntity()
    {
        super();
    }

    public GeoCodeEntity(GeoCodeEntity entity)
    {
        super(entity);
    }

    @Override
    public String getCountry()
    {
        return country;
    }

    @Override
    public void setCountry(String country)
    {
        if (isMutable() && !Objects.equals(this.country, country))
        {
            this.country = country;
            markDirty();
        }
    }

    @Override
    public String getRegion1()
    {
        return region1;
    }

    @Override
    public void setRegion1(String region1)
    {
        if (isMutable() && !Objects.equals(this.region1, region1))
        {
            this.region1 = region1;
            markDirty();
        }
    }

    @Override
    public String getRegion2()
    {
        return region2;
    }

    @Override
    public void setRegion2(String region2)
    {
        if (isMutable() && !Objects.equals(this.region2, region2))
        {
            this.region2 = region2;
            markDirty();
        }
    }

    @Override
    public String getRegion3()
    {
        return region3;
    }

    @Override
    public void setRegion3(String region3)
    {
        if (isMutable() && !Objects.equals(this.region3, region3))
        {
            this.region3 = region3;
            markDirty();
        }
    }

    @Override
    public String getRegion4()
    {
        return region4;
    }

    @Override
    public void setRegion4(String region4)
    {
        if (isMutable() && !Objects.equals(this.region4, region4))
        {
            this.region4 = region4;
            markDirty();
        }
    }

    @Override
    public long getZipCode()
    {
        return zipCode;
    }

    @Override
    public void setZipCode(long zipCode)
    {
        if (isMutable() && this.zipCode != zipCode)
        {
            this.zipCode = zipCode;
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
    public float getLatitude()
    {
        return latitude;
    }

    @Override
    public void setLatitude(float latitude)
    {
        if (isMutable() && this.latitude != latitude)
        {
            this.latitude = latitude;
            markDirty();
        }
    }

    @Override
    public float getLongitude()
    {
        return longitude;
    }

    @Override
    public void setLongitude(float longitude)
    {
        if (isMutable() && this.longitude != longitude)
        {
            this.longitude = longitude;
            markDirty();
        }
    }

    ///
    /// Non-entity implementations
    ///

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
    /// Equals & hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof GeoCodeEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        GeoCodeEntity that = (GeoCodeEntity) o;

        if (Float.compare(that.latitude, latitude) != 0)
        {
            return false;
        }
        if (Float.compare(that.longitude, longitude) != 0)
        {
            return false;
        }
        if (zipCode != that.zipCode)
        {
            return false;
        }
        if (country != null ? !country.equals(that.country) : that.country != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (region1 != null ? !region1.equals(that.region1) : that.region1 != null)
        {
            return false;
        }
        if (region2 != null ? !region2.equals(that.region2) : that.region2 != null)
        {
            return false;
        }
        if (region3 != null ? !region3.equals(that.region3) : that.region3 != null)
        {
            return false;
        }
        if (region4 != null ? !region4.equals(that.region4) : that.region4 != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (region1 != null ? region1.hashCode() : 0);
        result = 31 * result + (region2 != null ? region2.hashCode() : 0);
        result = 31 * result + (region3 != null ? region3.hashCode() : 0);
        result = 31 * result + (region4 != null ? region4.hashCode() : 0);
        result = 31 * result + (int) (zipCode ^ (zipCode >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (latitude != +0.0f ? Float.floatToIntBits(latitude) : 0);
        result = 31 * result + (longitude != +0.0f ? Float.floatToIntBits(longitude) : 0);
        return result;
    }
}
