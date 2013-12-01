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

package rogue.app.framework.view.el;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.AppObjectControllerCollection;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.enterprise.context.RequestScoped;
import java.beans.FeatureDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * EL Resolver that provides quick access to some of the objects within the framework.
 * <p/>
 * <pre>
 * The resolver recognizes one implicit variable: <code>{@link #APP_PROPERTY_EL appProperty}</code>
 *
 * e.g., #{appProperty['site.name']}
 * </pre>
 * <p/>
 * In addition to the implicit variable, the resolver also handles the following cases on some of the framework objects,
 * namely:
 * <pre>
 *     #{app-object-instance['attr.key']}           - Access to an attribute of an AppObject that supports attributes.
 *     #{app-object-controller['idAsString']}       - Get an application object, identified by its {@link AppObject#getIdAsString() ID as a string}
 *                                                    and set it as the selected resource on the controller itself.
 *     #{app-object-controller-collection['name']}  - Get a named controller from the collection.
 * </pre>
 */
public class AppELResolver extends ELResolver
{
    public static final String APP_PROPERTY_EL = "appProperty";
    private static final List<FeatureDescriptor> FEATURE_DESCRIPTOR_LIST = new ArrayList<>(1);

    static
    {
        FeatureDescriptor fd = new FeatureDescriptor();
        fd.setDisplayName("Application Property");
        fd.setValue(ELResolver.TYPE, Attribute.class);
        fd.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, false);
    }

    private static final String SYSTEM_ATTRIBUTES_REQ_KEY = "sys_attr_map_key";
    private static final ThreadLocal<Map<String, Object>> SYSTEM_ATTRIBUTES_QUERY_MAP = new ThreadLocal<>();
    private static final JpaController<Attribute> ATTRIBUTES_CONTROLLER =
            JpaController.getController(Attribute.class);

    private Map<Class<?>, Map<String, Boolean>> resolvedMethodsMap = new HashMap<>();

    @Override
    public Object getValue(ELContext context, Object base, Object property)
    {
        if (property instanceof String)
        {
            String key = (String) property;

            if (base == null)
            {
                if (APP_PROPERTY_EL.equals(key))
                {
                    context.setPropertyResolved(true);
                    return this;
                }
                else
                {
                    // We do not handle any other property for base==null
                    return null;
                }
            }

            if (base instanceof AppELResolver)
            {
                // We are working with system properties here.
                StateHolder stateHolder = ScopedStateHolder.getStateHolder(RequestScoped.class);
                Map<String, String> cachedMap = stateHolder.get(SYSTEM_ATTRIBUTES_REQ_KEY);
                if (cachedMap == null)
                {
                    cachedMap = new HashMap<>();
                    stateHolder.put(SYSTEM_ATTRIBUTES_REQ_KEY, cachedMap);
                }

                String value = null;
                if (!cachedMap.containsKey(key))
                {
                    if (!StringUtils.isEmpty(key))
                    {
                        Map<String, Object> queryParams = SYSTEM_ATTRIBUTES_QUERY_MAP.get();
                        if (queryParams == null)
                        {
                            queryParams = new HashMap<>(4);
                            queryParams.put("nameSpace", Attributes.SYSTEM_NAMESPACE);
                            queryParams.put("appObjRefKey", null);
                            queryParams.put("layerKey", null);

                            SYSTEM_ATTRIBUTES_QUERY_MAP.set(queryParams);
                        }

                        queryParams.put("queryString", key.toLowerCase());
                        Attribute attr = ATTRIBUTES_CONTROLLER
                                .executeNamedQuerySingleResult("AttributeEntity.findByName", queryParams);

                        if (attr != null)
                        {
                            value = attr.getValue();
                        }

                        cachedMap.put(key, value);
                    }
                }
                else
                {
                    value = cachedMap.get(key);
                }

                if (value == null)
                {
                    value = key;
                }

                context.setPropertyResolved(true);
                return value;
            }
            else if (base instanceof Attributes)
            {
                if (isAccessorMethod(base.getClass(), key))
                {
                    return null;
                }

                Attributes attr = (Attributes) base;
                String value = attr.getAttribute(key);
                context.setPropertyResolved(true);
                return value;
            }
            else if (base instanceof AppObjectController)
            {
                if (isAccessorMethod(base.getClass(), key))
                {
                    return null;
                }

                AppObjectController controller = (AppObjectController) base;
                Object id = controller.parseId(key);
                if (id != null)
                {
                    AppObject appObject = controller.findById(id);
                    controller.setSelectedResource(appObject);
                    context.setPropertyResolved(true);
                    return controller;
                }
            }
            else if (base instanceof AppObjectControllerCollection)
            {
                if (isAccessorMethod(base.getClass(), key))
                {
                    return null;
                }

                AppObjectController controller = ((AppObjectControllerCollection) base).getController(key);
                if (controller != null)
                {
                    context.setPropertyResolved(true);
                    return controller;
                }
            }
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value)
    {
        // Do nothing.
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
    {
        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
    {
        return true;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base)
    {
        return FEATURE_DESCRIPTOR_LIST.iterator();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base)
    {
        return null;
    }

    private boolean isAccessorMethod(Class<?> clazz, String name)
    {
        if (clazz == null || name == null)
        {
            // We do not handle null objects in this resolver.
            return true;
        }

        Map<String, Boolean> nameMap = resolvedMethodsMap.get(clazz);
        if (nameMap == null)
        {
            nameMap = new HashMap<>();
            resolvedMethodsMap.put(clazz, nameMap);
        }

        if (!nameMap.containsKey(name))
        {
            Method[] methods = clazz.getMethods();
            String camelCase = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, name);
            // name.substring(0, 1).toUpperCase() + name.substring(1);
            String getter = "get" + camelCase;
            String setter = "set" + camelCase;
            String setter1 = "is" + camelCase;
            String[] accessors = {name, getter, setter, setter1};
            boolean found = false;
            for (Method m : methods)
            {
                for (String s : accessors)
                {
                    if (Objects.equals(m.getName(), s))
                    {
                        found = true;
                        break;
                    }
                }
            }
            nameMap.put(name, found);
        }

        return nameMap.get(name);
    }
}
