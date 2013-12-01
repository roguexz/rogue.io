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

package rogue.app.framework.view.faces.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

/**
 * The <code>GroupComponent</code> serves to handle the cases where all one needs to do is encapsulate the HTML content
 * without the need for using explicit JSTL IF tag.
 *
 * @see <a href="http://roguexz.blogspot.in/2013/10/the-empty-jsf-component.html">The write up</a> on why I needed
 *      this component.
 */

@FacesComponent(value = "group", createTag = true, tagName = "group")
public class GroupComponent extends UIComponentBase
{
    @Override
    public String getFamily()
    {
        return "Rogue.IO";
    }
}
