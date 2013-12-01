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

import rogue.app.framework.model.PersistentObject;

/**
 * Data structure for GeoCode data being stored in the system. The primary intention for this data is to be
 * able to lookup LatLong coordinates based on the zip code.
 */
public interface GeoCode extends PersistentObject<GeoCode>
{
    /**
     * Get the 2 character country code.
     *
     * @return the 2 character country code.
     */
    public String getCountry();

    /**
     * Set the 2 character country code.
     *
     * @param country the 2 character country code.
     */
    public void setCountry(String country);

    /**
     * Get the Region1 level information about this location. This is typically the state or the union territory.
     *
     * @return the Region1 level information.
     */
    public String getRegion1();

    /**
     * Set the Region1 level information. This is typically the state or the union territory.
     *
     * @param region1 the Region1 level information.
     */
    public void setRegion1(String region1);

    /**
     * Get the Region2 level information. This is typically the Divisional information about the location.
     *
     * @return the Region2 level information.
     */
    public String getRegion2();

    /**
     * Set the Region2 level information. This is typically the Divisional information about the location.
     *
     * @param region2 the Region2 level information.
     */
    public void setRegion2(String region2);

    /**
     * Get the Region3 level information. This is typically the District information about the location.
     *
     * @return the Region3 level information.
     */
    public String getRegion3();

    /**
     * Set the Region3 level information. This is typically the District information about the location.
     *
     * @param region3 the Region3 level information.
     */
    public void setRegion3(String region3);

    /**
     * Get the Region4 level information. This is typically the Taluk of the location.
     *
     * @return the Region4 level information.
     */
    public String getRegion4();

    /**
     * Set the Region4 level information. This is typically the Taluk of the location.
     *
     * @param region4 the Region4 level information.
     */
    public void setRegion4(String region4);

    /**
     * Get the zip code.
     *
     * @return the zip code.
     */
    public long getZipCode();

    /**
     * Set the zip code.
     *
     * @param zipCode the zip code.
     */
    public void setZipCode(long zipCode);

    /**
     * Get the name of this location.
     *
     * @return the name of this location.
     */
    public String getName();

    /**
     * Set the name of this location.
     *
     * @param name the name of this location.
     */
    public void setName(String name);

    /**
     * Get the latitude of this location.
     *
     * @return the latitude of this location.
     */
    public float getLatitude();

    /**
     * Set the latitude of this location.
     *
     * @param latitude the latitude of this location.
     */
    public void setLatitude(float latitude);

    /**
     * Get the longitude of this location.
     *
     * @return the longitude of this location.
     */
    public float getLongitude();

    /**
     * Set the longitude of this location.
     *
     * @param longitude the longitude of this location.
     */
    public void setLongitude(float longitude);

}
