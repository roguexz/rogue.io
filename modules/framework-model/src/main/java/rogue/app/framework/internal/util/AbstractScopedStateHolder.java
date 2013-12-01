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

import rogue.app.framework.util.StateHolder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation for all {@link rogue.app.framework.util.StateHolder} types
 */
public abstract class AbstractScopedStateHolder implements StateHolder, Serializable
{
    private Map<String, Object> cacheMap = null;

    protected AbstractScopedStateHolder()
    {
    }

    @Override
    public <T> T get(String key)
    {
        return cacheMap != null ? (T) cacheMap.get(key) : null;
    }

    @Override
    public void put(String key, Object value)
    {
        if (cacheMap == null)
        {
            cacheMap = new HashMap<>(5);
        }
        cacheMap.put(key, value);
    }

    @Override
    public void remove(String key)
    {
        if (cacheMap != null)
        {
            cacheMap.remove(key);
        }
    }

    @Override
    public boolean containsKey(String key)
    {
        return cacheMap != null && cacheMap.containsKey(key);
    }
}
