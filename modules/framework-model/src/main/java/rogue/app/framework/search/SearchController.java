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

package rogue.app.framework.search;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.search.*;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.internal.util.ClassUtils;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Search and index <code>AppObject</code> instances.
 */
public class SearchController<T extends PersistentObject<T>>
{
    /**
     * The default number of results that will be displayed.
     */
    public static final int DEFAULT_RESULT_SIZE = 25;

    private static final String CLASS_NAME = SearchController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final Map<Class<? extends AppObject>, SearchController> searchControllerMap = new HashMap<>();

    private Index searchIndex;
    private List<MethodMetadata> methodMetadataList;
    private final Class<T> appObjClass;

    protected SearchController(Class<T> appObjClass)
    {
        final String METHOD_NAME = "<clinit>";

        if (appObjClass == null)
        {
            throw new AppRuntimeException("AppObject class cannot be null");
        }

        SearchIndex searchIndexAnn = ClassUtils.getAnnotation(appObjClass, SearchIndex.class);

        if (searchIndexAnn == null)
        {
            throw new AppRuntimeException(appObjClass.getName() + " : is not annotated with SearchIndex annotation.");
        }

        this.appObjClass = appObjClass;
        String indexName = StringUtils.trimToEmpty(searchIndexAnn.name());

        if (StringUtils.isEmpty(indexName))
        {
            throw new AppRuntimeException("Index name cannot be empty.");
        }

        LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                    String.format("Constructing Index for class %s with index name %s",
                                  appObjClass.getName(), indexName));

        IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
        searchIndex = SearchServiceFactory.getSearchService().getIndex(indexSpec);

        // Identify all the fields that should be indexed.
        methodMetadataList = getSearchMethods(appObjClass);

        if (methodMetadataList.isEmpty())
        {
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        String.format("[%s] There are no methods annotated with the SearchField annotation",
                                      appObjClass.getName()));
        }
    }

    /**
     * Get hold of the search controller associated with the specified <code>AppObject</code> class.
     *
     * @param appObjClass the app object class.
     * @param <E>         parameter type indicating the extending class.
     * @return the search controller.
     * @throws AppRuntimeException if creation of the controller fails.
     */
    @SuppressWarnings("unchecked")
    public static <E extends PersistentObject<E>> SearchController<E> getSearchController(Class<E> appObjClass)
            throws AppRuntimeException
    {
        SearchController<E> controller = searchControllerMap.get(appObjClass);
        if (controller == null)
        {
            controller = new SearchController<E>(appObjClass);
            searchControllerMap.put(appObjClass, controller);
        }
        return controller;
    }

    /**
     * Get the search index associated with the current instance.
     *
     * @return the search index associated with the current instance.
     */
    protected Index getSearchIndex()
    {
        return searchIndex;
    }

    /**
     * Add the objects, identified by the given keys, to the index.
     *
     * @param keys the keys of the AppObject to add.
     */
    public void index(Key... keys)
    {
        if (keys != null)
        {
            List<Document> docList = new ArrayList<Document>();
            for (Key k : keys)
            {
                JpaController<T> controller = JpaController.getController(appObjClass);
                T object = controller.findById(k);
                Document document = buildDocument(object);
                if (document != null)
                {
                    // First try and remove it, then add it.
                    remove(k);
                    docList.add(document);
                }
            }

            if (!docList.isEmpty())
            {
                getSearchIndex().put(docList.toArray(new Document[docList.size()]));
            }
        }
    }

    /**
     * Remove the object, identified by the given key, from the index.
     *
     * @param keys the key of the AppObject to remove.
     */
    public void remove(Key... keys)
    {
        final String METHOD_NAME = "remove";
        if (keys != null)
        {
            List<String> docIds = new ArrayList<String>();
            for (Key k : keys)
            {
                if (k != null)
                {
                    docIds.add(String.valueOf(k.getId()));
                }
            }

            if (!docIds.isEmpty())
            {
                try
                {
                    getSearchIndex().delete(docIds.toArray(new String[docIds.size()]));
                }
                catch (DeleteException e)
                {
                    List<String> failedIds = new ArrayList<String>();
                    Iterator<OperationResult> resultIterator = e.getResults().iterator();
                    Iterator<String> docIdIterator = docIds.iterator();
                    while (resultIterator.hasNext() && docIdIterator.hasNext())
                    {
                        OperationResult result = resultIterator.next();
                        String docId = docIdIterator.next();
                        if (!StatusCode.OK.equals(result.getCode()))
                        {
                            failedIds.add(docId);
                        }
                    }
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to remove documents " + failedIds, e);
                }
            }
        }
    }

    /**
     * Search and return a list of <code>AppObject</code>s that match the given query criteria.
     *
     * @param queryString the string to query.
     * @param limit       the number of results to fetch. If set to a non-positive number, then
     *                    {@link SearchController#DEFAULT_RESULT_SIZE default values} will take effect.
     * @return a list of results matching the given search criteria.
     */
    public List<T> search(String queryString, int limit)
    {
        if (StringUtils.isEmpty(queryString))
        {
            return Collections.emptyList();
        }

        if (limit <= 0)
        {
            limit = DEFAULT_RESULT_SIZE;
        }

        Query.Builder builder = Query.newBuilder();
        builder.setOptions(QueryOptions.newBuilder().setLimit(limit).build());
        Query query = builder.build(queryString);
        return search(query);
    }

    /**
     * Search and return a list of <code>AppObject</code>s that match the given query criteria.
     *
     * @param query the query to execute.
     * @return a list of results matching the given search criteria.
     */
    public List<T> search(Query query)
    {
        final String METHOD_NAME = "search(query)";
        Results<ScoredDocument> scoredResults = getSearchIndex().search(query);
        List<T> results = new ArrayList<T>();
        JpaController<T> controller = JpaController.getController(appObjClass);
        for (ScoredDocument doc : scoredResults)
        {
            try
            {
                Key key = controller.constructKey(Long.parseLong(doc.getId()));
                T appObj = controller.findById(key);
                if (appObj != null)
                {
                    results.add(appObj);
                }
            }
            catch (NumberFormatException e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Caught a number format exception while parsing the key.", e);
            }
        }
        return results;
    }


    protected Document buildDocument(T appObject) throws AppRuntimeException
    {
        final String METHOD_NAME = "buildDocument";
        if (appObject == null)
        {
            return null;
        }

        if (appObject.getId() == null)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        "Cannot build documents whose ID is null - " + appObject.toString());
        }

        if (methodMetadataList.isEmpty())
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        String.format("Method list is either null or empty for class: %s. Aborting operation.",
                                      this.appObjClass.getName()));
            return null;
        }

        Document.Builder docBuilder = Document.newBuilder();

        // Set the ID
        docBuilder.setId(Long.toString(appObject.getId().getId()));

        // Iterate over the methods.
        for (MethodMetadata metadata : methodMetadataList)
        {
            try
            {
                Field.Builder fieldBuilder = resolveField(metadata, appObject);
                docBuilder.addField(fieldBuilder);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            String.format(
                                    "Caught exception while trying to resolve field: %s. Aborting operation. ",
                                    metadata.getMethod().getName()));
                return null;
            }
        }

        return docBuilder.build();
    }

    protected Field.Builder resolveField(MethodMetadata metadata, T appObject)
            throws InvocationTargetException, IllegalAccessException
    {
        final String METHOD_NAME = "resolveField";
        Method m = metadata.getMethod();
        Object value = m.invoke(appObject, (Object[]) null);

        if (value == null)
        {
            return null;
        }

        Field.Builder builder = Field.newBuilder();
        builder.setName(metadata.getFieldName());
        switch (metadata.getFieldType())
        {
            case TEXT:
                builder.setText(String.valueOf(value));
                break;
            case HTML:
                builder.setHTML(String.valueOf(value));
                break;
            case NUMBER:
                builder.setNumber(Double.parseDouble(String.valueOf(value)));
                break;
            case ATOM:
                builder.setAtom(String.valueOf(value));
                break;
            case DATE:
                if (value instanceof Date)
                {
                    builder.setDate((Date) value);
                }
                else
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                "Resolved value is not an instance of Date - " + value);
                }
                break;
            case GEO_POINT:
                if (value instanceof GeoPt)
                {
                    builder.setGeoPoint(toGeoPoint((GeoPt) value));
                }
                else if (value instanceof GeoPoint)
                {
                    builder.setGeoPoint((GeoPoint) value);
                }
                else
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                "Resolved value is neither an instance of GeoPt nor GeoPoint - " + value);
                }
                break;
            default:
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            String.format("No code available to handle fieldType: %s. Treating it as string.",
                                          metadata.getFieldType()));
                builder.setText(String.valueOf(value));
        }
        return builder;
    }

    /**
     * Utility method to convert from GeoPt to GeoPoint.
     *
     * @param pt the GeoPt to convert.
     * @return the converted GeoPoint object.
     */
    protected GeoPoint toGeoPoint(GeoPt pt)
    {
        return pt != null ? new GeoPoint(pt.getLatitude(), pt.getLongitude()) : null;
    }

    /**
     * Get a list of methods annotated with {@link rogue.app.framework.search.SearchField} within the given class's hierarchy.
     *
     * @param clazz the class to introspect
     * @return a list of MethodMetadata objects.
     */
    private static List<SearchController.MethodMetadata> getSearchMethods(Class clazz)
    {
        List<Method> methodList = ClassUtils.getAnnotatedMethods(clazz, SearchField.class);
        List<SearchController.MethodMetadata> metadataList = new ArrayList<SearchController.MethodMetadata>();
        for (Method m : methodList)
        {
            metadataList.add(new SearchController.MethodMetadata(m));
        }
        return Collections.unmodifiableList(metadataList);
    }

    /**
     * Utility class leveraged by the Search implementation.
     */
    private static class MethodMetadata
    {
        private Method method;
        private String fieldName;
        private Field.FieldType fieldType;

        protected MethodMetadata(Method method)
        {
            this.method = method;
            SearchField fieldAnn = method.getAnnotation(SearchField.class);
            if (fieldAnn == null)
            {
                throw new IllegalArgumentException("Method does not contain the SearchField annotation.");
            }
            this.fieldName = StringUtils.trim(fieldAnn.name());
            if (StringUtils.isEmpty(this.fieldName))
            {
                throw new AppRuntimeException("Search field name cannot be empty.");
            }
            this.fieldType = fieldAnn.type();
        }

        public Method getMethod()
        {
            return method;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public Field.FieldType getFieldType()
        {
            return fieldType;
        }
    }
}
