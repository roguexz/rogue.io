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
import com.google.appengine.api.datastore.KeyFactory;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaController;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.io.Serializable;

/**
 * Base generic class for supporting a JSF converter around the JPA entity objects that we have.
 */
public class EntityConverter<E extends PersistentObject<E>> implements Converter, Serializable
{
    private Class<E> appObjClassRef;

    /**
     * Constructs a new instance with the given arguments.
     *
     * @param appObjClassRef the application object class reference.
     */
    public EntityConverter(Class<E> appObjClassRef)
    {
        this.appObjClassRef = appObjClassRef;
    }


    /**
     * Converts the supplied string value to an appropriate object (if applicable).
     *
     * @param context   the FacesContext
     * @param component the UIComponent
     * @param value     the value, which in our case would be the key of the entity.
     * @return the Entity as an object
     */
    public E getAsObject(FacesContext context, UIComponent component, String value)
    {
        if (value == null || value.trim().length() == 0)
        {
            return null;
        }

        try
        {
            Key key = KeyFactory.stringToKey(value);
            return JpaController.getController(appObjClassRef).findById(key);
        }
        catch (IllegalArgumentException ex)
        {
            // Failed to construct the key.
            return null;
        }
    }

    /**
     * Get the String representation of the object.
     *
     * @param context   the FacesContext
     * @param component the UIComponent
     * @param value     the object whose string representation is requested for.
     * @return a string representation of the Entity's key.
     */
    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext context, UIComponent component, Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (appObjClassRef.isAssignableFrom(value.getClass()))
        {
            return KeyFactory.keyToString(((E) value).getId());
        }
        else
        {
            throw new IllegalArgumentException(
                    "Object " + value + " if of type " + value.getClass().getName() +
                            "; Expected type: " + appObjClassRef.getName());
        }
    }
}
