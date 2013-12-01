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

import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.i18n.model.AppLocale;
import rogue.app.framework.i18n.model.Country;
import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.persistence.Cacheable;
import rogue.app.framework.persistence.*;

import javax.persistence.*;
import java.util.Locale;
import java.util.Objects;

@Entity
@NamedQueries(
        {
                @NamedQuery(name = "AppLocaleEntity.findAll",
                            query = "SELECT e FROM AppLocaleEntity e ORDER BY e.lowercaseLocaleString"),
                @NamedQuery(name = "AppLocaleEntity.findAll.count", query = "SELECT COUNT(e) FROM AppLocaleEntity e"),
                @NamedQuery(name = "AppLocaleEntity.searchByName",
                            query = "SELECT e FROM AppLocaleEntity e WHERE e.lowercaseLocaleString LIKE :queryString ORDER BY e.lowercaseLocaleString"),
                @NamedQuery(name = "AppLocaleEntity.searchByName.count",
                            query = "SELECT COUNT(e) FROM AppLocaleEntity e WHERE e.lowercaseLocaleString LIKE :queryString"),
                @NamedQuery(name = "AppLocaleEntity.findByName",
                            query = "SELECT e FROM AppLocaleEntity e WHERE e.lowercaseLocaleString = :queryString"),
                @NamedQuery(name = "AppLocaleEntity.findByName.count",
                            query = "SELECT COUNT(e) FROM AppLocaleEntity e WHERE e.lowercaseLocaleString = :queryString")
        }
)
@OrderByAttribute("lowercaseLocaleString")
@Cacheable(preferredScope = MemcachedScoped.class)
@EntityImplementationFor(AppLocale.class)
public class AppLocaleEntity extends PersistentEntity<AppLocale> implements AppLocale
{
    private String localeString;
    private String lowercaseLocaleString;
    private Key countryKey;
    @Column(nullable = false)
    private Key languageKey;

    public AppLocaleEntity()
    {
        super();
    }

    public AppLocaleEntity(AppLocaleEntity baseResource)
    {
        super(baseResource);
    }

    public Key getCountryKey()
    {
        return countryKey;
    }

    public void setCountryKey(Key countryKey)
    {
        if (isMutable() && !Objects.equals(this.countryKey, countryKey))
        {
            this.countryKey = countryKey;
            this.locale = null;
            markDirty();
        }
    }

    public Key getLanguageKey()
    {
        return languageKey;
    }

    public void setLanguageKey(Key languageKey)
    {
        if (isMutable() && !Objects.equals(this.languageKey, languageKey))
        {
            this.languageKey = languageKey;
            this.locale = null;
            markDirty();
        }
    }

    protected void prePersist()
    {
        super.prePersist();
        localeString = getLocale().toString();
        lowercaseLocaleString = StringUtils.lowerCase(localeString);
    }

    ///
    /// Non-entity implementations
    ///

    @Transient
    private Locale locale;

    @Override
    public Locale getLocale()
    {
        if (locale == null)
        {
            Language l = getLanguage();
            if (l == null || StringUtils.isEmpty(l.getCode()))
            {
                locale = new Locale("en"); // Default to english
            }
            else
            {
                Country c = getCountry();
                if (c == null)
                {
                    locale = new Locale(l.getCode());
                }
                else
                {
                    locale = new Locale(l.getCode(), c.getCode());
                }
            }
        }
        return locale;
    }

    @Override
    public Country getCountry()
    {
        return PersistenceUtils.getAppObject(Country.class, getCountryKey());
    }

    @Override
    public void setCountry(Country country)
    {
        Key key = PersistenceUtils.getEntityKey(country);
        if (isMutable() && !Objects.equals(this.countryKey, key))
        {
            setCountryKey(key);
            markDirty();
        }
    }

    @Override
    public Language getLanguage()
    {
        return PersistenceUtils.getAppObject(Language.class, getLanguageKey());
    }

    @Override
    public void setLanguage(Language language)
    {
        Key key = PersistenceUtils.getEntityKey(language);
        if (isMutable() && !Objects.equals(this.languageKey, key))
        {
            setLanguageKey(key);
            markDirty();
        }
    }

    @Override
    public String toString()
    {
        return getLocale().toString();
    }

    ///
    /// Equals and hashcode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AppLocaleEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        AppLocaleEntity that = (AppLocaleEntity) o;

        if (countryKey != null ? !countryKey.equals(that.countryKey) : that.countryKey != null)
        {
            return false;
        }
        if (languageKey != null ? !languageKey.equals(that.languageKey) : that.languageKey != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (countryKey != null ? countryKey.hashCode() : 0);
        result = 31 * result + (languageKey != null ? languageKey.hashCode() : 0);
        return result;
    }
}
