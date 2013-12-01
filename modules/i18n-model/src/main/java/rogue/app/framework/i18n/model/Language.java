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
 * Representation of the ISO 639.2 language codes.
 *
 * @see <a href="http://www.loc.gov/standards/iso639-2/php/English_list.php">ISO 639.2</a>
 */
public interface Language extends PersistentObject<Language>
{
    /**
     * Get the name of this language.
     *
     * @return the name of this language.
     */
    public String getName();

    /**
     * Set the name of this language.
     *
     * @param name the name of this language.
     */
    public void setName(String name);

    /**
     * Get the 2 letter code for this language (if available).
     *
     * @return the 2 letter code for this language (if available).
     */
    public String getCode();

    /**
     * Set the 2 letter code for this language (if available).
     *
     * @param code the 2 letter code for this language (if available).
     */
    public void setCode(String code);

    /**
     * Get the 3 letter code for this language (if available).
     *
     * @return the 3 letter code for this language (if available).
     */
    public String getCode3();

    /**
     * Set the 3 letter code for this language (if available).
     *
     * @param code3 the 3 letter code for this language (if available).
     */
    public void setCode3(String code3);
}
