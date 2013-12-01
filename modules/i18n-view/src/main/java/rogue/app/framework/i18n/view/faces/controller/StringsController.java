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

import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.i18n.model.AppLocale;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.LayerUtils;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.controller.CustomizedAttributesController;

import java.util.List;

/**
 * Controller for managing the translation strings.
 */
@Pages({
               @PageMetadata(displayName = "Translation Strings",
                             path = "/i18n/fragments/translations/strings/listing.xhtml",
                             pageType = PageType.ITEMS_LISTING, fragment = true),
               @PageMetadata(displayName = "Translation String Details",
                             path = "/i18n/fragments/translations/strings/details.xhtml",
                             pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "strings", model = Attribute.class)
public class StringsController extends CustomizedAttributesController
{

    /**
     * Creates a new instance of <code>StringsController</code>.
     */
    public StringsController()
    {
        super();
    }

    /**
     * Creates a new instance of <code>StringsController</code> with the specified parent controller.
     *
     * @param parentController the parent with which this controller is associated.
     */
    public StringsController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    /**
     * In addition to setting the parent, this method also reloads the locales.
     */
    @Override
    public void setParentResource(AppObject<?, ?> parentResource)
    {
        super.setParentResource(parentResource);

        // Reload the locales as well.
        List<AppLocale> appLocales = JpaController.getController(AppLocale.class).find(null);
        setLayers(LayerUtils.getLayers(appLocales));
    }
}
