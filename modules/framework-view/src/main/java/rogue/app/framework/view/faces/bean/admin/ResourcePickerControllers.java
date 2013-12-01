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

package rogue.app.framework.view.faces.bean.admin;

import rogue.app.framework.view.faces.AbstractFacesController;
import rogue.app.framework.view.faces.controller.AttributesController;
import rogue.app.framework.view.faces.controller.UsersController;

import java.io.Serializable;

/**
 * A copy of admin controllers, with the exception that these controllers are used for resource selection only.
 */
//@Named("ResourcePickers")
//@ViewScoped
public class ResourcePickerControllers implements Serializable
{
    //private AppLocalesController appLocalesController;
    private AttributesController systemAttributes;
//    private CountriesController countriesController;
//    private GeoCodesController geoCodesController;
//    private LanguagesController languagesController;
//    private TranslationsController translationsController;
    private UsersController usersController;

//    public AppLocalesController getAppLocalesController()
//    {
//        if (appLocalesController == null)
//        {
//            appLocalesController = new AppLocalesController();
//            initController(appLocalesController);
//        }
//        return appLocalesController;
//    }

    public AttributesController getSystemAttributes()
    {
        if (systemAttributes == null)
        {
            systemAttributes = new AttributesController();
            initController(systemAttributes);
        }
        return systemAttributes;
    }

//    public CountriesController getCountriesController()
//    {
//        if (countriesController == null)
//        {
//            countriesController = new CountriesController();
//            initController(countriesController);
//        }
//        return countriesController;
//    }
//
//    public GeoCodesController getGeoCodesController()
//    {
//        if (geoCodesController == null)
//        {
//            geoCodesController = new GeoCodesController();
//            initController(geoCodesController);
//        }
//        return geoCodesController;
//    }
//
//    public LanguagesController getLanguagesController()
//    {
//        if (languagesController == null)
//        {
//            languagesController = new LanguagesController();
//            initController(languagesController);
//        }
//        return languagesController;
//    }
//
//    public TranslationsController getTranslationsController()
//    {
//        if (translationsController == null)
//        {
//            translationsController = new TranslationsController();
//            initController(translationsController);
//        }
//        return translationsController;
//    }

    public UsersController getUsersController()
    {
        if (usersController == null)
        {
            usersController = new UsersController();
            initController(usersController);
        }
        return usersController;
    }

    private void initController(AbstractFacesController<?, ?> controller)
    {
        controller.setResourcePickerMode(true);
    }

}
