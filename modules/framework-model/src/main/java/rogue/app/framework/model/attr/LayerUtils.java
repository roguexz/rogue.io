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

package rogue.app.framework.model.attr;

import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.NamedResource;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helps with the management of layers.
 */
public final class LayerUtils implements Serializable
{

    private LayerUtils()
    {
    }

    /**
     * Find, or create, a layer object associated with the given application object.
     *
     * @param appObject the application object
     * @return the layer object, or null if the specified argument is null.
     */
    public static Layer getLayerForObject(PersistentObject<?> appObject)
    {
        if (appObject == null)
        {
            return null;
        }

        if (appObject.getId() == null)
        {
            throw new AppRuntimeException(
                    "Layer objects can be created for persisted objects only. Persist the base object and try again.");
        }

        JpaController<Layer> controller = JpaController.getController(Layer.class);

        Map<String, Object> params = new HashMap<>(1);
        params.put("baseRefKey", appObject.getId());

        Layer layer = JpaController.getController(Layer.class)
                                   .executeNamedQuerySingleResult("LayerEntity.findByBaseRef", params);

        if (layer == null)
        {
            layer = controller.getNewInstance();

            if (appObject instanceof NamedResource)
            {
                NamedResource nr = (NamedResource) appObject;
                layer.setName("Layer for: " + nr.getName());
                layer.setDescription(nr.getDescription());
            }
            else
            {
                layer.setName("Layer for: " + appObject.toString());
            }
            layer.setBaseReference(appObject);

            controller.save(layer);
        }

        return layer;
    }

    /**
     * Get a list of layer objects, creating them if need be.
     *
     * @param appObjects list of application objects for which layers are required.
     * @return a list of layer objects.
     * @throws AppRuntimeException if any of the application objects have not been persisted before being passed in.
     */
    public static List<Layer> getLayers(List<? extends PersistentObject> appObjects)
    {
        List<Layer> layers = new ArrayList<>();
        for (PersistentObject o : appObjects)
        {
            Layer l = getLayerForObject(o);
            if (l != null)
            {
                layers.add(l);
            }
        }
        return layers;
    }
}
