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

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The default navigation model that is used in the administration screens of the application. The
 * <code>AdminNavigationModel</code> loads up all instances of {@link NavigationNode navigation nodes} that have been
 * defined in the system, via CDI.
 */
@Named
@SessionScoped
public class DefaultNavigationModel implements NavigationModel, Serializable
{
    @Inject
    @Any
    private Instance<NavigationNode> injectedNodes;

    private List<NavigationNode> nodes;

    public DefaultNavigationModel()
    {
    }

    @PostConstruct
    private void init()
    {
        if (nodes == null)
        {
            nodes = new ArrayList<>();

            if (injectedNodes != null)
            {
                for (NavigationNode nn : injectedNodes)
                {
                    nodes.add(nn);
                }

                Collections.sort(nodes, DefaultNavigationNode.NAVIGATION_NODE_COMPARATOR);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<NavigationNode> getNodes()
    {
        return nodes;
    }
}
