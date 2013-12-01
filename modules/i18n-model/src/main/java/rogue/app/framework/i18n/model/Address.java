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

package rogue.app.framework.i18n.model;

import com.google.appengine.api.datastore.GeoPt;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.patterns.Association;
import rogue.app.framework.patterns.None;

/**
 * The base information required for an address.
 */
public interface Address extends GeoRegion<Address, City, None>, Association<PersistentObject>
{
    /**
     * Get the name of the contact person at this address.
     *
     * @return the name of the contact person at this address.
     */
    @Override
    String getName();

    /**
     * Set the name of the contact person at this address.
     *
     * @param name the name of the contact person at this address.
     */
    @Override
    void setName(String name);

    /**
     * Get the first line of the address.
     *
     * @return the first line of the address.
     */
    public String getAddress1();

    /**
     * Set the first line of the address.
     *
     * @param address1 the first line of the address.
     */
    public void setAddress1(String address1);

    /**
     * Get the second line of the address.
     *
     * @return the second line of the address.
     */
    public String getAddress2();

    /**
     * Set the second line of the address.
     *
     * @param address2 the second line of the address.
     */
    public void setAddress2(String address2);

    /**
     * Get the locality of the address.
     *
     * @return the locality of the address.
     */
    public String getLocality();

    /**
     * Set the locality of the address.
     *
     * @param locality the locality of the address.
     */
    public void setLocality(String locality);

    /**
     * Get the city of the address.
     *
     * @return the city of the address.
     */
    public City getCity();

    /**
     * Set the city of the address.
     *
     * @param city the city of the address.
     */
    public void setCity(City city);

    /**
     * Get the postal code of the address.
     *
     * @return the postal code of the address.
     */
    public String getPostalCode();

    /**
     * Set the postal code of the address.
     *
     * @param postalCode the postal code of the address.
     */
    public void setPostalCode(String postalCode);

    /**
     * Get the GeoPoint of this address.
     *
     * @return the GeoPoint of this address.
     */
    public GeoPt getGeoPoint();

    /**
     * Set the GeoPoint of this address.
     *
     * @param geoPoint the GeoPoint of this address.
     */
    public void setGeoPoint(GeoPt geoPoint);
}
