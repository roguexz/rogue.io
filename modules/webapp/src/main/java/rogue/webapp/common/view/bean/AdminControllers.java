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

package rogue.webapp.common.view.bean;

import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.i18n.view.faces.controller.*;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.AppObjectControllerCollection;
import rogue.app.framework.model.NamedAppObjectControllerCollection;
import rogue.app.framework.view.faces.AbstractFacesController;
import rogue.app.framework.view.faces.controller.*;
import rogue.app.store.view.faces.controller.BrandsController;
import rogue.app.store.view.faces.controller.ProductCategoriesController;
import rogue.app.store.view.faces.controller.ProductsController;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Bean for getting access to the various admin controllers. By centralizing access to the various controllers for the
 * UI, we can provide additional checks such as security and re-initialization etc.
 */
@Named("admin")
@SessionScoped
public class AdminControllers extends NamedAppObjectControllerCollection
        implements AppObjectControllerCollection, Serializable
{
    private Map<String, AppObjectController<?, ?>> controllerMap = new HashMap<>(5);

    public AdminControllers()
    {
        addController(new AttributesController());
        addController(new UsersController());

        addController(new CountriesController());
        addController(new LanguagesController());
        addController(new AppLocalesController());
        addController(new GeoCodesController());
        addController(new TranslationsController());

        addController(new ProductsController());
        addController(new ProductCategoriesController());
        addController(new BrandsController());
    }

    @Override
    public AppObjectController<?, ?> getController(String name)
    {
        return controllerMap.get(name);
    }


    private void addController(AbstractFacesController controller)
    {
        ControllerFor ann = controller.getClass().getAnnotation(ControllerFor.class);
        addController(ann.name(), controller);
    }

    private void addController(String name, AbstractFacesController controller)
    {
        controller.setControllerCollection(this);
        controllerMap.put(name, controller);
    }
}
