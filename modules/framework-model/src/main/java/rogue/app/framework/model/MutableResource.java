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

package rogue.app.framework.model;

import rogue.app.framework.AppRuntimeException;

/**
 * Identifies a resource which can be edited at runtime. <code>MutableResource</code> allows us to easily implement
 * the copy-before-write paradigm.
 */
public interface MutableResource<T>
{
    /**
     * Identifies if the current instance is mutable or not.
     */
    public boolean isMutable();

    /**
     * Get a mutable instance.
     *
     * @return a mutable instance.
     * @throws rogue.app.framework.AppRuntimeException
     *          if the operation fails.
     */
    public T getMutableInstance() throws AppRuntimeException;

    /**
     * Get the base resource from which this object was cloned.
     *
     * @return the base resource if available.
     */
    public T getBaseResource();

    /**
     * Identifies if the data in this object has been edited or not.
     *
     * @return true if the data within this object has been edited, false otherwise.
     */
    public boolean isDirty();
}
