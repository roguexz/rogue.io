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
import rogue.app.framework.patterns.Composition;

import java.util.List;

/**
 * A GeoRegion represents a geographical region - like a Country, State, City, etc. This interface captures the common
 * attributes associated with each of the sub-classes.
 *
 * @param <T> the current geo region type
 */
public interface GeoRegion<T extends GeoRegion<T, P, C>, P, C> extends PersistentObject<T>, Composition<P, C>
{
    /**
     * Get the name of this geographical region.
     *
     * @return the name of this geographical region.
     */
    public String getName();

    /**
     * Set the name of this geographical region.
     *
     * @param name the name of this geographical region.
     */
    public void setName(String name);

    /**
     * Get the language spoken by this geographical region. If this region supports more than one, then this will
     * return the most common language or the default language to be used within this region.
     *
     * @return the language spoken by this geographical region.
     */
    public Language getLanguage();

    /**
     * Set the language spoken by this geographical region. If this region supports more than one, then this will set
     * the most common language or the default language to be used within this region.
     *
     * @param language the language spoken by this geographical region.
     */
    public void setLanguage(Language language);

    /**
     * Get the list of languages spoken within this geographical region.
     *
     * @return the list of languages spoken within this geographical region.
     */
    public List<? extends Language> getLanguages();

    /**
     * Add a language to the list of spoken languages to this geographical region.
     *
     * @param language      the language to add.
     * @param markAsDefault should this marked as the default language for this region?
     */
    public void addLanguage(Language language, boolean markAsDefault);

    /**
     * Remove the specified language from the list of languages spoken in this region. If the specified language
     * is the default language, then {@link #getLanguage() default language} will return the first available language
     * from the remaining list, or null.
     *
     * @param language the language to remove.
     */
    public void removeLanguage(Language language);

    /**
     * Identifies if this geo region should be enabled in the application or not. This method is primarily targeted
     * towards identifying which GeoRegions ought to be displayed to the end user.
     *
     * @return true if this geo region should be enabled in the application, false otherwise.
     */
    public boolean isEnabled();

    /**
     * Set whether or not this geographical region should be enabled for participating in the end user application.
     *
     * @param enabled should this region be enabled in the end user UI?
     */
    public void setEnabled(boolean enabled);

}
