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

import java.util.Locale;

/**
 * A locale supported by the application.
 */
public interface AppLocale extends PersistentObject<AppLocale>
{
    /**
     * Get the java.util.Locale object representing this locale instance.
     *
     * @return the java.util.Locale object representing this locale instance.
     */
    public Locale getLocale();

    /**
     * Get the <code>Country</code> associated with this locale
     *
     * @return the <code>Country</code> associated with this locale
     */
    public Country getCountry();

    /**
     * Set the <code>Country</code> associated with this locale
     *
     * @param country the <code>Country</code> associated with this locale
     */
    public void setCountry(Country country);

    /**
     * Get the language associated with this locale.
     *
     * @return the language associated with this locale.
     */
    public Language getLanguage();

    /**
     * Set the language associated with this locale.
     *
     * @param language the language associated with this locale.
     */
    public void setLanguage(Language language);
}
