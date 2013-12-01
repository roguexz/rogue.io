package rogue.webapp.common.view.navigation.admin;

import rogue.app.store.internal.catalog.CatalogQualifier;
import rogue.app.store.view.faces.controller.BrandsController;
import rogue.app.store.view.faces.controller.ProductCategoriesController;
import rogue.app.store.view.faces.controller.ProductsController;
import rogue.webapp.common.view.bean.AdminControllers;

import javax.inject.Inject;

/**
 * Navigation nodes for the store administration
 */
@CatalogQualifier
public class CatalogNavigationNodes extends AdminNavNode
{
    @Inject
    public CatalogNavigationNodes(AdminControllers adminControllers)
    {
        super("Catalog", true, "", 3);
        addControllerRef("Products", adminControllers, ProductsController.class);
        addControllerRef("Brands", adminControllers, BrandsController.class);
        addControllerRef("Categories", adminControllers, ProductCategoriesController.class);
    }
}
