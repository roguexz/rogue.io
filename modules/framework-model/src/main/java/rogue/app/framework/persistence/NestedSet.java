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

package rogue.app.framework.persistence;

import rogue.app.framework.AppRuntimeException;

/**
 * Framework interface, implemented by classes that use rational numbers to key nested sets.
 *
 * @see <a href="http://arxiv.org/abs/0806.3115">Using rational numbers to key nested sets, by Dan Hazel</a>
 */
public interface NestedSet
{
    /**
     * Get the numerator value for the current node.
     *
     * @return the numerator value for the current node.
     */
    public long getNodeNumerator();

    /**
     * Get the denominator value for the current node.
     *
     * @return the denominator value for the current node.
     */
    public long getNodeDenominator();

    /**
     * Get the next sibling's numerator value.
     *
     * @return the next sibling's numerator value.
     */
    public long getSiblingNodeNumerator();

    /**
     * Get the next sibling's denominator value.
     *
     * @return the next sibling's denominator value.
     */
    public long getSiblingNodeDenominator();

    /**
     * Get the node's index (1 based) among it's sibling. This index may not represent the true index, if the
     * implementation chooses to not reorder elements when a sibling on the left of this node is deleted.
     *
     * @return the node's index (1 based) among it's sibling.
     */
    public int getNodeIndex();

    /**
     * Set the node's index (1 based) among it's siblings.
     *
     * @param idx the node's index (1 based) among it's siblings.
     */
    public void setNodeIndex(int idx);

    /**
     * Get the current value of the number of child tokens issued. This value will be equal to or greater than the
     * number of children for this node.
     *
     * @return the current value of the number of child tokens issued.
     */
    public int getChildCounter();

    /**
     * Increment and return a new child index token that can be used for a newly added child element. It is the
     * responsibility of the caller to invoke this method on a mutable instance and then persist the mutable instance
     * to the data store.
     *
     * @return the incremented child index token.
     * @throws rogue.app.framework.AppRuntimeException
     *          if the method is invoked on an immutable instance.
     */
    public int incrementChildCounter() throws AppRuntimeException;
}
