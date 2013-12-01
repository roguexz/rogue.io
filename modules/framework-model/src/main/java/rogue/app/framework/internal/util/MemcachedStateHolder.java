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

package rogue.app.framework.internal.util;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.persistence.MemcachedScoped;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of {@link rogue.app.framework.util.StateHolder} that is scoped to {@link rogue.app.framework.persistence.MemcachedScoped}
 */
@ApplicationScoped
@ScopedStateHolderQualifier(MemcachedScoped.class)
public class MemcachedStateHolder implements StateHolder
{
    private static final String CLASS_NAME = MemcachedStateHolder.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * Default cache expiration of 1 hour.
     */
    private static final Expiration DEFAULT_EXPIRATION = Expiration.byDeltaSeconds(3600);

    private static MemcacheService memCache = MemcacheServiceFactory.getMemcacheService();

    @Override
    public <T> T get(String key)
    {
        final String METHOD_NAME = "get";
        if (key != null)
        {
            try
            {
                return (T) memCache.get(key);
            }
            catch (Exception ex)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to fetch cached value for key: " + key, ex);
            }
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        "Key is null. This should never happen. Returning null.",
                        new AppRuntimeException("Cache key was null."));
        }
        return null;
    }

    @Override
    public void put(String key, Object value)
    {
        final String METHOD_NAME = "put";
        if (key != null)
        {
            try
            {
                memCache.put(key, value, DEFAULT_EXPIRATION);
            }
            catch (Exception ex)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Unable to add object to cache [ " + key + " , " + value + " ] ", ex);
            }
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Key is null. This should never happen.",
                        new AppRuntimeException("Cache key was null."));
        }
    }

    @Override
    public void remove(String key)
    {
        final String METHOD_NAME = "remove";
        if (key != null)
        {
            try
            {
                memCache.delete(key);
            }
            catch (Exception ex)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to discard cached value for key: " + key,
                            ex);
            }
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        "Key is null. This should never happen.",
                        new AppRuntimeException("Cache key was null."));
        }
    }

    @Override
    public boolean containsKey(String key)
    {
        return memCache.contains(key);
    }
}
