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

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.*;

/**
 * Default implementation of the {@link NavigationNode} definition.
 */
public class DefaultNavigationNode implements NavigationNode, Serializable
{
    public static final Comparator<NavigationNode> NAVIGATION_NODE_COMPARATOR = new Comparator<NavigationNode>()
    {
        @Override
        public int compare(NavigationNode o1, NavigationNode o2)
        {
            return (o1 != null) ? ((o2 != null) ? (o1.getIndex() - o2.getIndex()) : -1) : ((o2 != null) ? 1 : 0);
        }
    };

    private int idx;
    private String name;
    private boolean synthetic;
    private NavigationNode parent;
    private List<NavigationNode> children;
    private String path;
    private boolean listSorted = false;

    public DefaultNavigationNode(String name, boolean synthetic, String path, int idx)
    {
        this.name = Preconditions.checkNotNull(name, "Name cannot be null");
        this.synthetic = synthetic;
        this.path = path;
        this.idx = idx;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isSynthetic()
    {
        return synthetic;
    }

    @Override
    public NavigationNode getParent()
    {
        return parent;
    }

    @Override
    public void setParent(NavigationNode node)
    {
        ensureNoCyclicDependency(node);
        this.parent = node;
    }

    @Override
    public boolean isLeafNode()
    {
        return children == null || children.isEmpty();
    }

    @Override
    public Collection<NavigationNode> getChildren()
    {
        if (children != null && !listSorted)
        {
            Collections.sort(children, NAVIGATION_NODE_COMPARATOR);
            listSorted = true;
        }
        return children;
    }

    @Override
    public void addChildNode(NavigationNode node)
    {
        Preconditions.checkNotNull(node, "Child node cannot be null.");
        if (children == null)
        {
            children = new ArrayList<>();
        }

        node.setParent(this);
        children.add(node);
        listSorted = false;
    }

    @Override
    public void removeChildNode(NavigationNode node)
    {
        if (children != null)
        {
            children.remove(node);
        }
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public int getIndex()
    {
        return idx;
    }

    private void ensureNoCyclicDependency(NavigationNode node)
    {
        // Verify that there is no cyclic dependency.
        NavigationNode p = getParent();
        StringBuilder builder = new StringBuilder(100);
        builder.append(getName()).append(" -> ");
        while (p != null)
        {
            builder.append(p.getName()).append(" -> ");
            if (p == node)
            {
                builder.append(node.getName());
                throw new IllegalArgumentException(
                        "Cyclic dependency found. Cannot set a parent node as a child node. " + builder.toString());
            }

            p = p.getParent();
        }
    }
}
