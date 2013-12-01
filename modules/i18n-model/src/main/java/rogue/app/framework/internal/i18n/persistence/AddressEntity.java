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

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import rogue.app.framework.annotations.ParentType;
import rogue.app.framework.i18n.model.Address;
import rogue.app.framework.i18n.model.City;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.patterns.None;
import rogue.app.framework.persistence.Cacheable;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.PersistenceUtils;

import javax.enterprise.context.SessionScoped;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.util.Objects;

/**
 * Represents a AddressEntity.
 */
@Entity
@ParentType(City.class)
@NamedQueries(
        {
                @NamedQuery(name = "AddressEntity.findAssociatedEntries",
                            query = "SELECT e FROM AddressEntity e WHERE e.appObjRefKey = :appObjRefKey"),
                @NamedQuery(name = "AddressEntity.findAssociatedEntries.count",
                            query = "SELECT COUNT(e) FROM AddressEntity e WHERE e.appObjRefKey = :appObjRefKey")
        })
@Cacheable(preferredScope = SessionScoped.class)
@EntityImplementationFor(Address.class)
public class AddressEntity extends GeoRegionEntity<Address, City, None> implements Address
{
    // The Street AddressEntity
    private String address1;
    // The colony / road details.
    private String address2;
    // The locality
    private String locality;
    // The city
    private Key cityKey;
    // The PIN code
    private String postalCode;
    // The Geo point of this address.
    private GeoPt geoPoint;

    // Attributes for association reference.
    private Key appObjRefKey;
    private String appObjRefClassName;


    public AddressEntity()
    {
        super();
    }

    public AddressEntity(AddressEntity baseResource)
    {
        super(baseResource);
    }

    public Key getCityKey()
    {
        return cityKey;
    }

    public void setCityKey(Key cityKey)
    {
        if (isMutable() && !Objects.equals(this.cityKey, cityKey))
        {
            this.cityKey = cityKey;
            markDirty();
        }
    }

    @Override
    public String getAddress1()
    {
        return address1;
    }

    @Override
    public void setAddress1(String address1)
    {
        if (isMutable() && !Objects.equals(this.address1, address1))
        {
            this.address1 = address1;
            markDirty();
        }
    }

    @Override
    public String getAddress2()
    {
        return address2;
    }

    @Override
    public void setAddress2(String address2)
    {
        if (isMutable() && !Objects.equals(this.address2, address2))
        {
            this.address2 = address2;
            markDirty();
        }
    }

    @Override
    public String getLocality()
    {
        return locality;
    }

    @Override
    public void setLocality(String locality)
    {
        if (isMutable() && !Objects.equals(this.locality, locality))
        {
            this.locality = locality;
            markDirty();
        }
    }

    @Override
    public String getPostalCode()
    {
        return postalCode;
    }

    @Override
    public void setPostalCode(String postalCode)
    {
        if (isMutable() && !Objects.equals(this.postalCode, postalCode))
        {
            this.postalCode = postalCode;
            markDirty();
        }
    }

    @Override
    public GeoPt getGeoPoint()
    {
        return geoPoint;
    }

    @Override
    public void setGeoPoint(GeoPt geoPoint)
    {
        if (isMutable() && !Objects.equals(this.geoPoint, geoPoint))
        {
            this.geoPoint = geoPoint;
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

    ///
    /// Non Entity Implementations
    ///

    @Override
    public City getCity()
    {
        return PersistenceUtils.getAppObject(City.class, getCityKey());
    }

    @Override
    public void setCity(City city)
    {
        Key key = PersistenceUtils.getEntityKey(city);
        if (isMutable() && !Objects.equals(this.cityKey, key))
        {
            setCityKey(key);
            markDirty();
        }
    }

    @Override
    public String toString()
    {
        return "Address";
    }

    @Override
    @SuppressWarnings("unchecked")
    public PersistentObject getAssociatedResource()
    {
        return (PersistentObject) PersistenceUtils.getAppObject(getAppObjRefClassName(), getAppObjRefKey());
    }

    @Override
    public void setAssociatedResource(PersistentObject appObject)
    {
        setAppObjRefKey(PersistenceUtils.getEntityKey(appObject));
        setAppObjRefClassName(appObject == null ? null : appObject.getClass().getName());
    }


    ///
    /// Override of Equals and hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AddressEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        AddressEntity that = (AddressEntity) o;

        if (address1 != null ? !address1.equals(that.address1) : that.address1 != null)
        {
            return false;
        }
        if (address2 != null ? !address2.equals(that.address2) : that.address2 != null)
        {
            return false;
        }
        if (cityKey != null ? !cityKey.equals(that.cityKey) : that.cityKey != null)
        {
            return false;
        }
        if (geoPoint != null ? !geoPoint.equals(that.geoPoint) : that.geoPoint != null)
        {
            return false;
        }
        if (locality != null ? !locality.equals(that.locality) : that.locality != null)
        {
            return false;
        }
        if (postalCode != null ? !postalCode.equals(that.postalCode) : that.postalCode != null)
        {
            return false;
        }
        if (appObjRefClassName != null ? !appObjRefClassName.equals(that.appObjRefClassName) :
            that.appObjRefClassName != null)
        {
            return false;
        }
        if (appObjRefKey != null ? !appObjRefKey.equals(that.appObjRefKey) : that.appObjRefKey != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (locality != null ? locality.hashCode() : 0);
        result = 31 * result + (cityKey != null ? cityKey.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        result = 31 * result + (geoPoint != null ? geoPoint.hashCode() : 0);
        result = 31 * result + (appObjRefKey != null ? appObjRefKey.hashCode() : 0);
        result = 31 * result + (appObjRefClassName != null ? appObjRefClassName.hashCode() : 0);
        return result;
    }
}
