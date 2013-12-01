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

package rogue.app.framework.patterns;

/**
 * A generic interface that identifies the current object as being orthogonally associated with another object within
 * the system. E.g., An address object could be associated with either an User or an Establishment.
 * <p/>
 * This isn't exactly a pattern, but I couldn't figure out where else to place this interface. So it is here for now.
 *
 * @param <T> the object type with which the current object is associated.
 */
public interface Association<T>
{
    /**
     * Get the resource associated with the current object.
     *
     * @return the resource associated with the current object.
     */
    public T getAssociatedResource();

    /**
     * Set the resource associated with the current object.
     *
     * @param associatedResource the resource associated with the current object.
     */
    public void setAssociatedResource(T associatedResource);

}
