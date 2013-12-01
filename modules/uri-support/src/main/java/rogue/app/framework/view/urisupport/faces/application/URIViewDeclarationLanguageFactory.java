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

package rogue.app.framework.view.urisupport.faces.application;

import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.urisupport.internal.URIUtil;

import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

/**
 * A custom view declaration language factory that identifies the correct VDL instance for the URLs resolved using
 * {@link URIResolver}.
 */
public class URIViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory
{
    private ViewDeclarationLanguageFactory wrapped;

    public URIViewDeclarationLanguageFactory(ViewDeclarationLanguageFactory wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ViewDeclarationLanguageFactory getWrapped()
    {
        return wrapped;
    }

    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId)
    {
        // Optimization: Ignore URIs that end with .xhtml
        if (viewId != null && viewId.startsWith("/") && !viewId.endsWith(URIUtil.getFaceletSuffix()))
        {
            URIAction action = URIResolver.getInstance().resolveURI(viewId);
            if (action != null)
            {
                viewId = URIViewHandler.ADMIN_PAGE_TEMPLATE;
            }
        }
        return getWrapped().getViewDeclarationLanguage(viewId);
    }
}
