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

package rogue.app.framework.persistence;

import com.google.appengine.api.datastore.Key;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for handling persistence requests.
 */
public final class PersistenceUtils
{
    private static final String CLASS_NAME = PersistenceUtils.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final EntityManagerFactory emfInstance =
            Persistence.createEntityManagerFactory("transactions-optional");

    private PersistenceUtils()
    {
    }

    /**
     * Get the namespace that can be used for Attributes & BinaryResources. The default implementation returns
     * the fully qualified class name.
     *
     * @param instance the application object instance.
     * @return the attribute name space for this object, or null if the instance is null.
     */
    public static String getAppObjectNameSpace(AppObject instance)
    {
        return instance != null ? instance.getClass().getName() : null;
    }

    /**
     * Execute a <code>JpaCallback</code> implementation.
     *
     * @param callback        the callback to execute
     * @param useTransactions should the operation be performed with transaction support?
     * @param params          arguments to supply to the callback
     * @param <T>             the return type
     * @return the result of the callback
     * @throws AppRuntimeException if the operation fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T executeJpaCallback(JpaCallback<T> callback, boolean useTransactions, Object... params)
            throws AppRuntimeException
    {
        final String METHOD_NAME = "executeJpaCallback";

        if (LOGGER.isLoggable(Level.FINE))
        {
            StringBuilder builder = new StringBuilder(200);
            builder.append("-----> Executing JPA Callback for: ");
            if (params != null)
            {
                for (Object o : params)
                {
                    builder.append(o).append(":");
                }
            }
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, builder.toString());
        }

        EntityManager em = null;
        EntityTransaction trx = null;
        try
        {
            em = emfInstance.createEntityManager();
            try
            {
                if (useTransactions)
                {
                    trx = em.getTransaction();
                    trx.begin();
                }

                T result = callback.execute(em, params);

                // TODO: Remove this hack later on.
                if (result instanceof List)
                {
                    List tmpList = new ArrayList(((List) result).size());
                    tmpList.addAll((List) result);
                    result = (T) tmpList;
                }

                if (useTransactions)
                {
                    trx.commit();
                }

                return result;
            }
            catch (Exception ex)
            {
                if (useTransactions)
                {
                    if (trx != null && trx.isActive())
                    {
                        trx.rollback();
                    }
                }
                throw new AppRuntimeException(ex);
            }
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
    }

    /**
     * Null safe method to get hold of the entity's key.
     *
     * @param entity the entity's whose key is desired.
     * @return the key of the passed in entity.
     */
    public static Key getEntityKey(PersistentObject<?> entity)
    {
        return entity == null ? null : entity.getId();
    }

    /**
     * Null safe utility method to get hold of an entity object based on the class name and the key.
     *
     * @param className the class name of the target object.
     * @param key       the key of the target object.
     * @return a object reference if found, null otherwise.
     */
    @SuppressWarnings("unchecked")
    public static PersistentObject<?> getAppObject(String className, Key key)
    {
        final String METHOD_NAME = "getAppObject";
        PersistentObject<?> appObject = null;

        if (key != null)
        {
            Class<? extends PersistentObject> clazz = null;
            if (className != null)
            {
                try
                {
                    clazz = (Class<? extends PersistentObject>) Thread.currentThread().getContextClassLoader()
                                                                      .loadClass(className);
                }
                catch (ClassNotFoundException e)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Invalid class name?", e);
                }
            }

            if (clazz != null)
            {
                appObject = JpaController.getController(clazz).findById(key);
            }
        }

        return appObject;
    }

    /**
     * Null safe utility method to get hold of an object given its key.
     *
     * @param objClass the class of the target object
     * @param key      the key of the target object
     * @return the application object class if available.
     */
    public static <E extends PersistentObject<E>> E getAppObject(Class<E> objClass, Key key)
    {
        if (key != null)
        {
            if (objClass != null)
            {
                return JpaController.getController(objClass).findById(key);
            }
        }
        return null;
    }
}
