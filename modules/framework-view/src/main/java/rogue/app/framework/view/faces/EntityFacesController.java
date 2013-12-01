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

package rogue.app.framework.view.faces;

import com.google.appengine.api.datastore.Key;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.view.faces.controller.AttributesController;
import rogue.app.framework.view.faces.model.QueryDataModel;

import javax.faces.model.DataModel;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for handling the view operations pertaining to a JPA entity.
 */
public abstract class EntityFacesController<T extends PersistentObject<T>> extends AbstractFacesController<T, Key>
{
    private static final String CLASS_NAME = EntityFacesController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private EntityConverter<T> converter;

    protected EntityFacesController()
    {
        super();
        initClass(null);
    }

    protected EntityFacesController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
        initClass(null);
    }

    private void initClass(String prefix)
    {
        this.converter = new EntityConverter<>(getAppObjClassRef());

        String refClassName = getAppObjClassRef().getName() + "_";
        if (prefix != null)
        {
            refClassName = refClassName + prefix + "_";
        }

        // Setup the attributes controller if applicable.
        if (isAttributesSupported())
        {
            AttributesController attributesController = new AttributesController(this);
            addChildController(attributesController);
        }
    }

    /**
     * Get a reference to the JpaController associated with the parametrized type of this class.
     *
     * @return reference to the JpaController associated with the parametrized type of this class.
     */
    public JpaController<T> getDelegate()
    {
        return JpaController.getController(getAppObjClassRef());
    }

    /**
     * Identifies if the underlying application object is capable of supporting attributes or not.
     *
     * @return true if attributes are supported, false otherwise.
     */
    public boolean isAttributesSupported()
    {
        return Attributes.class.isAssignableFrom(getAppObjClassRef()) || Attributes.class
                .isAssignableFrom(getDelegate().getEntityClass());
    }


    /**
     * Invalidate the present data model.
     */
    public void invalidateDataModel()
    {
        super.invalidateDataModel();
    }

    /**
     * Get the FacesConverter instance for the entity managed by this controller.
     *
     * @return the FacesConverter instance for the entity managed by this controller.
     */
    public EntityConverter<T> getConverter()
    {
        return converter;
    }

    /**
     * Convenience method for getting hold of all items.
     *
     * @return a list of items.
     */
    public List<T> getItems()
    {
        final String METHOD_NAME = "getItems";
        DataModel<T> dataModel = constructDataModel();

        if (dataModel != null && dataModel instanceof QueryDataModel)
        {
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                        "Fetching data by querying the QueryDataModel implementation.");
            return ((QueryDataModel<T>) dataModel).load(0, -1, null, null, null);
        }

        if (dataModel == null)
        {
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Received null data model. Returning an empty list.");
            return Collections.emptyList();
        }

        LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                    "Fetching data by querying findEntities(0,-1) on the controller.");
        return getDelegate().findEntities(0, -1);
    }

}
