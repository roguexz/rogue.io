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

package rogue.app.framework.view.faces.controller;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.security.User;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;

import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for User objects.
 */
@Pages({
               @PageMetadata(displayName = "Users", outcome = "admin-users-listing",
                             path = "/framework/fragments/users/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "User Details", outcome = "admin-user-details",
                             path = "/framework/fragments/users/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "users", model = User.class)
public class UsersController extends EntityFacesController<User>
{

    public UsersController()
    {
        super();
//        AttributesController controller = getAttributesController();
//        if (controller != null)
//        {
//            controller.setListingPageMetadata(new PageInfo("Attributes", "admin-user-attributes",
//                                                           PageType.ITEMS_LISTING,
//                                                           "/framework/fragments/attributes/listing.xhtml",
//                                                           true));
//            controller.setDetailsPageMetadata(new PageInfo("Attribute Details", "admin-user-attribute-details",
//                                                           PageType.ITEM_DETAILS,
//                                                           "/framework/fragments/attributes/details.xhtml", true));
//            addChildController(controller);
//        }

//        AddressController<User> addressController = new AddressController<>(this);
//        addressController.setListingPageMetadata(
//                new PageMetadataImpl("Addresses", "admin-user-address-listing", PageType.ITEMS_LISTING));
//        addressController.setDetailsPageMetadata(
//                new PageMetadataImpl("Address Details", "admin-user-address-details", PageType.ITEM_DETAILS));
//        addChildController(addressController);
    }

//    public AddressController<User> getAddressController()
//    {
//        return (AddressController<User>) getChildController(Address.class);
//    }

    @Override
    protected DataModel<User> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ?
                                    "UserEntity.findAll" : "UserEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(searchString) ?
                                        "UserEntity.findAll.count" : "UserEntity.searchByName.count";

        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }

}
