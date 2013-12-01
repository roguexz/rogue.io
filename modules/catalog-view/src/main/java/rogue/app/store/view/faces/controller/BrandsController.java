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
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;
import rogue.app.store.catalog.model.Brand;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.validator.ValidatorException;
import java.util.HashMap;
import java.util.Map;

@Pages({
               @PageMetadata(displayName = "Brands", pageType = PageType.ITEMS_LISTING,
                             path = "/catalog/fragments/brand/listing.xhtml", fragment = true),
               @PageMetadata(displayName = "Brand Details", pageType = PageType.ITEM_DETAILS,
                             path = "/catalog/fragments/brand/details.xhtml",
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "brands", model = Brand.class)
public class BrandsController extends EntityFacesController<Brand>
{

    private UniqueNameValidator<Brand> nameValidator =
            new UniqueNameValidator<>("getName", "BrandEntity.findByName.count", "queryString", this);

    public BrandsController()
    {
        super();
    }

    public BrandsController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    public UniqueNameValidator getNameValidator()
    {
        return nameValidator;
    }

    @Override
    protected DataModel<Brand> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();

        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery =
                StringUtils.isEmpty(searchString) ? "BrandEntity.findAll" : "BrandEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(
                searchString) ? "BrandEntity.findAll.count" : "BrandEntity.searchByName.count";

        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }

    /**
     * Validate the brand parent.
     *
     * @param context   the current FacesContext instance.
     * @param component the UIComponent.
     * @param value     the value that has been set.
     * @throws ValidatorException if the validation fails.
     */
    public void validateBrandParent(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        if (value instanceof Brand)
        {
            Brand parent = (Brand) value;
            Brand selectedResource = getSelectedResource();
            if (selectedResource != null)
            {
                if (selectedResource.getId() == null)
                {
                    // New item, ignore and move on
                    return;
                }

                String parentName = parent.getName();
                while (parent != null)
                {
                    if (parent.getId().equals(selectedResource.getId()))
                    {
                        String message = parentName + " cannot be set as the parent of " + selectedResource
                                .getName() + " . This will cause a cyclic dependency.";
                        throw new ValidatorException(
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
                    }
                    parent = parent.getParent();
                }
            }
        }
    }
}
