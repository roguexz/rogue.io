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

package rogue.app.store.view.faces.controller;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;
import rogue.app.store.catalog.model.Product;
import rogue.app.store.catalog.model.ProductCategory;

import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.Map;

/**
 * View controller for Products.
 */
@Pages({
               @PageMetadata(displayName = "Products",
                             pageType = PageType.ITEMS_LISTING, path = "/catalog/fragments/product/listing.xhtml",
                             fragment = true),
               @PageMetadata(displayName = "Product Details",
                             pageType = PageType.ITEM_DETAILS, path = "/catalog/fragments/product/details.xhtml",
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "products", model = Product.class)
public class ProductsController extends EntityFacesController<Product>
{
    private UniqueNameValidator<Product> nameValidator =
            new UniqueNameValidator<>("getName", "ProductEntity.findByName.count", "queryString", this);

    public ProductsController()
    {
        super();
        addChildController(new ImagesController(this));
    }

    public ProductsController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
        addChildController(new ImagesController(this));
    }

    public UniqueNameValidator getNameValidator()
    {
        return nameValidator;
    }

    @Override
    protected DataModel<Product> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);

        String resultsQuery = null;
        String countQueryString = null;

        boolean findByCategoryKey = false;
        AppObjectController<?, ?> parent = getParentController();
        if (parent instanceof ProductCategoriesController)
        {
            ProductCategoriesController categoriesController = (ProductCategoriesController) parent;
            ProductCategory cat = categoriesController.getSelectedResource();
            if (cat != null)
            {
                queryParams.put("queryString", cat.getId());
                findByCategoryKey = true;

                resultsQuery = "ProductEntity.findByCategory";
                countQueryString = "ProductEntity.findByCategory.count";
            }
        }

        if (!findByCategoryKey)
        {
            String searchString = getSearchCommand().getSelectedResource();
            if (!StringUtils.isEmpty(searchString))
            {
                queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
            }
            resultsQuery = StringUtils.isEmpty(searchString) ?
                           "ProductEntity.findAll" : "ProductEntity.searchByName";
            countQueryString = StringUtils.isEmpty(searchString) ?
                               "ProductEntity.findAll.count" : "ProductEntity.searchByName.count";
        }


        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }
}
