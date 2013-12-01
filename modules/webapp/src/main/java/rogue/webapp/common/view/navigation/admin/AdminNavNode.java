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

package rogue.webapp.common.view.navigation.admin;

import com.google.common.base.Preconditions;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.AppObjectControllerCollection;
import rogue.app.framework.view.faces.AbstractFacesController;
import rogue.app.framework.view.navigation.DefaultNavigationNode;

/**
 * Base class for all nodes that will be displayed as the navigation for administration screens.
 */
public abstract class AdminNavNode extends DefaultNavigationNode
{
    private int idx = 0;

    public AdminNavNode(String name, boolean synthetic, String path, int idx)
    {
        super(name, synthetic, path, idx);
    }

    protected void addControllerRef(String name, AppObjectControllerCollection collection,
                                    Class<? extends AppObjectController> clazz)
    {
        Preconditions.checkNotNull(collection, "Collection cannot be null");
        Preconditions.checkNotNull(clazz, "Class cannot be null");

        ControllerFor controllerFor = clazz.getAnnotation(ControllerFor.class);
        if (controllerFor != null)
        {
            addControllerRef(name,
                             (AbstractFacesController) collection.getController(controllerFor.name()));
        }
    }

    protected void addControllerRef(String name, AbstractFacesController controller)
    {
        if (controller != null)
        {
            addChildNode(new DefaultNavigationNode(name, false,
                                                   controller.getListingPageMetadata().getOutcome(), idx++));
        }
    }
}
