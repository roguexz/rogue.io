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

import rogue.app.framework.internal.FrameworkQualifier;
import rogue.app.framework.view.faces.controller.AttributesController;
import rogue.app.framework.view.faces.controller.UsersController;
import rogue.webapp.common.view.bean.AdminControllers;

import javax.inject.Inject;

/**
 * The navigation node for "System" in the administration screens. This node contains the following child nodes:
 * <ul>
 * <li>Application Properties</li>
 * <li>Users</li>
 * </ul>
 */
@FrameworkQualifier
public class FrameworkNavigationNodes extends AdminNavNode
{
    @Inject
    public FrameworkNavigationNodes(AdminControllers adminControllers)
    {
        super("System", true, null, 0);
        addControllerRef("Attributes", adminControllers, AttributesController.class);
        addControllerRef("Users", adminControllers, UsersController.class);
    }
}
