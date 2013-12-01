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

import rogue.app.framework.patterns.None;

/**
 * Representation of a City
 */
public interface City extends GeoRegion<City, State, None>
{
    /**
     * Get the name of the city.
     *
     * @return the name of the city.
     */
    public String getName();

    /**
     * Set the name of the city.
     *
     * @param name the name of the city.
     */
    public void setName(String name);

    /**
     * Get the alternate name for the city.
     *
     * @return the alternate name for the city.
     */
    public String getAlternateName();

    /**
     * Set the alternate name for the city.
     *
     * @param name the alternate name for the city.
     */
    public void setAlternateName(String name);

    /**
     * Get the state to which this cty belongs.
     *
     * @return the state to which this cty belongs.
     */
    public State getState();

    /**
     * Set the state to which this cty belongs.
     *
     * @param state the state to which this cty belongs.
     */
    public void setState(State state);

    /**
     * Convenience method to get the country to which this city belongs to.
     *
     * @return the country to which this city belongs to.
     */
    public Country getCountry();

}
