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

/**
 * Represents a State / Region which is part of a country.
 */
public interface State extends GeoRegion<State,Country,City>
{
    /**
     * Get the name of this state.
     *
     * @return the name of this state.
     */
    public String getName();

    /**
     * Set the name of this state.
     *
     * @param name the name of this state.
     */
    public void setName(String name);


    /**
     * Get the country to which this geographical region belongs to.
     *
     * @return the country to which this geographical region belongs to.
     */
    public Country getCountry();

    /**
     * Set the country to which this geographical region belongs.
     *
     * @param country the country to which this geographical region belongs to.
     */
    public void setCountry(Country country);

    /**
     * Get the ISO 3166 code of the country.
     */
    public String getCode();

    /**
     * Set the ISO 3166 code of the country.
     */
    public void setCode(String code);

}
