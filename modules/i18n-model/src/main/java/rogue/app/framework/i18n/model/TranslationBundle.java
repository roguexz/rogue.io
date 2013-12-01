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

import rogue.app.framework.model.NamedResource;
import rogue.app.framework.model.PersistentObject;

import java.util.List;

/**
 * Represents a resource bundle whose content is persisted in the data store.
 */
public interface TranslationBundle
        extends PersistentObject<TranslationBundle>, NamedResource
{

    /**
     * Get a list of keys which are a part of this translation bundle.
     *
     * @return a list of keys which are a part of this translation bundle.
     */
    public List<String> getKeys();

    /**
     * Get the number of keys which are a part of this translation bundle.
     *
     * @return the number of keys which are a part of this translation bundle.
     */
    public int getKeyCount();

    /**
     * Returns true if the specified key has been defined, false otherwise.
     *
     * @param key the key for the translation entry
     * @return true if the specified key has been defined, false otherwise.
     */
    public boolean containsKey(String key);

    /**
     * Get the default value associated with the specified key.
     *
     * @param key the key for the translation entry
     * @return the value associated with the key.
     */
    public String getValue(String key);

    /**
     * Get the translated value for the specified key. If no value was found for the given locale, then the default
     * value for the entry will be returned.
     *
     * @param key    the key for the translation entry.
     * @param locale a supported application locale.
     * @return the translated value, or the default if no translation was found.
     */
    public String getValue(String key, AppLocale locale);

    /**
     * Add / update a translation entry.
     *
     * @param key         the key for the translation entry.
     * @param value       the default value for the translation entry.
     * @param description the description for the translation entry.
     */
    public void addEntry(String key, String value, String description);

    /**
     * Remove a translation entry, along with all its localizations, from this bundle.
     *
     * @param key the key of the translation entry.
     */
    public void removeEntry(String key);

    /**
     * Add / update the localized value of a translation entry.
     *
     * @param key    the key for the translation entry.
     * @param value  the localized value for the translation entry.
     * @param locale a supported application locale.
     */
    public void addEntry(String key, String value, AppLocale locale);

    /**
     * Remove the localized value for a translation entry.
     *
     * @param key    the key of the translation entry.
     * @param locale a supported application locale.
     */
    public void removeEntry(String key, AppLocale locale);

}
