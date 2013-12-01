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
 * <code>Identity</code> states that an object, that implements this interface, can be identified uniquely within the
 * system. Think of <code>Identity</code> as a way to abstract over all objects residing within the system, which use
 * different schemes to identify a object. E.g., an object stored in the database might define a <code>Key</code> to
 * uniquely identify itself; whereas a Calendar might define a GUID (as a String) as the identity. Usually, systems tend
 * to auto-generate this identifier, hence this interface only exposes a getter for the same.
 */
public interface Identity<E>
{
    /**
     * Get the unique identifier of this object.
     *
     * @return the unique identifier of this object.
     */
    public E getId();

}
