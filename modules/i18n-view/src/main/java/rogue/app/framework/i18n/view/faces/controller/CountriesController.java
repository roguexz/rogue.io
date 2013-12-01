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
import rogue.app.framework.i18n.model.Country;
import rogue.app.framework.i18n.model.State;
import rogue.app.framework.patterns.None;
import rogue.app.framework.persistence.JpaQuery;
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
               @PageMetadata(displayName = "Countries", outcome = "admin-countries-listing",
                             path = "/i18n/fragments/country/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "Country Details", outcome = "admin-country-details",
                             path = "/i18n/fragments/country/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name="countries", model = Country.class)
public class CountriesController extends GeoRegionController<Country, None, State>
{
    private UniqueNameValidator<Country> nameValidator =
            new UniqueNameValidator<>("getName", "CountryEntity.findByName.count", "queryString", this);
    private UniqueNameValidator<Country> countryCodeValidator =
            new UniqueNameValidator<>("getCode", "CountryEntity.findByCode.count", "queryString", this);

    public CountriesController()
    {
        super();
        addChildController(new StatesController(this));
    }

    public StatesController getStatesController()
    {
        return (StatesController) getChildController(State.class);
    }

    @Override
    protected DataModel<Country> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ?
                                    "CountryEntity.findAll" : "CountryEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(searchString) ?
                                        "CountryEntity.findAll.count" : "CountryEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }

    public UniqueNameValidator<Country> getNameValidator()
    {
        return nameValidator;
    }

    public UniqueNameValidator<Country> getCountryCodeValidator()
    {
        return countryCodeValidator;
    }
}
