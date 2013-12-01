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

package rogue.app.framework.i18n.view.faces.controller;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.i18n.model.City;
import rogue.app.framework.i18n.model.GeoRegion;
import rogue.app.framework.i18n.model.State;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.patterns.None;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;

import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.Map;

@Pages({
               @PageMetadata(displayName = "Cities", outcome = "admin-cities-listing",
                             path = "/i18n/fragments/city/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "City Details", outcome = "admin-city-details",
                             path = "/i18n/fragments/city/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "cities", model = City.class)
public class CitiesController extends GeoRegionController<City, State, None>
{
    private UniqueNameValidator<City> nameValidator;

    public CitiesController(GeoRegionController parentController)
    {
        super(parentController);
    }

    @Override
    protected DataModel<City> constructDataModel()
    {
        GeoRegion parent = (GeoRegion) getParentResource();
        if (parent == null)
        {
            return null;
        }

        final Map<String, Object> queryParams = new HashMap<>(2);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }
        queryParams.put("parentKey", PersistenceUtils.getEntityKey(parent));

        final String resultsQuery =
                StringUtils.isEmpty(searchString) ? "CityEntity.findAll" : "CityEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(
                searchString) ? "CityEntity.findAll.count" : "CityEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }

    public UniqueNameValidator<City> getNameValidator()
    {
        if (nameValidator == null)
        {
            Map<String, Object> params = new HashMap<>(1);
            params.put("parentKey", PersistenceUtils.getEntityKey((PersistentObject<?>) getParentResource()));
            nameValidator = new UniqueNameValidator<>("getName", "CityEntity.findByName.count", "queryString", this,
                                                      params);
        }
        return nameValidator;
    }

    @Override
    public void setParentResource(AppObject<?, ?> parent)
    {
        super.setParentResource(parent);
        nameValidator = null;
    }

}
