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

package rogue.app.framework.internal.persistence.attr;

import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.model.attr.CustomizedAttributes;
import rogue.app.framework.model.attr.Layer;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.RequestScoped;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>AttributesManager</code> helps with managing attributes for the associated application objects.
 */
public class AttributesManager implements Attributes, CustomizedAttributes, Serializable
{
    private static final String CLASS_NAME = AttributesManager.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final Layer BASE_DEFINITION_LAYER = null;

    private final PersistentObject<?> parent;
    private final List<String> attributeNames;
    private final Map<String, Map<Layer, Attribute>> attributesToPersist;
    private final Map<String, Map<Layer, Attribute>> attributesToDelete;

    private boolean backendAttrNamesLoaded = false;

    public AttributesManager(PersistentObject<?> parent)
    {
        if (parent == null)
        {
            throw new NullPointerException("Parent object reference cannot be null.");
        }

        this.parent = parent;
        attributeNames = new ArrayList<>();
        attributesToPersist = new HashMap<>(4);
        attributesToDelete = new HashMap<>(4);
    }

    ///
    /// Attributes - Method definitions
    ///

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAttributeNames()
    {
        ensureClassInheritance(Attributes.class);

        List<String> nameList;

        if (!parent.isMutable())
        {
            Map<String, Object> queryParams = new HashMap<>(3);
            queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(parent));
            queryParams.put("appObjRefKey", parent.getId());
            queryParams.put("layerKey", BASE_DEFINITION_LAYER); // base definitions only.
            nameList = JpaController.getController(Attribute.class)
                                    .executeNamedQueryListResult("AttributeEntity.getNames", 0, -1, queryParams);
        }
        else
        {
            if (!backendAttrNamesLoaded)
            {
                if (parent.getBaseResource() != null)
                {
                    List<String> baseNames = ((Attributes) parent.getBaseResource()).getAttributeNames();
                    for (String n : baseNames)
                    {
                        if (!attributeNames.contains(n))
                        {
                            attributeNames.add(n);
                        }
                    }

                    // Clear out any names that have been put in to the to-be-deleted collection.
                    for (Map.Entry<String, Map<Layer, Attribute>> entry : attributesToDelete.entrySet())
                    {
                        if (entry.getValue() != null && entry.getValue().containsKey(BASE_DEFINITION_LAYER))
                        {
                            attributeNames.remove(entry.getKey());
                        }
                    }
                }
                backendAttrNamesLoaded = true;
            }
            nameList = attributeNames;
        }

        return nameList;
    }

    public int getAttributeCount()
    {
        ensureClassInheritance(Attributes.class);

        if (parent.isMutable() && backendAttrNamesLoaded)
        {
            return attributeNames.size();
        }

        String KEY = parent.hashCode() + "_attributeKey";
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(RequestScoped.class);

        Long count = stateHolder.get(KEY);
        if (count == null)
        {
            Map<String, Object> queryParams = new HashMap<>(3);
            queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(parent));
            queryParams.put("appObjRefKey", parent.getId());
            queryParams.put("layerKey", BASE_DEFINITION_LAYER); // base definitions only.
            count = JpaController.getController(Attribute.class)
                                 .executeNamedQuerySingleResult("AttributeEntity.getNames.count", queryParams);

            if (count == null)
            {
                count = 0L;
            }

            if (parent.isMutable())
            {
                // Add & deduct the attributes to persist / delete
                if (parent.getId() == null)
                {
                    count = (long) attributesToPersist.size();
                }
                else
                {
                    count -= attributesToDelete.size();
                    for (Map.Entry<String, Map<Layer, Attribute>> entry : attributesToPersist.entrySet())
                    {
                        Map<Layer, Attribute> custMap = entry.getValue();
                        if (custMap != null && custMap.containsKey(BASE_DEFINITION_LAYER))
                        {
                            Attribute attr = custMap.get(BASE_DEFINITION_LAYER);
                            if (attr != null && attr.getId() == null)
                            {
                                count++;
                            }
                        }
                    }
                }
            }

            stateHolder.put(KEY, count);
        }

        return count.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttributeDefined(String name)
    {
        return isAttributeCustomized(name, BASE_DEFINITION_LAYER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String name)
    {
        return getAttribute(name, BASE_DEFINITION_LAYER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String name, String value)
    {
        setAttribute(name, value, BASE_DEFINITION_LAYER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String name)
    {
        removeAttribute(name, BASE_DEFINITION_LAYER);
    }

    ///
    /// CustomizedAttributes - Method definitions
    ///

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttributeCustomized(String name, Layer layer)
    {
        ensureClassInheritance(layer == BASE_DEFINITION_LAYER ? Attributes.class : CustomizedAttributes.class);

        boolean defined = false;
        if (!parent.isMutable())
        {
            defined = getAttributeObject(name, layer) != null;
        }
        else
        {
            if (parent.getId() != null)
            {
                // Check if the attribute is already present in the data store.
                defined = getAttributeObject(name, layer) != null;
            }

            if (defined)
            {
                // Check to see if the attribute has been marked for deletion
                if (getLocalAttribute(name, layer, attributesToDelete) != null)
                {
                    defined = false;
                }
            }
            else
            {
                // Check if this is a new local attribute that has not yet been persisted.
                if (getLocalAttribute(name, layer, attributesToPersist) != null)
                {
                    defined = true;
                }
            }
        }

        return defined;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAttribute(String name, Layer layer)
    {
        ensureClassInheritance(layer == BASE_DEFINITION_LAYER ? Attributes.class : CustomizedAttributes.class);

        Attribute attribute;
        if (!parent.isMutable())
        {
            attribute = getAttributeObject(name, layer);
        }
        else
        {
            attribute = getLocalAttribute(name, layer, attributesToPersist);

            if (attribute == null && parent.getId() != null
                    // Check if the attribute has been targeted for deletion or not
                    && getLocalAttribute(name, layer, attributesToDelete) == null)
            {
                // Try and acquire it from the backend.
                attribute = getAttributeObject(name, layer);
            }
        }

        return attribute != null ? attribute.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setAttribute(String name, String value, Layer layer)
    {
        setAttribute(name, value, null, layer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute(String name, Layer layer)
    {
        ensureClassInheritance(layer == BASE_DEFINITION_LAYER ? Attributes.class : CustomizedAttributes.class);

        if (!parent.isMutable())
        {
            // Do not entertain. Just return.
            return;
        }

        if (name == null)
        {
            return;
        }

        // 1. Get hold of the attribute object that should be deleted
        Attribute attribute = null;

        if (parent.getId() != null)
        {
            attribute = getAttributeObject(name, layer);
        }

        if (attribute != null)
        {
            putLocalAttribute(name, layer, attributesToDelete, attribute);
        }

        removeLocalAttribute(name, layer, attributesToPersist);

        if (BASE_DEFINITION_LAYER == layer)
        {
            attributeNames.remove(name);
        }
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(String name, String value, String description, Layer layer)
    {
        ensureClassInheritance(layer == BASE_DEFINITION_LAYER ? Attributes.class : CustomizedAttributes.class);

        if (!parent.isMutable())
        {
            // Do not entertain. Just return.
            return;
        }

        if (StringUtils.isEmpty(name))
        {
            throw new IllegalArgumentException("Attribute name cannot be null.");
        }

        // 0. A customization entry is not allowed before creation of the base entry
        if (layer != BASE_DEFINITION_LAYER && !isAttributeDefined(name))
        {
            throw new AppRuntimeException(
                    "Attribute customization not permitted. There is no base definition. Ensure that the base attribute has been defined - " + name);
        }

        // 1. Remove it from the to-be-deleted collection
        removeLocalAttribute(name, layer, attributesToDelete);

        // 2. Find / create the attribute object
        Attribute attribute = getAttributeObject(name, layer);

        if (attribute == null)
        {
            attribute = getLocalAttribute(name, layer, attributesToPersist);
        }

        if (attribute == null)
        {
            attribute = JpaController.getController(Attribute.class).getNewInstance();
            attribute.setName(name);
            attribute.setNameSpace(PersistenceUtils.getAppObjectNameSpace(parent));
            attribute.setAppObject(parent);
        }
        else
        {
            attribute = attribute.getMutableInstance();
        }

        attribute.setDescription(description);
        attribute.setValue(value);

        // 3. Add it to the to-be-persisted collection
        putLocalAttribute(name, layer, attributesToPersist, attribute);

        // 4. Update the name in the attribute names list
        if (!attributeNames.contains(name))
        {
            attributeNames.add(name);
        }
    }

    public boolean isDirty()
    {
        return !attributesToDelete.isEmpty() || !attributesToPersist.isEmpty();
    }

    /**
     * Get hold of this object's associated named attribute, identified by the given <code>name</code> and customized
     * at the specified <code>layer</code>. This operation runs against the data store directly.
     *
     * @param name  name of the attribute.
     * @param layer customization layer.
     * @return the attribute object if found.
     */
    public Attribute getAttributeObject(String name, Layer layer)
    {
        ensureClassInheritance(layer == null ? Attributes.class : CustomizedAttributes.class);

        if (name == null)
        {
            return null;
        }

        Map<String, Object> queryParams = new HashMap<>(4);
        queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(parent));
        queryParams.put("appObjRefKey", parent.getId());
        queryParams.put("layerKey", PersistenceUtils.getEntityKey(layer));
        queryParams.put("queryString", name.toLowerCase());

        return JpaController.getController(Attribute.class)
                            .executeNamedQuerySingleResult("AttributeEntity.findByName", queryParams);
    }

    public void persist()
    {
        final String METHOD_NAME = "persist";
        ensureClassInheritance(Attributes.class);

        if (parent.getId() == null)
        {
            throw new AppRuntimeException(
                    "Attributes can be associated once the base object has been persisted. Persist the current object before attempting this operation.");
        }

        // handle the objects that need to be persisted
        JpaController<Attribute> controller = JpaController.getController(Attribute.class);
        for (Map.Entry<String, Map<Layer, Attribute>> e : attributesToPersist.entrySet())
        {
            for (Map.Entry<Layer, Attribute> f : e.getValue().entrySet())
            {
                Layer layer = f.getKey();
                Attribute attr = f.getValue();

                StringBuilder builder = new StringBuilder(100);
                builder.append("[").append(parent.toString()).append(", ").append(parent.getId()).append("]\n");
                boolean logException = false;

                if (!Objects.equals(PersistenceUtils.getAppObjectNameSpace(parent),
                                    attr.getNameSpace()))
                {
                    builder.append(String.format(
                            "\tNamespace mismatch [expected: %s, actual: %s]. This indicates a code bug.\n",
                            PersistenceUtils.getAppObjectNameSpace(parent), attr.getNameSpace()));
                    logException = true;
                }

                if (!Objects.equals(parent.getId(), PersistenceUtils.getEntityKey(attr.getAppObject())))
                {
                    builder.append(String.format(
                            "\tApplication Object Reference mismatch [expected: %s, actual: %s]. This indicates a code bug.\n",
                            parent.getId(), PersistenceUtils.getEntityKey(attr.getAppObject())));
                    logException = true;
                }

                if (attr.getCustomizationLayer() != layer)
                {
                    builder.append(String.format(
                            "\tCustomization layer mismatch [expected: %s, actual: %s]. This indicates a code bug.\n",
                            layer,
                            attr.getCustomizationLayer()));
                    logException = true;
                }

                if (logException)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Illegal state",
                                new AppRuntimeException(builder.toString()));
                }

                controller.save(attr);
            }
        }

        // handle the objects that need to be deleted
        for (Map.Entry<String, Map<Layer, Attribute>> e : attributesToDelete.entrySet())
        {
            List<Key> keys = new ArrayList<>();
            for (Map.Entry<Layer, Attribute> f : e.getValue().entrySet())
            {
                if (f.getKey() == BASE_DEFINITION_LAYER)
                {
                    // Mark all customization for deletion.
                    Map<String, Object> queryParams = new HashMap<>(3);
                    queryParams.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(parent));
                    queryParams.put("appObjRefKey", parent.getId());
                    queryParams.put("queryString", e.getKey().toLowerCase());

                    // Delete all customizations also.
                    List<Attribute> attributes =
                            controller.executeNamedQueryListResult("AttributeEntity.findByName.allLayers",
                                                                   0, -1, queryParams);
                    if (!attributes.isEmpty())
                    {
                        for (Attribute attr : attributes)
                        {
                            Key k = PersistenceUtils.getEntityKey(attr);
                            if (k != null && !keys.contains(k))
                            {
                                keys.add(k);
                            }
                        }
                    }
                }
                else
                {
                    if (f.getValue() != null && f.getValue().getId() != null)
                    {
                        keys.add(f.getValue().getId());
                    }
                }

                if (!keys.isEmpty())
                {
                    controller.deleteById(keys.toArray(new Key[keys.size()]));
                }
            }
        }
    }

    private Attribute getLocalAttribute(String name, Layer layer, Map<String, Map<Layer, Attribute>> localAttrMap)
    {
        if (localAttrMap == null)
        {
            throw new IllegalArgumentException("Local attribute map should never be null");
        }

        Attribute attribute = null;
        Map<Layer, Attribute> customizationMap = localAttrMap.get(name);
        if (customizationMap != null)
        {
            attribute = customizationMap.get(layer);
        }

        return attribute;
    }

    private void putLocalAttribute(String name, Layer layer, Map<String, Map<Layer, Attribute>> localAttrMap,
                                   Attribute attribute)
    {
        if (localAttrMap == null)
        {
            throw new IllegalArgumentException("Local attribute map should never be null");
        }

        Map<Layer, Attribute> customizationMap = localAttrMap.get(name);
        if (customizationMap == null)
        {
            customizationMap = new TreeMap<>(LAYER_COMPARATOR);
            localAttrMap.put(name, customizationMap);
        }
        customizationMap.put(layer, attribute);
    }

    private void removeLocalAttribute(String name, Layer layer, Map<String, Map<Layer, Attribute>> localAttrMap)
    {
        if (localAttrMap == null)
        {
            throw new IllegalArgumentException("Local attribute map should never be null");
        }

        Map<Layer, Attribute> customizationMap = localAttrMap.get(name);
        if (customizationMap != null)
        {
            customizationMap.remove(layer);
        }
    }

    private void ensureClassInheritance(Class<?> requiredParent)
    {
        if (requiredParent != null && !requiredParent.isAssignableFrom(parent.getClass()))
        {
            throw new AppRuntimeException(String.format("Entity object %s does not inherit from %s.",
                                                        getClass().getName(), requiredParent.getName()));
        }
    }

    private static final Comparator<Layer> LAYER_COMPARATOR = new Comparator<Layer>()
    {
        @Override
        public int compare(Layer layer1, Layer layer2)
        {
            return layer1 == null ? (layer2 == null ? 0 : -1) :
                   layer1.getName().compareTo(layer2 == null ? null : layer2.getName());
        }
    };

    ///
    /// Overrides for equals & hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AttributesManager))
        {
            return false;
        }

        AttributesManager that = (AttributesManager) o;

        if (!attributesToDelete.equals(that.attributesToDelete))
        {
            return false;
        }
        if (!attributesToPersist.equals(that.attributesToPersist))
        {
            return false;
        }
        // This causes a recursion. So do not include the parent.
//        if (parent != null ? !parent.equals(that.parent) : that.parent != null)
//        {
//            return false;
//        }

        return true;
    }

    @Override
    public int hashCode()
    {
        // int result = parent != null ? parent.hashCode() : 0;
        // result = 31 * result + attributesToPersist.hashCode();
        int result = attributesToPersist.hashCode();
        result = 31 * result + attributesToDelete.hashCode();
        return result;
    }
}
