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
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.i18n.model.GeoCode;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.Map;

@Pages({
               @PageMetadata(displayName = "Geo Codes", outcome = "admin-geocodes-listing",
                             path = "/i18n/fragments/geoCode/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "Geo Code Details", outcome = "admin-geocode-details",
                             path = "/i18n/fragments/geoCode/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name="geo-codes", model = GeoCode.class)
public class GeoCodesController extends EntityFacesController<GeoCode>
{
    private MapModel mapModel = new DefaultMapModel();
    private Marker marker;

    private UniqueNameValidator<GeoCode> zipCodeValidator =
            new UniqueNameValidator<>("getZipCode", "GeoCodeEntity.findByZipCode.count", "queryString", this);

    public GeoCodesController()
    {
        super();
    }

    public GeoCodesController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    @Override
    protected DataModel<GeoCode> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ?
                                    "GeoCodeEntity.findAll" : "GeoCodeEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(
                searchString) ? "GeoCodeEntity.findAll.count" : "GeoCodeEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);

    }

    public UniqueNameValidator<GeoCode> getZipCodeValidator()
    {
        return zipCodeValidator;
    }

    @Override
    public void setSelectedResource(GeoCode item)
    {
        super.setSelectedResource(item);
        updateMarker();
    }

    @Override
    protected void saveCommandActionListener(ActionEvent event)
    {
        super.saveCommandActionListener(event);
        updateMarker();
    }

    public MapModel getMapModel()
    {
        return mapModel;
    }

    private void updateMarker()
    {
        GeoCode item = getSelectedResource();
        if (item != null)
        {
            LatLng ll = new LatLng(item.getLatitude(), item.getLongitude());
            if (marker == null)
            {
                marker = new Marker(ll);
                mapModel.addOverlay(marker);
            }
            else
            {
                marker.setLatlng(ll);
            }
        }
    }

}
