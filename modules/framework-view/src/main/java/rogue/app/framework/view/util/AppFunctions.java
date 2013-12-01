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

package rogue.app.framework.view.util;

import com.google.appengine.api.datastore.KeyFactory;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.primefaces.component.api.Widget;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines the JSF EL Functions exposed in this application.
 */
public final class AppFunctions
{
    private static final String CLASS_NAME = AppFunctions.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final ThreadLocal<Map<String, Object>> SYSTEM_ATTRIBUTES_QUERY_MAP = new ThreadLocal<>();
    private static final JpaController<Attribute> ATTRIBUTES_CONTROLLER =
            JpaController.getController(Attribute.class);
    private static final String SYSTEM_ATTRIBUTES_REQ_KEY = "sys_attr_map_key";


    private AppFunctions()
    {
    }

    public static String truncate(String input, int length)
    {
        if (input != null && input.length() > length)
        {
            int diff = input.length() - (length + 3); // 3 for the ellipsis ...
            if (diff < 0)
            {
                length += diff;
            }
            input = input.substring(0, length);
            input = new StringBuilder(input).append("...").toString();
        }
        return input;
    }

    /**
     * Try and get the formatted display string which is appropriate for the UI.
     */
    public static String getFormattedString(Object object)
    {
        if (object != null)
        {
            if (object instanceof Formattable)
            {
                return String.format("%s", object);
            }
            return object.toString();
        }
        return null;
    }

    public static <E> List<E> subList(List<E> list, int start, int count)
    {
        List<E> subList = null;
        if (list != null && start < list.size())
        {
            if (start + count > list.size())
            {
                count = list.size() - start;
            }

            subList = list.subList(start, start + count);
        }
        return subList;
    }

    /**
     * Because f:convertDateTime does not apply to labels of selectItems.
     *
     * @param date    the date to format
     * @param pattern the pattern to format to
     * @return a formatted string.
     */
    public static String formatDate(Date date, String pattern)
    {
        if (date == null)
        {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        return sdf.format(date);
    }

    /**
     * Because writing a converter tag exclusively for this is a pain.
     *
     * @param dateTime the date time object
     * @param pattern  the pattern to translate to.
     * @return a formatted string.
     */
    public static String formatDateTime(DateTime dateTime, String pattern)
    {
        return dateTime == null ? null : dateTime.toString(pattern);
    }

    public static Date convertToDate(DateTime dateTime)
    {
        return dateTime == null ? null : dateTime.toDate();
    }

    public static String getApplicationProperty(String key, String defaultValue)
    {
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
            value = defaultValue;
        }

        return value;
    }


    public static Object[] getEnumValues(String enumClassName)
    {
        final String METHOD_NAME = "getEnumValues";
        try
        {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(enumClassName);
            if (Enum.class.isAssignableFrom(clazz))
            {
                return clazz.getEnumConstants();
            }
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to load class", e);
        }
        return new Object[0];
    }

    public static Object getEnum(String enumClassName, String enumString)
    {
        final String METHOD_NAME = "getEnum";
        try
        {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(enumClassName);
            if (Enum.class.isAssignableFrom(clazz))
            {
                return Enum.valueOf(clazz, enumString);
            }
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to load class", e);
        }
        return null;
    }

    public static Map<String, Object> getAppBundle(String bundleName)
    {
        final String METHOD_NAME = "getAppBundle";
        String KEY = METHOD_NAME + "_" + bundleName;
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        Map<String, Object> bundleMap;
        bundleMap = stateHolder.get(KEY);

        if (bundleMap == null && !StringUtils.isEmpty(bundleName))
        {
            Locale locale = Locale.ENGLISH;
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null)
            {
                locale = context.getViewRoot().getLocale();
            }

            try
            {
                // bundleMap = new AppResourceBundle(bundleName, locale); TODO
                bundleMap = new HashMap<>();
            }
            catch (MissingResourceException ex)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to load resource", ex);
                bundleMap = Collections.emptyMap();
            }

            stateHolder.put(KEY, bundleMap);
        }
        return bundleMap;
    }

    /**
     * Utility method to get hold of all global faces messages.
     *
     * @return a list of global faces messages.
     */
    public static List<FacesMessage> getGlobalMessageList()
    {
        List<FacesMessage> msgList = Collections.emptyList();
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null)
        {
            msgList = ctx.getMessageList(null);
        }
        return msgList;
    }

    /**
     * Utility method to mark a faces message as rendered.
     *
     * @param message the faces message object.
     */
    public static void markFacesMessageRendered(FacesMessage message)
    {
        if (message != null)
        {
            message.rendered();
        }
    }

    public static String resolveWidgetVariable(UIComponent component)
    {
        String widgetVar = null;
        if (component != null && component instanceof Widget)
        {
            widgetVar = ((Widget) component).resolveWidgetVar();
        }
        return widgetVar;
    }

    public static UIComponent nextUIComponent(UIComponent component)
    {
        UIComponent next = null;
        if (component != null)
        {
            UIComponent parent = component.getParent();
            if (parent != null)
            {
                List<UIComponent> children = parent.getChildren();
                int idx = children.indexOf(component);
                if (idx + 1 < children.size())
                {
                    next = children.get(idx + 1);
                }
            }
        }
        return next;
    }

    public static String formatInterval(Interval interval)
    {
        return String.format("%s, between %s and %s", interval.getStart().toString("E MMM d"),
                             interval.getStart().toString("hh:mm a"), interval.getEnd().toString("hh:mm a"));
    }
}
