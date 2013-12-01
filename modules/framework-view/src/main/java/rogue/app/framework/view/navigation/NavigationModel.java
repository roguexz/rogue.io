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

import java.io.Serializable;
import java.util.Collection;

/**
 * The navigation model represents the entry points in to the application.
 */
public interface NavigationModel
{
    /**
     * Get the top level navigation nodes.
     *
     * @return the root level navigation nodes.
     */
    Collection<NavigationNode> getNodes();
}
