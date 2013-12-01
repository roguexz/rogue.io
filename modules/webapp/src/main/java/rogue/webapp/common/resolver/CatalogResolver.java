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

package rogue.webapp.common.resolver;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Preconditions;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolutionContext;
import rogue.app.framework.view.urisupport.URIResolutionProvider;
import rogue.app.framework.view.util.FacesUtils;
import rogue.app.store.catalog.model.ProductCategory;
import rogue.app.store.view.faces.controller.ProductCategoriesController;
import rogue.webapp.common.view.bean.Catalog;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provider for handling pretty URLs on the public site. The following patterns are handled by this resolver:
 * <p/>
 * <pre>
 *     /catalog                                       -- All products under all categories
 *     /catalog/{rootCategory}                        -- All products under the specified root category
 *     /catalog/{rootCategory}/category/{subCategory} -- All products under the specified subCategory
 * </pre>
 */
public class CatalogResolver implements URIResolutionProvider
{
    private static final String CLASS_NAME = CatalogResolver.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final String CATALOG = "catalog";
    private static final String VIEW_ID = "/catalog/index.xhtml";
    private static final String ROOT_CATEGORY = "rootCategory";
    private static final String SUB_CATEGORY = "subCategory";

    @Override
    public URIAction resolveURI(URIResolutionContext context, String uri)
    {
        Preconditions.checkNotNull(context, "URI context cannot be null.");
        if (CATALOG.equals(context.getRootContext()))
        {
            URIAction action = null;
            // For case 1, we don't have to do anything as of now.
            if (uri != null && !uri.isEmpty())
            {
                String[] tokens = uri.split("/");
                int idx = 0;
                if (uri.startsWith("/"))
                {
                    // ignore the first token
                    idx++;
                }

                if (idx < tokens.length)
                {
                    while (idx < tokens.length)
                    {
                        String category = tokens[idx++];
                        if (action == null)
                        {
                            action = new CatalogAction(context.getRootContext(), ROOT_CATEGORY);
                        }
                        else
                        {
                            action = new CatalogAction(SUB_CATEGORY, action);
                        }

                        action.setId(category);
                        action.setViewId(VIEW_ID);
                    }
                }

                if (action != null)
                {
                    context.setResolved(true);
                    return action;
                }
            }
        }
        return null;
    }

    @Override
    public AppObjectController<?, ?> resolveAction(URIResolutionContext context, URIAction action)
    {
        if (!(action instanceof CatalogAction))
        {
            return null;
        }

        Object obj = FacesUtils.resolveObject("#{" + action.getContext() + "}");

        if (obj instanceof Catalog)
        {
            Catalog catalog = (Catalog) obj;

            Stack<URIAction> stack = new Stack<>();
            URIAction ua = action;
            boolean subCategorySpecified = false;
            while (ua != null)
            {
                stack.push(ua);
                if (SUB_CATEGORY.equals(ua.getControllerName()))
                {
                    subCategorySpecified = true;
                }
                ua = ua.getParent();
            }

            if (stack.size() > 2)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, "resolveAction",
                            "Something is not right with the URI. Found more than 2 URIAction objects.");
            }

            while (!stack.isEmpty())
            {
                ua = stack.pop();
                ProductCategoriesController controller;
                if (ROOT_CATEGORY.equals(ua.getControllerName()))
                {
                    controller = catalog.getCategories();

                    Key key = controller.parseId(ua.getId());
                    ProductCategory category = controller.findById(key);
                    controller.setSelectedResource(category);

                    if (!subCategorySpecified)
                    {
                        context.setResolved(true);
                    }
                }
                else if (SUB_CATEGORY.equals(ua.getControllerName()))
                {
                    controller = catalog.getSubCategories();

                    Key key = controller.parseId(ua.getId());
                    ProductCategory category = controller.findById(key);
                    controller.setSelectedResource(category);

                    context.setResolved(true);
                }
            }


        }

        return null;
    }

    public static final class CatalogAction extends URIAction
    {
        public CatalogAction(String context, String controllerName)
        {
            super(context, controllerName);
        }

        public CatalogAction(String controllerName, URIAction parent)
        {
            super(controllerName, parent);
        }

        @Override
        public String toString()
        {
            return "CatalogAction: " + getControllerName();
        }
    }
}
