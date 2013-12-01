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

package rogue.webapp.common.view;

import rogue.app.framework.model.BinaryResource;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;
import rogue.app.store.catalog.model.Product;

import javax.enterprise.context.RequestScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bunch of utility functions.
 */
public final class SiteFunctions
{
    private SiteFunctions()
    {
    }

    /**
     * Get an image associated with a product.
     *
     * @param product the product object
     * @param type    the image type - these are values from {@link rogue.app.store.catalog.model.ImageType}
     * @return the image object, if present.
     */
    public static BinaryResource getImage(Product product, String type)
    {
        List<BinaryResource> list = getImages(product, type);
        return !list.isEmpty() ? list.get(0) : null;
    }

    /**
     * Get a list of images associated with a product.
     *
     * @param product the product object
     * @param type    the image type - these are values from {@link rogue.app.store.catalog.model.ImageType}
     * @return a list of images associated with a product.
     */
    public static List<BinaryResource> getImages(Product product, String type)
    {
        StateHolder holder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        List<BinaryResource> resources = null;

        if (holder != null && product != null && type != null)
        {
            final String KEY = product.getIdAsString() + "_image_" + type;
            resources = holder.get(KEY);
            if (resources == null)
            {
                JpaController<BinaryResource> binariesController = JpaController.getController(BinaryResource.class);
                Map<String, Object> queryParams = new HashMap<>(3);
                queryParams.put("appObjRefKey", product.getId());
                queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(product));
                queryParams.put("resourceType", type);
                JpaQuery listingQuery = new JpaQuery("BinaryResourceEntity.findResourcesByType", true, queryParams);
                listingQuery.setMaxResults(-1);
                resources = binariesController.find(listingQuery);

                holder.put(KEY, resources);
            }
        }
        return resources;
    }
}
