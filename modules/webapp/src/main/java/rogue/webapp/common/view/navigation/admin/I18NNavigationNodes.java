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

package rogue.webapp.common.view.navigation.admin;

import rogue.app.framework.i18n.view.faces.controller.*;
import rogue.app.framework.internal.i18n.I18NQualifier;
import rogue.webapp.common.view.bean.AdminControllers;

import javax.inject.Inject;

/**
 * The admin navigation model for I18N module.
 */
@I18NQualifier
public class I18NNavigationNodes extends AdminNavNode
{
    @Inject
    public I18NNavigationNodes(AdminControllers adminControllers)
    {
        super("Localization", true, null, 1);
        addControllerRef("Countries", adminControllers, CountriesController.class);
        addControllerRef("Languages", adminControllers, LanguagesController.class);
        addControllerRef("Locales", adminControllers, AppLocalesController.class);
        addControllerRef("Translations", adminControllers, TranslationsController.class);
        addControllerRef("Geo Codes", adminControllers, GeoCodesController.class);
    }
}
