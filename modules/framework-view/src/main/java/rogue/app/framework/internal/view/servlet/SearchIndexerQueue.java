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

package rogue.app.framework.internal.view.servlet;

import com.google.appengine.api.datastore.Key;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.search.SearchController;
import rogue.app.framework.view.servlet.AppQueueServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>SearchIndexQueue</code> servlet handles queued up search indexing requests.
 * <p></p>
 * The servlet looks for two parameters in the request URL.
 * <pre>
 *     appObjClass - the name of the app object class, whose index needs to be updated
 *     keys        - a comma separated list of keys that need to be indexed.
 *     remove      - a boolean attribute identifying if the keys should be removed from the index.
 *                   The default value is false.
 * </pre>
 */
public class SearchIndexerQueue extends AppQueueServlet
{
    private static final String CLASS_NAME = SearchIndexerQueue.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static final String REQ_PARAM_CLASS_NAME = "appObjClass";
    public static final String REQ_PARAM_KEYS = "keys";
    public static final String REQ_PARAM_REMOVE = "remove";

    @Override
    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        final String METHOD_NAME = "processRequest";

        String appObjClassName = StringUtils.trim(request.getParameter(REQ_PARAM_CLASS_NAME));
        if (StringUtils.isEmpty(appObjClassName))
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Class name was null or empty. Aborting operation.");
            return;
        }
        String keys = StringUtils.trim(request.getParameter(REQ_PARAM_KEYS));

        if (StringUtils.isEmpty(keys))
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Keys was empty. Aborting operation.");
            return;
        }

        boolean remove = Boolean.parseBoolean(request.getParameter(REQ_PARAM_REMOVE));

        try
        {
            Class c = Thread.currentThread().getContextClassLoader().loadClass(appObjClassName);
            if (AppObject.class.isAssignableFrom(c))
            {
                SearchController searchController = SearchController.getSearchController(c);
                JpaController jpaController = JpaController.getController(c);

                if (jpaController == null)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                "Unable to acquire JpaController for class: " + appObjClassName);
                    return;
                }

                if (searchController == null)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                "Unable to acquire SearchController for class: " + appObjClassName);
                    return;
                }

                String[] tmp = keys.split(",");
                List<Key> keyList = new ArrayList<Key>();
                for (String s : tmp)
                {
                    s = StringUtils.trim(s);
                    if (StringUtils.isEmpty(s))
                    {
                        continue;
                    }

                    try
                    {
                        keyList.add(jpaController.constructKey(Long.parseLong(s)));
                    }
                    catch (Exception e)
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                    "Unable to construct and add key for class: " + appObjClassName, e);
                    }
                }

                if (remove)
                {
                    searchController.remove(keyList.toArray(new Key[keyList.size()]));
                }
                else
                {
                    searchController.index(keyList.toArray(new Key[keyList.size()]));
                }
            }
            else
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Loaded class cannot be assigned from AppObject: " + appObjClassName);
            }
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to load class: " + appObjClassName, e);
        }
        catch (AppRuntimeException e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to complete operation.", e);
        }
    }
}
