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
import rogue.app.framework.i18n.model.TranslationBundle;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;

import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.Map;

@Pages({
               @PageMetadata(displayName = "Translation Bundles", outcome = "admin-xlations-listing",
                             path = "/i18n/fragments/translations/bundles/listing.xhtml",
                             pageType = PageType.ITEMS_LISTING, fragment = true),
               @PageMetadata(displayName = "Translation Bundle Details", outcome = "admin-xlation-details",
                             path = "/i18n/fragments/translations/bundles/details.xhtml",
                             pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "translations", model = TranslationBundle.class)
public class TranslationsController extends EntityFacesController<TranslationBundle>
{
    private UniqueNameValidator<TranslationBundle> nameValidator =
            new UniqueNameValidator<>("getName", "TranslationBundleEntity.findByName.count",
                                      "queryString", this);

    public TranslationsController()
    {
        super();
        // Now remove attributes controller that was added by the parent and add the strings controller instead
        AppObjectController controller = getChildController(Attribute.class);
        removeChildController(controller);

        addChildController(new StringsController(this));
    }

    public UniqueNameValidator<TranslationBundle> getNameValidator()
    {
        return nameValidator;
    }

    @Override
    protected DataModel<TranslationBundle> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ? "TranslationBundleEntity.findAll" :
                                    "TranslationBundleEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(searchString) ? "TranslationBundleEntity.findAll.count" :
                                        "TranslationBundleEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);

    }
}
