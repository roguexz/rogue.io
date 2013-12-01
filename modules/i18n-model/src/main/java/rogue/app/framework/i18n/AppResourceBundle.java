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

package rogue.app.framework.i18n;

import rogue.app.framework.i18n.model.AppLocale;
import rogue.app.framework.i18n.model.TranslationBundle;
import rogue.app.framework.persistence.JpaController;

import java.util.*;

/**
 * A {@link java.util.ResourceBundle} implementation that is backed by a
 * {@link rogue.app.framework.i18n.model.TranslationBundle} entity. Additionally, the <code>AppResourceBundle</code>
 * implements the {@link java.util.Map} interface (read-only) which makes it easy to use on the UI.
 */
public class AppResourceBundle extends ResourceBundle implements Map<String, Object>
{
    private TranslationBundle translationBundle;
    private AppLocale appLocale;

    public AppResourceBundle(String bundleName)
    {
        this(bundleName, null);
    }

    public AppResourceBundle(String bundleName, Locale locale)
    {
        if (bundleName == null)
        {
            throw new NullPointerException("Bundle name cannot be null");
        }

        Map<String, Object> params = new HashMap<>(1);
        params.put("queryString", bundleName.toLowerCase());
        translationBundle = JpaController.getController(TranslationBundle.class)
                                         .executeNamedQuerySingleResult("TranslationBundleEntity.findByName", params);
        if (translationBundle == null)
        {
            throw new MissingResourceException("Unable to find translation bundle with name: " + bundleName, bundleName,
                                               null);
        }

        if (locale != null)
        {
            // Check if we support the locale or not.
            params.clear();
            params.put("queryString", locale.toString());
            appLocale = JpaController.getController(AppLocale.class)
                                     .executeNamedQuerySingleResult("AppLocaleEntity.findByName", params);
        }
    }

    @Override
    public Locale getLocale()
    {
        return appLocale != null ? appLocale.getLocale() : null;
    }

    @Override
    protected Object handleGetObject(String key)
    {
        if (key == null)
        {
            throw new NullPointerException("Key cannot be null");
        }

        if (!translationBundle.containsKey(key))
        {
            throw new MissingResourceException(
                    String.format("Translation bundle: %s does not contain key %s", translationBundle.getName(), key),
                    translationBundle.getName(), key);
        }

        return translationBundle.getValue(key, appLocale);
    }

    @Override
    public Enumeration<String> getKeys()
    {
        return Collections.enumeration(translationBundle.getKeys());
    }

    @Override
    public int size()
    {
        return translationBundle.getKeys().size();
    }

    @Override
    public boolean isEmpty()
    {
        return translationBundle.getKeys().isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        return translationBundle.getKeys().contains(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    @Override
    public String get(Object key)
    {
        return translationBundle.getValue(key == null ? null : key.toString(), appLocale);
    }

    @Override
    public Object put(String key, Object value)
    {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public String remove(Object o)
    {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map)
    {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public Collection<Object> values()
    {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public Set<Entry<String, Object>> entrySet()
    {
        throw new UnsupportedOperationException("Operation not supported");
    }
}
