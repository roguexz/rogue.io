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
import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.model.PersistentObject;
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
               @PageMetadata(displayName = "Languages", outcome = "admin-languages-listing",
                             path = "/i18n/fragments/language/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "Language Details", outcome = "admin-language-details",
                             path = "/i18n/fragments/language/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true
                       , type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "languages", model = Language.class)
public class LanguagesController extends EntityFacesController<Language>
{
    private UniqueNameValidator<Language> nameValidator =
            new UniqueNameValidator<>("getName", "LanguageEntity.findByName.count", "queryString", this);
    private UniqueNameValidator<Language> code2Validator =
            new UniqueNameValidator<>("getCode", "LanguageEntity.findByCode.count", "queryString", this);
    private UniqueNameValidator<Language> code3Validator =
            new UniqueNameValidator<>("getCode3", "LanguageEntity.findByCode3.count", "queryString", this);

    public LanguagesController()
    {
        super();
    }

    public LanguagesController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    public UniqueNameValidator<Language> getNameValidator()
    {
        return nameValidator;
    }

    public UniqueNameValidator<Language> getCodeValidator()
    {
        return code2Validator;
    }

    public UniqueNameValidator<Language> getCode3Validator()
    {
        return code3Validator;
    }


    @Override
    protected DataModel<Language> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ? "LanguageEntity.findAll" :
                                    "LanguageEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(
                searchString) ? "LanguageEntity.findAll.count" : "LanguageEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }
}
