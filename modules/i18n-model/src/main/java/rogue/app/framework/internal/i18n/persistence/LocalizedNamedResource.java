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

import rogue.app.framework.i18n.model.AppLocale;
import rogue.app.framework.model.NamedResource;

/**
 * Identifies a resource that supports localization of its name. This is commonly seen with people having different
 * names in different locales. Likewise, cities and countries are called differently in different languages.
 */
public interface LocalizedNamedResource extends NamedResource
{
    /**
     * Get the localized name of this resource. If no localized name was found, then this method returns
     * the default {@link #getName() name} of this resource.
     *
     * @param locale the target locale.
     * @return the localized name of this resource if available, else the default name.
     */
    public String getLocalizedName(AppLocale locale);

    /**
     * Set the localized name of this resource.
     *
     * @param name   the localized name of this resource.
     * @param locale the target locale.
     */
    public void setLocalizedName(String name, AppLocale locale);

    /**
     * Get the localized description of this resource. If no localized description was found, then this method returns
     * the default {@link #getDescription() description} of this resource.
     *
     * @param locale the target locale.
     * @return the localized description of this resource if available, else the default description.
     */
    public String getLocalizedDescription(AppLocale locale);

    /**
     * Set the localized description of this resource.
     *
     * @param description the localized description of this resource.
     * @param locale      the target locale.
     */
    public void setLocalizedDescription(String description, AppLocale locale);
}
