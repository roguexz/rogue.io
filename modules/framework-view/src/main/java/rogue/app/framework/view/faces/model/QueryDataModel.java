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

package rogue.app.framework.view.faces.model;

import com.google.appengine.api.datastore.Key;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import rogue.app.framework.model.AppObjectCollection;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.Query;
import rogue.app.framework.view.faces.EntityConverter;

import java.util.List;
import java.util.Map;

/**
 * Implementation of PrimeFace's <code>LazyDataModel</code> that leverages queries to fetch the underlying data.
 */
public class QueryDataModel<T extends PersistentObject<T>> extends LazyDataModel<T>
{
    private final EntityConverter<T> converter;
    private Query listingQuery;
    private AppObjectCollection<T, Key> controller;

    public QueryDataModel(AppObjectCollection<T, Key> controller, EntityConverter<T> converter, Query listingQuery,
                          Query countQuery)
    {
        this.controller = controller;
        this.converter = converter;
        this.listingQuery = listingQuery;

        this.setRowCount((int) controller.count(countQuery));
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters)
    {
        listingQuery.setStartIndex(first);
        listingQuery.setMaxResults(pageSize);
        return controller.find(listingQuery);
    }

    @Override
    public Object getRowKey(T object)
    {
        return converter.getAsString(null, null, object);
    }

    @Override
    public T getRowData(String rowKey)
    {
        return converter.getAsObject(null, null, rowKey);
    }
}
