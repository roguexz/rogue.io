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

import rogue.app.framework.patterns.Composition;

import java.util.List;

/**
 * Identify application objects that exhibit the characteristic of a parent-child relationship on <b>the same type</b>.
 */
public interface NestedResource<E extends NestedResource<E>> extends Composition<E, E>
{
    /**
     * Get the parent of this object.
     *
     * @return the parent of this object, or null if not available.
     */
    public E getParent();

    /**
     * Set the parent of this resource.
     *
     * @param parent the parent of this resource. A null value indicates that this is a top level node.
     */
    public void setParent(E parent);

    /**
     * Get a list of immediate children of this resource. This will effectively return the result of invoking
     * {@link #getChildren(boolean) getChildren(false)}.
     *
     * @return a list of immediate children.
     */
    public List<E> getChildren();

    /**
     * Get a count of immediate children of this resource. This will effectively return the result of invoking
     * {@link #getChildCount(boolean) getChildCount(false)}.
     *
     * @return a count of immediate children of this resource.
     */
    public int getChildCount();

    /**
     * Get the children of this object. The boolean argument determines if this method should return just the immediate
     * children, or fetch a recursive list of all children under this object.
     *
     * @param recursive boolean argument identifying if the method should return just the immediate children or the
     *                  recursive list.
     * @return a list of child objects. If no children were found, then an empty list is returned.
     */
    public List<E> getChildren(boolean recursive);

    /**
     * Get the count of the children under this resource.
     *
     * @param recursive boolean argument identifying if the method should return a count of just the immediate children
     *                  or the entire sub-tree.
     * @return the number of children.
     */
    public int getChildCount(boolean recursive);
}
