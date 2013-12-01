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
import com.google.appengine.api.datastore.KeyFactory;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.event.ChangeEvent;
import rogue.app.framework.event.ChangeListenerFor;
import rogue.app.framework.internal.util.ClassUtils;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.AppObjectCollection;
import rogue.app.framework.model.CompositeQuery;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.patterns.Composition;
import rogue.app.framework.patterns.Delegation;
import rogue.app.framework.persistence.annotations.ParentRef;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic JpaController class for executing common operations.
 */
public class JpaController<T extends PersistentObject<T>> implements AppObjectCollection<T, Key>
{
    private static final String CLASS_NAME = JpaController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private static final Map<Class<? extends PersistentObject>, JpaController<? extends PersistentObject>>
            controllersMap = new HashMap<>();
    private static final Map<Class<? extends PersistentObject>, Class<? extends PersistentEntity>> classMappingReference
            = new HashMap<>();
    private static final Map<Class<? extends PersistentObject>, List<Method>> changeListenerMethods = new HashMap<>(3);
    private Map<String, String> namedQueries = new HashMap<>(6);

    private static final String ANNOTATIONS_PROCESSED = CLASS_NAME + "_annotations_processed";


    ///
    /// Member variables
    ///
    private Class<T> appObjectClass;
    private Class<? extends PersistentEntity> entityClass;
    private transient Constructor<? extends PersistentEntity> constructor;
    private Class<? extends Annotation> cacheScope;
    private final String cachePrefix;
    private final String cacheRevision;
    private final String findEntitiesCacheKey;
    private final String getEntitiesCountCacheKey;

    protected JpaController(Class<T> appObjClass)
    {
        // final String METHOD_NAME = "<ctor>";

        this.appObjectClass = appObjClass;
        this.entityClass = classMappingReference.get(appObjClass);
        if (this.entityClass == null)
        {
            // Check if the incoming class has the entity for annotation.
            EntityImplementationFor ann = ClassUtils.getAnnotation(appObjClass, EntityImplementationFor.class);
            if (ann != null && ann.value() != null)
            {
                // reverse the order
                this.entityClass = (Class<? extends PersistentEntity>) appObjClass;
                this.appObjectClass = (Class<T>) ann.value();
            }
            else
            {
                throw new IllegalArgumentException("Entity class not registered for : " + appObjClass.getName());
            }
        }

        // Get the default cache scope.
        Cacheable c = entityClass.getAnnotation(Cacheable.class);
        if (c != null)
        {
            cacheScope = c.preferredScope();
        }

        // Create a cache prefix
        cachePrefix = "cache_" + entityClass.getName() + "_";
        cacheRevision = cachePrefix + "revision";
        findEntitiesCacheKey = cachePrefix + "findEntities";
        getEntitiesCountCacheKey = cachePrefix + "getEntitiesCount";

        // Read all the named queries associated with the entity class.
        NamedQueries queries = entityClass.getAnnotation(NamedQueries.class);
        if (queries != null)
        {
            NamedQuery[] qArr = queries.value();
            if (qArr != null)
            {
                for (NamedQuery q : qArr)
                {
                    namedQueries.put(q.name(), q.query());
                }
            }
        }

        // In case there is only one query for the entity class.
        NamedQuery query = entityClass.getAnnotation(NamedQuery.class);
        if (query != null)
        {
            namedQueries.put(query.name(), query.query());
        }

        namedQueries = Collections.unmodifiableMap(namedQueries);
    }

    /**
     * Get the <code>AppObject</code> class that this controller is associated with.
     *
     * @return the <code>AppObject</code> class that this controller is associated with.
     */
    public Class<T> getAppObjectClass()
    {
        return appObjectClass;
    }

    /**
     * Get the <code>PersistentEntity</code> class that this controller is associated with.
     *
     * @return the <code>PersistentEntity</code> class that this controller is associated with.
     */
    public Class<? extends PersistentEntity> getEntityClass()
    {
        return entityClass;
    }

    /**
     * Utility method to construct the key given it's internal ID.
     *
     * @param id the internal id of the key.
     * @return a key object.
     */
    public Key constructKey(long id)
    {
        return KeyFactory.createKey(entityClass.getSimpleName(), id);
    }

    @Override
    public rogue.app.framework.model.Query newQueryInstance() throws AppRuntimeException
    {
        throw new UnsupportedOperationException("Operation not implemented.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getNewInstance()
    {
        try
        {
            if (constructor == null)
            {
                constructor = entityClass.getConstructor(entityClass);
            }
            return (T) constructor.newInstance(new Object[]{null});
        }
        catch (Exception e)
        {
            throw new AppRuntimeException(String.format(
                    "Failed to create instance of entity [%s]. Ensure that the class has the required public constructors. ",
                    entityClass.getName()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getNewInstance(AppObject<?, ?> parent)
    {
        T instance = getNewInstance();
        if (parent != null && instance != null)
        {
            if (instance instanceof Composition)
            {
                ((Composition) instance).setParent(parent);
            }
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getMutableInstance(T appObject) throws AppRuntimeException
    {
        return appObject != null ? appObject.getMutableInstance() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T findById(Key id) throws AppRuntimeException
    {
        T entity = null;
        if (id != null)
        {
            StateHolder stateHolder = getStateHolder();
            String cacheKey = getCacheKey(id);

            entity = stateHolder.get(cacheKey);

            if (entity == null && !stateHolder.containsKey(cacheKey))
            {
                entity = (T) PersistenceUtils.executeJpaCallback(FIND_BY_ID_CALLBACK, false, entityClass, id);
                stateHolder.put(cacheKey, entity);
            }
        }
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Key parseId(String idAsString) throws AppRuntimeException
    {
        final String METHOD_NAME = "parseId";
        Key key = null;
        if (idAsString != null)
        {
            try
            {
                // Check if the id is a long number
                long id = Long.parseLong(idAsString);
                key = KeyFactory.createKey(getEntityClass().getSimpleName(), id);
            }
            catch (NumberFormatException e)
            {
                LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Failed to parse key", e);
            }

            if (key == null)
            {
                // Attempt to parse the string as is
                try
                {
                    key = KeyFactory.stringToKey(idAsString);
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Failed to parse key", e);
                }
            }
        }
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> find(rogue.app.framework.model.Query query) throws AppRuntimeException
    {
        if (query != null)
        {
            if (query instanceof JpaQuery)
            {
                JpaQuery jpaQuery = (JpaQuery) query;
                return jpaQuery.isNamedQuery() ?
                       this.<T>executeNamedQueryListResult(jpaQuery.getQuery(), jpaQuery.getStartIndex(),
                                                           jpaQuery.getMaxResults(),
                                                           jpaQuery.getQueryParams()) :
                       this.<T>executeQueryListResult(jpaQuery.getQuery(), jpaQuery.getStartIndex(),
                                                      jpaQuery.getMaxResults(),
                                                      jpaQuery.getQueryParams());
            }
            else if (query instanceof CompositeQuery)
            {
                // TODO: Ugly! Clean this up.
                CompositeQuery cq = (CompositeQuery) query;
                Map<String, Object> params = new HashMap<>();
                params.put("parent", cq.getParent());

                String cacheKey = getCacheKey(findEntitiesCacheKey, null, cq.getStartIndex(), cq.getMaxResults());
                StateHolder stateHolder = getStateHolder();

                List<T> list = stateHolder.get(cacheKey);
                if (list == null && !stateHolder.containsKey(cacheKey))
                {
                    list = (List<T>) PersistenceUtils
                            .executeJpaCallback(FIND_ENTITIES_CALLBACK, false, entityClass, cq.getStartIndex(),
                                                cq.getMaxResults(), cq.getParent());
                    stateHolder.put(cacheKey, list);
                }
                return list;

            }
            return findEntities(query.getStartIndex(), query.getMaxResults());
        }
        else
        {
            return findEntities(0, -1);
        }
    }

    @Override
    public long count(rogue.app.framework.model.Query query) throws AppRuntimeException
    {
        if (query != null)
        {
            if (query instanceof JpaQuery)
            {
                JpaQuery pQuery = (JpaQuery) query;
                Long result = (Long) (pQuery.isNamedQuery() ?
                                      executeNamedQuerySingleResult(pQuery.getQuery(), pQuery.getQueryParams()) :
                                      executeQuerySingleResult(pQuery.getQuery(), pQuery.getQueryParams()));
                return result != null ? result : 0;
            }
            else if (query instanceof CompositeQuery)
            {
                CompositeQuery cq = (CompositeQuery) query;
                Map<String, Object> params = new HashMap<>();
                params.put("parent", cq.getParent());

                String cacheKey = getCacheKey(getEntitiesCountCacheKey, null, 0, -1);
                StateHolder stateHolder = getStateHolder();
                Long count = stateHolder.get(cacheKey);
                if (count == null && !stateHolder.containsKey(cacheKey))
                {
                    count = PersistenceUtils
                            .executeJpaCallback(FIND_ENTITIES_COUNT_CALLBACK, false, entityClass, cq.getParent());
                    stateHolder.put(cacheKey, count);
                }
                return count == null ? 0 : count;
            }
        }
        return getEntitiesCount();
    }

    /**
     * Find a list of entities.
     *
     * @param startIndex the start index
     * @param itemCount  the number of items to fetch.
     * @return a list of entities.
     * @throws AppRuntimeException if the operation fails.
     */
    @SuppressWarnings("unchecked")
    public List<T> findEntities(int startIndex, int itemCount) throws AppRuntimeException
    {
        String cacheKey = getCacheKey(findEntitiesCacheKey, null, startIndex, itemCount);
        StateHolder stateHolder = getStateHolder();

        List<T> list = stateHolder.get(cacheKey);
        if (list == null && !stateHolder.containsKey(cacheKey))
        {
            list = (List<T>) PersistenceUtils
                    .executeJpaCallback(FIND_ENTITIES_CALLBACK, false, entityClass, startIndex, itemCount);
            stateHolder.put(cacheKey, list);
        }
        return list;
    }

    /**
     * Get the number of entities in the system.
     *
     * @return the number of entities within the system.
     * @throws AppRuntimeException if the operation fails.
     */
    public long getEntitiesCount() throws AppRuntimeException
    {
        String cacheKey = getCacheKey(getEntitiesCountCacheKey, null, 0, -1);
        StateHolder stateHolder = getStateHolder();
        Long count = stateHolder.get(cacheKey);
        if (count == null && !stateHolder.containsKey(cacheKey))
        {
            count = PersistenceUtils.executeJpaCallback(FIND_ENTITIES_COUNT_CALLBACK, false, entityClass);
            stateHolder.put(cacheKey, count);
        }
        return count == null ? 0 : count;
    }

    /**
     * Persist the given entity.
     *
     * @param entity the entity to persist.
     * @throws AppRuntimeException if the operation fails.
     */
    @SuppressWarnings("unchecked")
    public void save(T entity) throws AppRuntimeException
    {
        final String METHOD_NAME = "save";

        if (entity == null)
        {
            throw new AppRuntimeException("Entity cannot be null.");
        }

        if (entity instanceof Delegation)
        {
            entity = ((Delegation<T>) entity).getDelegate();
        }

        T oldObject = entity.getBaseResource();

        invokeAnnotatedMethods((PersistentEntity) entity, PreExecute.class, false);
        PersistenceUtils.executeJpaCallback(PERSIST_ENTITY_CALLBACK, true, entityClass, entity);
        invokeAnnotatedMethods((PersistentEntity) entity, PostExecute.class, false);
        // Clear the entity from the cache so that it can be fetched again afresh.
        getStateHolder().remove(getCacheKey(entity.getId()));
        incrementRevision();

        T newObject = entity;
        List<Method> changeListeners = changeListenerMethods.get(appObjectClass);
        if (changeListeners != null && !changeListeners.isEmpty())
        {
            ChangeEvent<T> event = new ChangeEvent<>(this, oldObject, newObject);
            for (Method m : changeListeners)
            {
                try
                {
                    m.setAccessible(true);
                    m.invoke(null, event);
                    m.setAccessible(false);
                }
                catch (IllegalAccessException | InvocationTargetException e)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to invoke change listener on method.",
                                e);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(AppObject... appObjects) throws AppRuntimeException
    {
        if (appObjects != null)
        {
            Key[] keys = new Key[appObjects.length];
            int count = 0;
            for (AppObject appObj : appObjects)
            {
                if (appObj instanceof PersistentEntity)
                {
                    Key id = ((PersistentEntity) appObj).getId();
                    if (id != null)
                    {
                        keys[count++] = id;
                    }
                }
            }

            if (keys.length != count)
            {
                keys = Arrays.copyOf(keys, count);
            }
            deleteById(keys);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(Key... entityKeys) throws AppRuntimeException
    {
        if (entityKeys != null && entityKeys.length != 0)
        {
            StateHolder stateHolder = getStateHolder();

            for (Key k : entityKeys)
            {
                PersistentEntity entity = PersistenceUtils.getAppObject(entityClass, k);
                invokeAnnotatedMethods(entity, PreExecute.class, true);
                PersistenceUtils.executeJpaCallback(DELETE_ENTITY_BY_ID_CALLBACK, true, entityClass, k);
                invokeAnnotatedMethods(entity, PostExecute.class, true);

                stateHolder.remove(getCacheKey(k));
            }
            incrementRevision();
        }
    }

    /**
     * Execute a given named query, associated with the entity managed by this controller, and returns its results.
     *
     * @param namedQuery the named query to execute
     * @param startIndex the index of the first result
     * @param itemCount  the maximum number of items in the result
     * @param params     the query parameters that should be applied to the query.
     * @return a list of results
     * @throws AppRuntimeException if the operation fails.
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> executeNamedQueryListResult(String namedQuery, Integer startIndex, Integer itemCount,
                                                   Map<String, Object> params)
            throws AppRuntimeException
    {
        // Verify if the named query belongs to the entity class that is being accessed or not.
        if (!namedQueries.containsKey(namedQuery))
        {
            throw new AppRuntimeException(String.format("Unable to find NamedQuery: %s on entity class %s", namedQuery,
                                                        entityClass.getName()));
        }

        String cacheKey = getCacheKey(namedQueries.get(namedQuery), params, startIndex, itemCount);

        StateHolder stateHolder = getStateHolder();
        List<E> result = stateHolder.get(cacheKey);

        if (result == null && !stateHolder.containsKey(cacheKey))
        {
            result = PersistenceUtils.executeJpaCallback(GENERIC_NAMED_QUERY_LIST_RESULT, false, null,
                                                         namedQuery, startIndex, itemCount, params);

            stateHolder.put(cacheKey, result);
        }

        return result;
    }

    /**
     * Execute a given named query, associated with the entity managed by this controller, and return a single result.
     *
     * @param namedQuery the named query to execute.
     * @param params     the query parameters that should be applied to the query.
     * @return the "single" result after executing the query, or null in all other cases.
     */
    @SuppressWarnings("unchecked")
    public <E> E executeNamedQuerySingleResult(String namedQuery, Map<String, Object> params)
    {
        // Verify if the named query belongs to the entity class that is being accessed or not.
        if (!namedQueries.containsKey(namedQuery))
        {
            throw new AppRuntimeException(String.format("Unable to find NamedQuery: %s on entity class %s", namedQuery,
                                                        entityClass.getName()));
        }

        String cacheKey = getCacheKey(namedQueries.get(namedQuery), params, null, null);
        StateHolder stateHolder = getStateHolder();
        E result = stateHolder.get(cacheKey);

        if (result == null && !stateHolder.containsKey(cacheKey))
        {
            result = (E) PersistenceUtils.executeJpaCallback(GENERIC_NAMED_QUERY_SINGLE_RESULT, false,
                                                             null, namedQuery, params);

            stateHolder.put(cacheKey, result);
        }
        return result;
    }

    public void executeNamedQueryUpdate(String namedQuery, Map<String, Object> params)
    {
        // Verify if the named query belongs to the entity class that is being accessed or not.
        if (!namedQueries.containsKey(namedQuery))
        {
            throw new AppRuntimeException(String.format("Unable to find NamedQuery: %s on entity class %s", namedQuery,
                                                        entityClass.getName()));
        }

        PersistenceUtils.executeJpaCallback(GENERIC_NAMED_QUERY_EXECUTE_UPDATE, true, null, namedQuery, params);
        incrementRevision();
    }

    /**
     * Execute the given query, associated with the entity managed by this controller, and returns its results.
     *
     * @param query      the query to execute
     * @param startIndex the index of the first result
     * @param itemCount  the maximum number of items in the result
     * @param params     the query parameters that should be applied to the query.
     * @return a list of results
     * @throws AppRuntimeException if the operation fails.
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> executeQueryListResult(String query, Integer startIndex, Integer itemCount,
                                              Map<String, Object> params) throws AppRuntimeException
    {
        String cacheKey = getCacheKey(query, params, startIndex, itemCount);

        StateHolder stateHolder = getStateHolder();
        List<E> result = stateHolder.get(cacheKey);

        if (result == null && !stateHolder.containsKey(cacheKey))
        {
            result = PersistenceUtils.executeJpaCallback(GENERIC_QUERY_LIST_RESULT, false, null,
                                                         query, startIndex, itemCount, params);

            stateHolder.put(cacheKey, result);
        }

        return result;
    }

    /**
     * Execute the given query, associated with the entity managed by this controller, and return a single result.
     *
     * @param query  the query to execute.
     * @param params the query parameters that should be applied to the query.
     * @return the "single" result after executing the query, or null in all other cases.
     */
    @SuppressWarnings("unchecked")
    public <E> E executeQuerySingleResult(String query, Map<String, Object> params)
    {
        String cacheKey = getCacheKey(query, params, null, null);

        StateHolder stateHolder = getStateHolder();
        E result = stateHolder.get(cacheKey);

        if (result == null && !stateHolder.containsKey(cacheKey))
        {
            result = (E) PersistenceUtils.executeJpaCallback(GENERIC_QUERY_SINGLE_RESULT, false,
                                                             null, query, params);
            stateHolder.put(cacheKey, result);
        }
        return result;
    }

    /**
     * Get a default instance of the JpaController for a given entity class.
     *
     * @param appObjClassRef the application object class reference.
     * @param <E>            type of the class
     * @return the JpaController instance.
     */
    @SuppressWarnings("unchecked")
    public static <E extends PersistentObject<E>> JpaController<E> getController(Class<E> appObjClassRef)
    {
        initController();

        JpaController<E> controller = (JpaController<E>) controllersMap.get(appObjClassRef);

        if (controller == null)
        {
            // Check if any of the super interfaces / classes have registered an JpaController.
            Class<?>[] ifs = appObjClassRef.getInterfaces();
            for (Class c : ifs)
            {
                if (c.isInterface() && PersistentObject.class.isAssignableFrom(c))
                {
                    controller = (JpaController<E>) controllersMap.get(c);
                }
                if (controller != null)
                {
                    break;
                }
            }
        }

        if (controller == null)
        {
            controller = new JpaController<>(appObjClassRef);
            controllersMap.put(appObjClassRef, controller);
        }
        return controller;
    }

    @SuppressWarnings("unchecked")
    private static void initController()
    {
        final String METHOD_NAME = "initController";
        if (!isAnnotationsProcessed())
        {
            synchronized (JpaController.class)
            {
                if (!isAnnotationsProcessed())
                {
                    try
                    {
                        // Process classes

                        Iterator<Class<?>> iterator = ClassUtils.getTypesAnnotatedWith(EntityImplementationFor.class);
                        while (iterator != null && iterator.hasNext())
                        {
                            Class<?> entityClass = iterator.next();
                            if (PersistentEntity.class.isAssignableFrom(entityClass))
                            {
                                EntityImplementationFor annotation =
                                        entityClass.getAnnotation(EntityImplementationFor.class);

                                if (annotation == null)
                                {
                                    LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
                                                "Framework issue. Reported annotation not found!!");
                                    return;
                                }

                                Class<? extends PersistentObject> appObjClass = annotation.value();
                                if (appObjClass.isAssignableFrom(entityClass))
                                {
                                    registerMapping(appObjClass, (Class<? extends PersistentEntity>) entityClass);
                                }
                                else
                                {
                                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, String.format(
                                            "Class %s does not implement the type object, %s. Ignoring entry.",
                                            entityClass.getName(), appObjClass.getName()));
                                }
                            }
                            else
                            {
                                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                            String.format("Class %s does not extend %s. Ignoring entry.",
                                                          entityClass.getName(), PersistentEntity.class.getName()));
                            }
                        }

                        // Process methods
                        Iterator<Method> methods = ClassUtils.getMethodsAnnotatedWith(ChangeListenerFor.class);
                        while (methods != null && methods.hasNext())
                        {
                            Method method = methods.next();
                            Class<?>[] paramTypes = method.getParameterTypes();
                            if (Modifier.isStatic(method.getModifiers()) && paramTypes.length == 1 && ChangeEvent.class
                                    .isAssignableFrom(paramTypes[0]))
                            {
                                ChangeListenerFor ann = method.getAnnotation(ChangeListenerFor.class);
                                Class<?> sourceClass = ann.sourceClass();
                                if (PersistentObject.class.isAssignableFrom(sourceClass))
                                {
                                    Class<? extends PersistentObject> pClass =
                                            (Class<? extends PersistentObject>) sourceClass;
                                    List<Method> mList = changeListenerMethods.get(pClass);
                                    if (mList == null)
                                    {
                                        mList = new ArrayList<>();
                                        changeListenerMethods.put(pClass, mList);
                                    }
                                    if (!mList.contains(method))
                                    {
                                        mList.add(method);
                                    }
                                }
                                else
                                {
                                    LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                                                "Currently change event is supported only for Persistent objects.");
                                }
                            }
                            else
                            {
                                LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, ChangeEvent.class.getSimpleName() +
                                        " can only be applied to methods that conform to the following signature '<any-access> static <any-return> methodName(ChangeEvent event)");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to initialize application.", e);
                    }

                    markAnnotationsProcessed();
                }
            }
        }
    }

    private static void registerMapping(Class<? extends PersistentObject> appObjClass,
                                        Class<? extends PersistentEntity> entityClassRef)
    {
        if (!appObjClass.isAssignableFrom(entityClassRef))
        {
            throw new IllegalArgumentException(
                    String.format("Entity class %s must implement the application object definition %s", entityClassRef,
                                  appObjClass));
        }
        classMappingReference.put(appObjClass, entityClassRef);
    }

    /**
     * Centralize the logic of generating cache keys.
     *
     * @param id the entity id for which a cache key needs to be generated.
     * @return the cache key, or null if the specified object id is null.
     */
    protected String getCacheKey(Key id)
    {
        return id != null ? cachePrefix + id.getId() + " / " + getRevision() : null;
    }

    protected String getCacheKey(String query, Map<String, Object> params, Integer start, Integer count)
    {
        StringBuilder bldr = new StringBuilder(100);
        bldr.append(query);
        if (params != null && !params.isEmpty())
        {
            for (Map.Entry<String, Object> entry : params.entrySet())
            {
                if (StringUtils.isEmpty(entry.getKey()))
                {
                    continue;
                }

                String key = ":" + entry.getKey();
                Object value = entry.getValue();
                int idx = -1;
                while ((idx = bldr.indexOf(key, idx + 1)) != -1)
                {
                    bldr.replace(idx, idx + key.length(), value != null ? value.toString() : "null");
                }
            }
        }

        // Add the start & stop indices
        if (start != null)
        {
            bldr.append(" start=").append(start);
        }
        if (count != null)
        {
            bldr.append(" count=").append(count);
        }

        // Append the revision number
        bldr.append(" / ").append(getRevision());
        return bldr.toString();
    }

    protected int getRevision()
    {
        // This is kinda flaky, but should work for a large number of use-cases. Worse case scenario, you will need
        // to flush the cache.

        Integer rev = ScopedStateHolder.getStateHolder(MemcachedScoped.class).get(cacheRevision);
        if (rev == null)
        {
            rev = 0;
        }
        return rev;
    }

    protected void incrementRevision()
    {
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(MemcachedScoped.class);
        Integer rev = stateHolder.get(cacheRevision);
        if (rev == null)
        {
            rev = 0;
        }
        stateHolder.put(cacheRevision, ++rev);
    }

    protected StateHolder getStateHolder()
    {
        return ScopedStateHolder.getStateHolder(cacheScope);
    }

    @SuppressWarnings("unchecked")
    private static void invokeAnnotatedMethods(PersistentEntity entity, Class annotation, boolean entityDeleted)
    {
        if (entity == null || annotation == null)
        {
            return;
        }

        Class clazz = entity.getClass();
        while (PersistentEntity.class.isAssignableFrom(clazz))
        {
            try
            {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method m : methods)
                {
                    if (m.isAnnotationPresent(annotation))
                    {
                        Class[] params = m.getParameterTypes();
                        if (params.length == 1 && params[0] == boolean.class)
                        {
                            m.setAccessible(true);
                            m.invoke(entity, entityDeleted);
                            m.setAccessible(false);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
            catch (Exception e)
            {
                throw (e instanceof AppRuntimeException) ? (AppRuntimeException) e : new AppRuntimeException(e);
            }
        }
    }

    private static boolean isAnnotationsProcessed()
    {

        StateHolder stateHolder = ScopedStateHolder.getStateHolder(ApplicationScoped.class);
        Boolean b = stateHolder.get(ANNOTATIONS_PROCESSED);
        return b != null ? b : false;
    }

    private static void markAnnotationsProcessed()
    {
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(ApplicationScoped.class);
        stateHolder.put(ANNOTATIONS_PROCESSED, Boolean.TRUE);
    }

    /* ============================================================================================================== *
     *                                                                                                                *
     *                                           JPA Callbacks                                                        *
     *                                                                                                                *
     * ============================================================================================================== *
     */

    /**
     * Callback to find entity by id.
     */
    @SuppressWarnings("unchecked")
    private static final JpaCallback<? extends PersistentEntity> FIND_BY_ID_CALLBACK =
            new JpaCallback<PersistentEntity>()
            {
                @Override
                public PersistentEntity execute(EntityManager entityManager, Object... params)
                        throws AppRuntimeException
                {
                    Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
                    Key key = (Key) params[1];
                    return entityManager.find(clazz, key);
                }
            };

    /**
     * Callback to get list of entities.
     */
    @SuppressWarnings("unchecked")
    private static final JpaCallback<List<? extends PersistentEntity>> FIND_ENTITIES_CALLBACK =
            new JpaCallback<List<? extends PersistentEntity>>()
            {
                @Override
                public List<? extends PersistentEntity> execute(EntityManager entityManager, Object... params)
                        throws AppRuntimeException
                {
                    Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];

                    Integer startResult = (Integer) params[1];
                    Integer maxResults = (Integer) params[2];
                    PersistentObject parent = null;
                    StringBuilder queryString = new StringBuilder("SELECT e FROM ").append(
                            clazz.getSimpleName()).append(" e");

                    if (params.length > 3 && params[3] instanceof PersistentObject)
                    {
                        // This is the case where I received a parent object from Composite Query.
                        // TODO: I am not very happy with this hack, but this will have to do for now.
                        parent = (PersistentObject) params[3];
                        Field field = ClassUtils.getAnnotatedField(clazz, ParentRef.class);
                        if (field != null)
                        {
                            String colName = field.getName();
                            // Verify if the Column annotation has been set or not.
                            Column column = field.getAnnotation(Column.class);
                            if (column != null && !column.name().isEmpty())
                            {
                                colName = column.name();
                            }
                            queryString.append(" WHERE e.").append(colName).append("=").append(":parentKey");
                        }
                    }
                    OrderByAttribute attr = clazz.getAnnotation(OrderByAttribute.class);
                    if (attr != null)
                    {
                        queryString.append(" ORDER BY e.").append(attr.value());
                    }

                    Query query = entityManager.createQuery(queryString.toString());
                    if (startResult != null && maxResults != null && maxResults != -1)
                    {
                        query.setFirstResult(startResult).setMaxResults(maxResults);
                    }
                    if (parent != null)
                    {
                        query.setParameter("parentKey", parent.getId());
                    }
                    return query.getResultList();
                }
            };

    /**
     * Callback to get the number of entities within the system.
     */
    @SuppressWarnings("unchecked")
    private static JpaCallback<Long> FIND_ENTITIES_COUNT_CALLBACK = new JpaCallback<Long>()
    {
        @Override
        public Long execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            PersistentObject parent = null;
            StringBuilder queryString = new StringBuilder(100);
            queryString.append("SELECT COUNT(e) FROM ").append(clazz.getSimpleName()).append(" e");

            if (params.length > 1 && params[1] instanceof PersistentObject)
            {
                // This is the case where I received a parent object from Composite Query.
                // TODO: I am not very happy with this hack, but this will have to do for now.
                parent = (PersistentObject) params[1];
                Field field = ClassUtils.getAnnotatedField(clazz, ParentRef.class);
                if (field != null)
                {
                    String colName = field.getName();
                    // Verify if the Column annotation has been set or not.
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && !column.name().isEmpty())
                    {
                        colName = column.name();
                    }
                    queryString.append(" WHERE e.").append(colName).append("=").append(":parentKey");
                }
            }

            Query query = entityManager.createQuery(queryString.toString());
            if (parent != null)
            {
                query.setParameter("parentKey", parent.getId());
            }
            return (Long) query.getSingleResult();
        }
    };

    /**
     * Callback to persist an entity.
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static JpaCallback<? extends PersistentEntity> PERSIST_ENTITY_CALLBACK = new JpaCallback<PersistentEntity>()
    {
        @Override
        public PersistentEntity execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            PersistentEntity entity = (PersistentEntity) params[1];

            if (entity.getId() == null)
            {
                entityManager.persist(entity);
            }
            else
            {
                entityManager.merge(entity);
            }
            return entity;
        }
    };

    /**
     * Callback to delete an entity by its ID.
     */
    @SuppressWarnings("unchecked")
    private static final JpaCallback DELETE_ENTITY_BY_ID_CALLBACK = new JpaCallback()
    {
        @Override
        public Object execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            Key key = (Key) params[1];
            Object entity = entityManager.getReference(clazz, key);
            entityManager.remove(entity);
            return null;
        }
    };

    /**
     * Generic NamedQuery callback.
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static final JpaCallback<List> GENERIC_NAMED_QUERY_LIST_RESULT = new JpaCallback<List>()
    {
        @Override
        public List execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            String namedQuery = (String) params[1];

            Integer startResult = (Integer) params[2];
            Integer maxResults = (Integer) params[3];

            Map<String, Object> parameters = (Map<String, Object>) params[4];
            Query query = entityManager.createNamedQuery(namedQuery);

            if (parameters != null)
            {
                for (Map.Entry<String, Object> entry : parameters.entrySet())
                {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            if (startResult != null && maxResults != null && maxResults != -1)
            {
                query.setFirstResult(startResult).setMaxResults(maxResults);
            }

            List result = query.getResultList();
            if (result.isEmpty())
            {
                result = Collections.EMPTY_LIST;
            }
            return result;
        }
    };

    @SuppressWarnings({"unchecked", "unused"})
    private static final JpaCallback<Object> GENERIC_NAMED_QUERY_SINGLE_RESULT = new JpaCallback<Object>()
    {
        @Override
        public Object execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            final String METHOD_NAME = "GENERIC_NAMED_QUERY_SINGLE_RESULT_Callback";
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            String namedQuery = (String) params[1];

            Map<String, Object> parameters = (Map<String, Object>) params[2];
            Query query = entityManager.createNamedQuery(namedQuery);

            if (parameters != null)
            {
                for (Map.Entry<String, Object> entry : parameters.entrySet())
                {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            Object result = null;

            try
            {
                result = query.getSingleResult();
            }
            catch (Exception e)
            {
                StringBuilder builder = new StringBuilder("Unable to find single record for named query: ")
                        .append(namedQuery).append(" : ").append(e.getMessage());

                LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, builder.toString());
                if (LOGGER.isLoggable(Level.FINEST))
                {
                    if (parameters != null)
                    {
                        builder.append(" [ ");
                        for (Map.Entry<String, Object> entry : parameters.entrySet())
                        {
                            builder.append("[").append(entry.getKey()).append(",").append(entry.getValue())
                                   .append("] ");
                        }
                        builder.append("]");
                    }
                    LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, builder.toString(), e);
                }
            }

            return result;
        }
    };

    /**
     * Generic query callback.
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static final JpaCallback<List> GENERIC_QUERY_LIST_RESULT = new JpaCallback<List>()
    {
        @Override
        public List execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            String q = (String) params[1];

            Integer startResult = (Integer) params[2];
            Integer maxResults = (Integer) params[3];

            Map<String, Object> parameters = (Map<String, Object>) params[4];
            Query query = entityManager.createQuery(q);

            if (parameters != null)
            {
                for (Map.Entry<String, Object> entry : parameters.entrySet())
                {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            if (startResult != null && maxResults != null && maxResults != -1)
            {
                query.setFirstResult(startResult).setMaxResults(maxResults);
            }

            List result = query.getResultList();
            if (result.isEmpty())
            {
                result = Collections.EMPTY_LIST;
            }
            return result;
        }
    };

    @SuppressWarnings({"unchecked", "unused"})
    private static final JpaCallback<Object> GENERIC_QUERY_SINGLE_RESULT = new JpaCallback<Object>()
    {
        @Override
        public Object execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            final String METHOD_NAME = "GENERIC_QUERY_SINGLE_RESULT_Callback";
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            String q = (String) params[1];

            Map<String, Object> parameters = (Map<String, Object>) params[2];
            Query query = entityManager.createQuery(q);

            if (parameters != null)
            {
                for (Map.Entry<String, Object> entry : parameters.entrySet())
                {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            Object result = null;

            try
            {
                result = query.getSingleResult();
            }
            catch (Exception e)
            {
                StringBuilder builder = new StringBuilder("Unable to find single record for query: ")
                        .append(q).append(" : ").append(e.getMessage());

                LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, builder.toString());
                if (LOGGER.isLoggable(Level.FINEST))
                {
                    if (parameters != null)
                    {
                        builder.append(" [ ");
                        for (Map.Entry<String, Object> entry : parameters.entrySet())
                        {
                            builder.append("[").append(entry.getKey()).append(",").append(entry.getValue())
                                   .append("] ");
                        }
                        builder.append("]");
                    }
                    LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, builder.toString(), e);
                }
            }

            return result;
        }
    };

    @SuppressWarnings({"unchecked", "unused"})
    private static final JpaCallback<Object> GENERIC_NAMED_QUERY_EXECUTE_UPDATE = new JpaCallback<Object>()
    {
        @Override
        public Object execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            final String METHOD_NAME = "GENERIC_NAMED_QUERY_EXECUTE_UPDATE_Callback";
            Class<? extends PersistentEntity> clazz = (Class<? extends PersistentEntity>) params[0];
            String namedQuery = (String) params[1];

            Map<String, Object> parameters = (Map<String, Object>) params[2];
            Query query = entityManager.createNamedQuery(namedQuery);

            if (parameters != null)
            {
                for (Map.Entry<String, Object> entry : parameters.entrySet())
                {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }

            try
            {
                query.executeUpdate();
            }
            catch (Exception e)
            {
                StringBuilder builder = new StringBuilder("Unable to execute named query: ")
                        .append(namedQuery).append(" : ").append(e.getMessage());

                LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, builder.toString());
                if (LOGGER.isLoggable(Level.FINEST))
                {
                    if (parameters != null)
                    {
                        builder.append(" [ ");
                        for (Map.Entry<String, Object> entry : parameters.entrySet())
                        {
                            builder.append("[").append(entry.getKey()).append(",").append(entry.getValue())
                                   .append("] ");
                        }
                        builder.append("]");
                    }
                    LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, builder.toString(), e);
                }
            }

            return null;
        }
    };
}
