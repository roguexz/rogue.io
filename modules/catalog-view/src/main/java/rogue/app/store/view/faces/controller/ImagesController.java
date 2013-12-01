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

package rogue.app.store.view.faces.controller;

import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.model.BinaryResource;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.view.ActionType;
import rogue.app.framework.view.PageMetadata;
import rogue.app.framework.view.PageType;
import rogue.app.framework.view.Pages;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.controller.BinaryResourcesController;
import rogue.app.store.catalog.model.ImageType;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.List;

/**
 * A binary resource controller that manages images.
 */
@Pages({
               @PageMetadata(displayName = "Images", pageType = PageType.ITEMS_LISTING,
                             path = "/catalog/fragments/image/listing.xhtml", fragment = true),
               @PageMetadata(displayName = "Image Details", pageType = PageType.ITEM_DETAILS,
                             path = "/catalog/fragments/image/details.xhtml", fragment = true,
                             type = {ActionType.CREATE, ActionType.VIEW, ActionType.UPDATE})
       })
@ControllerFor(name = "images", model = BinaryResource.class)
public class ImagesController extends BinaryResourcesController
{
    private List<String> imageTypes;

    public ImagesController()
    {
        super();
    }

    public ImagesController(
            EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    /**
     * Get the set of image types that are supported.
     *
     * @return the set of image types that are supported.
     */
    public List<String> getImageTypes()
    {
        if (imageTypes == null)
        {
            ImageType[] types = ImageType.values();
            imageTypes = new ArrayList<>(types.length);
            for (ImageType type : types)
            {
                imageTypes.add(type.name());
            }
        }
        return imageTypes;
    }

    /**
     * Validator for the image type.
     *
     * @param context   the FacesContext instance
     * @param component the UIComponent
     * @param value     the currently set value
     * @throws ValidatorException if the validation fails.
     */
    public void validateImageType(FacesContext context, UIComponent component, Object value) throws ValidatorException
    {
        if (value instanceof String)
        {
            BinaryResource resource = getSelectedResource();
            String type = (String) value;
            if (resource != null && (ImageType.DEFAULT.name().equals(type) || ImageType.THUMBNAIL.name().equals(type)))
            {
                List<BinaryResource> items = getItems();
                BinaryResource existingDefaultItem = null;
                for (BinaryResource item : items)
                {
                    if (type.equals(item.getResourceType()))
                    {
                        existingDefaultItem = item;
                        break;
                    }
                }

                if (existingDefaultItem != null && !existingDefaultItem.getId().equals(resource.getId()))
                {
                    FacesMessage message = new FacesMessage(
                            "There already is another image that has been marked as the " + type + " [ " + existingDefaultItem
                                    .getName() + " ]. You will need to unset that one before setting this one as the " + type);
                    throw new ValidatorException(message);
                }
            }
        }
    }
}
