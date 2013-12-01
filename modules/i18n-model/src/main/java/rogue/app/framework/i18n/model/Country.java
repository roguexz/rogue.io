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
 * ISO 3166 definition of Country that can be used by applications.
 */
public interface Country extends GeoRegion<Country, None, State>
{
    /**
     * Get the ISO 3166 code of the country.
     */
    public String getCode();

    /**
     * Set the ISO 3166 code of the country.
     */
    public void setCode(String code);

    /**
     * Get the international direct dialing code for the country.
     *
     * @return the international direct dialing code for the country.
     */
    public Integer getIDDCode();

    /**
     * Set the international direct dialing code for the country.
     *
     * @param code the international direct dialing code for the country.
     */
    public void setIDDCode(Integer code);

}
