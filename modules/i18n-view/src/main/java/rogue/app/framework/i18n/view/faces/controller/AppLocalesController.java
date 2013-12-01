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
import rogue.app.framework.i18n.model.AppLocale;
import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Pages({
               @PageMetadata(displayName = "Locales", outcome = "admin-locales-listing",
                             path = "/i18n/fragments/appLocale/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "Locale Details", outcome = "admin-locale-details",
                             path = "/i18n/fragments/appLocale/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name="locales", model = AppLocale.class)
public class AppLocalesController extends EntityFacesController<AppLocale>
{
    @Inject
    private LanguagesController languagesController;
    @Inject
    private CountriesController countriesController;

    public AppLocalesController()
    {
        super();
    }

    public AppLocalesController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    public LanguagesController getLanguagesController()
    {
        if (languagesController == null)
        {
            languagesController = new LanguagesController();
        }
        return languagesController;
    }

    public CountriesController getCountriesController()
    {
        if (countriesController == null)
        {
            countriesController = new CountriesController();
        }
        return countriesController;
    }

    @Override
    protected DataModel<AppLocale> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(1);
        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ?
                                    "AppLocaleEntity.findAll" : "AppLocaleEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(searchString) ?
                                        "AppLocaleEntity.findAll.count" : "AppLocaleEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }

    @Override
    protected String saveCommandAction()
    {
        if (ScopedStateHolder.getStateHolder(RequestScoped.class).get("exception.raised") != null)
        {
            return null;
        }
        else
        {
            return super.saveCommandAction();
        }
    }

    @Override
    protected void saveCommandActionListener(ActionEvent event)
    {
        FacesMessage message = null;
        Language l = getSelectedResource().getLanguage();
        if (l.getCode() == null)
        {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid Language",
                                       "Only languages containing a 2 character code can be used");
        }
        else
        {
            Locale locale = getSelectedResource().getLocale();
            final Map<String, Object> queryParams = new HashMap<>(1);
            String searchString = getSearchCommand().getSelectedResource();
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(locale.toString()));
            AppLocale appLocale =
                    JpaController.getController(AppLocale.class)
                                 .executeNamedQuerySingleResult("AppLocaleEntity.findByName", queryParams);

            if (appLocale == null)
            {
                super.saveCommandActionListener(event);
            }
            else
            {
                // Raise a faces message.
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Duplicate Locale",
                                           "A locale with the given combination already exists");
            }
        }

        if (message != null)
        {
            //UserSession.queueMessage(message);
            ScopedStateHolder.getStateHolder(RequestScoped.class).put("exception.raised", true);
        }
    }
}
