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

import java.util.List;

/**
 * Defines an object that implements the composite pattern which allows for parent child relationships to be defined.
 *
 * @param <P> the parent object type.
 * @param <C> the child object type.
 */
public interface Composition<P, C>
{
    /**
     * Get the parent of this object.
     *
     * @return the parent of this object.
     */
    public P getParent();

    /**
     * Set the parent of this object.
     *
     * @param parent the parent of this object.
     */
    public void setParent(P parent);

    /**
     * Get a list of children of this object. It is up to the implementation on whether it wishes to return an empty
     * list or null if children exist or the operation is not supported.
     *
     * @return a list of children of this object.
     */
    public List<C> getChildren();

    /**
     * Get the number of children of this object. Sometimes it is better to execute a different code path to get hold
     * of the number of children as compared to getting the count from the {@link #getChildren() list of children}.
     *
     * @return the number of children of this object.
     */
    public int getChildCount();
}
