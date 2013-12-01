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
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.view.faces.validator.UniqueNameValidator;

import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Pages({
               @PageMetadata(displayName = "Attributes", outcome = "admin-attributes-listing",
                             path = "/framework/fragments/attributes/listing.xhtml", pageType = PageType.ITEMS_LISTING,
                             fragment = true),
               @PageMetadata(displayName = "Attribute Details", outcome = "admin-attribute-details",
                             path = "/framework/fragments/attributes/details.xhtml", pageType = PageType.ITEM_DETAILS,
                             fragment = true, type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "attributes", model = Attribute.class)
public class AttributesController extends EntityFacesController<Attribute>
{
    private UniqueNameValidator<Attribute> nameValidator;

    public AttributesController()
    {
        super();
    }

    public AttributesController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    public PersistentObject<? extends PersistentObject> getParentResource()
    {
        return (PersistentObject<?>) super.getParentResource();
    }

    @Override
    public void setParentResource(AppObject<?, ?> parentResource)
    {
        if (!Objects.equals(getParentResource(), parentResource))
        {
            this.nameValidator = null;
        }
        super.setParentResource(parentResource);
    }

    public UniqueNameValidator<Attribute> getNameValidator()
    {
        if (nameValidator == null)
        {
            Map<String, Object> queryParams = new HashMap<>(3);
            PersistentObject<?> parentObject = getParentResource();
            queryParams.put("nameSpace", parentObject != null ? PersistenceUtils.getAppObjectNameSpace(parentObject) :
                                         Attributes.SYSTEM_NAMESPACE);
            queryParams.put("appObjRefKey", parentObject != null ? parentObject.getId() : null);
            queryParams.put("layerKey", null); // base definitions only.

            nameValidator = new UniqueNameValidator<>("getName", "AttributeEntity.findByName.count",
                                                      "queryString", this, queryParams);
        }
        return nameValidator;
    }

    @Override
    public Attribute getNewInstance()
    {
        Attribute attribute = super.getNewInstance();
        attribute.setNameSpace(Attributes.SYSTEM_NAMESPACE);
        return attribute;
    }

    @Override
    public Attribute getNewInstance(AppObject<?, ?> parent) throws AppRuntimeException
    {
        Attribute attribute = super.getNewInstance(parent);
        attribute.setNameSpace(PersistenceUtils.getAppObjectNameSpace(parent));
        attribute.setAppObject((PersistentObject<?>) parent);
        return attribute;
    }

    @Override
    protected DataModel<Attribute> constructDataModel()
    {
        final Map<String, Object> queryParams = new HashMap<>(3);
        PersistentObject<?> parentObject = getParentResource();
        queryParams.put("nameSpace", parentObject != null ? PersistenceUtils.getAppObjectNameSpace(parentObject) :
                                     Attributes.SYSTEM_NAMESPACE);
        queryParams.put("appObjRefKey", parentObject != null ? parentObject.getId() : null);
        queryParams.put("layerKey", null); // base definitions only.

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
