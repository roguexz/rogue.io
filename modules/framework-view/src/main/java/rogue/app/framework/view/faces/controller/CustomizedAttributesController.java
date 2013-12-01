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

package rogue.app.framework.view.faces.controller;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Layer;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.view.faces.EntityConverter;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;

import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base controller for managing customized attributes.
 */
public abstract class CustomizedAttributesController extends AttributesController
{
    private static final EntityConverter<Layer> LAYER_ENTITY_CONVERTER = new EntityConverter<>(Layer.class);

    private UniqueNameValidator<Attribute> nameValidator;

    private List<Layer> layers;
    private Layer selectedLayer;

    /**
     * Creates a new instance of <code>CustomizedAttributesController</code>.
     */
    protected CustomizedAttributesController()
    {
        super();
    }

    /**
     * Creates a new instance of <code>CustomizedAttributesController</code> with the specified parent controller.
     *
     * @param parentController the parent with which this controller is associated.
     */
    public CustomizedAttributesController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    public void setParentResource(AppObject<?, ?> parentResource)
    {
        if (!Objects.equals(getParentResource(), parentResource))
        {
            super.setParentResource(parentResource);
            this.nameValidator = null;
            invalidateDataModel();
        }
    }

    /**
     * Get the customization layers that have been set for this controller.
     *
     * @return the customization layers that have been set for this controller.
     */
    public List<Layer> getLayers()
    {
        return layers;
    }

    /**
     * Set the customization layers that have been for this controller.
     *
     * @param layers the customization layers that have been for this controller.
     */
    public void setLayers(List<Layer> layers)
    {
        this.layers = layers;
    }

    /**
     * Get the entity converter for layers.
     *
     * @return the entity converter for layers.
     */
    public EntityConverter<Layer> getLayerConverter()
    {
        return LAYER_ENTITY_CONVERTER;
    }

    /**
     * Get the currently selected layer.
     *
     * @return the currently selected layer.
     */
    public Layer getSelectedLayer()
    {
        return selectedLayer;
    }

    /**
     * Set the currently selected layer.
     *
     * @param selectedLayer the currently selected layer.
     */
    public void setSelectedLayer(Layer selectedLayer)
    {
        this.selectedLayer = selectedLayer;
    }

    @Override
    public Attribute getNewInstance()
    {
        Attribute attribute = super.getNewInstance();
        attribute.setCustomizationLayer(getSelectedLayer());
        return attribute;
    }

    @Override
    public Attribute getNewInstance(AppObject<?, ?> parent) throws AppRuntimeException
    {
        Attribute attribute = super.getNewInstance(parent);
        attribute.setCustomizationLayer(getSelectedLayer());
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueNameValidator<Attribute> getNameValidator()
    {
        if (nameValidator == null)
        {
            Map<String, Object> queryParams = new HashMap<>(3);
            PersistentObject<?> parentObject = getParentResource();
            queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(parentObject));
            queryParams.put("appObjRefKey", parentObject != null ? parentObject.getId() : null);
            queryParams.put("layerKey", PersistenceUtils.getEntityKey(selectedLayer));

            nameValidator = new UniqueNameValidator<>("getName", "AttributeEntity.findByName.count",
                                                      "queryString", this, queryParams);
        }
        return nameValidator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataModel<Attribute> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(4);
        PersistentObject<?> parentObject = getParentResource();
        queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(parentObject));
        queryParams.put("appObjRefKey", parentObject != null ? parentObject.getId() : null);
        queryParams.put("layerKey", PersistenceUtils.getEntityKey(selectedLayer));

        String searchString = getSearchCommand().getSelectedResource();
        if (!StringUtils.isEmpty(searchString))
        {
            // Lowercase will cause issues with localized strings - Use wisely.
            queryParams.put("queryString", StringUtils.lowerCase(searchString) + "%");
        }

        final String resultsQuery = StringUtils.isEmpty(searchString) ? "AttributeEntity.findAttributes" :
                                    "AttributeEntity.searchByName";
        final String countQueryString = StringUtils.isEmpty(searchString) ? "AttributeEntity.findAttributes.count" :
                                        "AttributeEntity.searchByName.count";
        JpaQuery listingQuery = new JpaQuery(resultsQuery, true, queryParams);
        JpaQuery countQuery = new JpaQuery(countQueryString, true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }
}
