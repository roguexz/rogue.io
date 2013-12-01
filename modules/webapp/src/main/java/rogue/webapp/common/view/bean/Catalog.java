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

package rogue.webapp.common.view.bean;

import rogue.app.framework.model.AppObject;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.store.catalog.model.ProductCategory;
import rogue.app.store.view.faces.controller.ProductCategoriesController;
import rogue.app.store.view.faces.controller.ProductsController;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.DataModel;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The bean used for rendering the site.
 */
@Named
@RequestScoped
public class Catalog implements Serializable
{
    private ProductCategoriesController categories = new ProductCategoriesController()
    {
        /**
         * Only fetch categories that have been enabled.
         */
        @Override
        protected DataModel<ProductCategory> constructDataModel()
        {
            final Map<String, Object> queryParams = new HashMap<>(1);
            queryParams.put("queryString", null);
            JpaQuery listingQuery = new JpaQuery("ProductCategoryEntity.findEnabledCategories", true, queryParams);
            JpaQuery countQuery = new JpaQuery("ProductCategoryEntity.findEnabledCategories.count", true, queryParams);
            return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
        }
    };
    private ProductCategoriesController subCategories = new ProductCategoriesController(categories)
    {
        /**
         * Only fetch categories that have been enabled.
         */
        @Override
        protected DataModel<ProductCategory> constructDataModel()
        {
            final Map<String, Object> queryParams = new HashMap<>(1);
            AppObject parent = getParentController().getSelectedResource();
            if (parent != null)
            {
                queryParams.put("queryString", parent.getId());
            }
            JpaQuery listingQuery = new JpaQuery("ProductCategoryEntity.findEnabledCategories", true, queryParams);
            JpaQuery countQuery = new JpaQuery("ProductCategoryEntity.findEnabledCategories.count", true, queryParams);
            return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
        }
    };
    private ProductsController products = new ProductsController(categories);

    public Catalog()
    {
    }

    /**
     * Get the categories controller.
     */
    public ProductCategoriesController getCategories()
    {
        return categories;
    }

    /**
     * Get the controller for the subcategories for the currently selected root category.
     */
    public ProductCategoriesController getSubCategories()
    {
        return subCategories;
    }

    /**
     * Get the product's controller that takes in to account the currently selected category.
     */
    public ProductsController getProducts()
    {
        return products;
    }
}
