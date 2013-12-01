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

package rogue.app.framework.util;

/**
 * A state holder works like a cache map that can store and fetch data.
 */
public interface StateHolder
{
    /**
     * Get the object stored in this state holder, identified by the given key.
     *
     * @param key the key used to store the object in this map.
     * @return the stored object.
     */
    public <T> T get(String key);

    /**
     * Store the given object using the specified key in this map.
     *
     * @param key   the key used to store the object in this map.
     * @param value the stored object.
     */
    public void put(String key, Object value);

    /**
     * Removes the stored object, identified by the specified key, from this state holder.
     *
     * @param key the key used to store the object in this map.
     */
    public void remove(String key);

    /**
     * Returns true if this state holder contains an object identified by the specified key.
     *
     * @param key the key used to store the object in this map.
     * @return true if such a key exists, false otherwise.
     */
    public boolean containsKey(String key);

}
