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

package rogue.app.framework.view.navigation;

import java.util.Collection;

/**
 * Represents a node within the {@link DefaultNavigationModel}.
 * <ul>
 * <li>Nodes can comprise of further nodes.</li>
 * <li>A node is considered as a folder if its {@link #isSynthetic()} returns true.</li>
 * <li>Nodes are sorted within a collection based on their {@link #getIndex() index} value.</li>
 * </ul>
 */
public interface NavigationNode
{
    /**
     * Get the index at which this node should be placed in the parent. If two nodes return the same index, then the
     * system is free to define an ordering scheme.
     *
     * @return the index at which this node should be placed in the parent.
     */
    int getIndex();

    /**
     * Get the name of the navigation node.
     *
     * @return the name of the navigation node.
     */
    String getName();

    /**
     * Is this node a synthetic category only node?
     *
     * @return true if this node a synthetic category-only node, false otherwise.
     */
    boolean isSynthetic();

    /**
     * Get the parent node if available.
     *
     * @return the parent node if available.
     */
    NavigationNode getParent();

    /**
     * Set the parent node.
     */
    void setParent(NavigationNode parent);

    /**
     * Identifies if this node has any children or not.
     *
     * @return true if this node has any children, false otherwise.
     */
    boolean isLeafNode();

    /**
     * Get the children of this node, if available.
     *
     * @return the children of this node, if available.
     */
    Collection<NavigationNode> getChildren();

    /**
     * Add a child node.
     *
     * @param node the node to add.
     */
    void addChildNode(NavigationNode node);

    /**
     * Remove a child node.
     *
     * @param node the node to remove.
     */
    void removeChildNode(NavigationNode node);

    /**
     * Get the path associated with this node.
     *
     * @return the path associated with this node.
     */
    String getPath();
}
