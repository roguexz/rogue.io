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

/**
 * A generic interface that identifies controllers / holders that support selection of resources.
 *
 * @param <T> the type of the object that is stored by this object
 */
public interface ResourceSelection<T>
{
    /**
     * Get the resource on which the rest operations will be invoked.
     *
     * @return the resource on which the rest operations will be invoked.
     */
    public T getSelectedResource();

    /**
     * Set the resource on which the rest operations will be invoked.
     *
     * @param resource the resource on which the rest operations will be invoked.
     */
    public void setSelectedResource(T resource);

    /**
     * Get a list of resources on which the rest of the operations will be performed.
     *
     * @return a list of resources on which the rest of the operations will be performed.
     */
    public T[] getSelectedResources();

    /**
     * Set a list of resources on which the rest of the operations will be performed.
     *
     * @param resources a list of resources on which the rest of the operations will be performed.
     */
    public void setSelectedResources(T[] resources);
}
