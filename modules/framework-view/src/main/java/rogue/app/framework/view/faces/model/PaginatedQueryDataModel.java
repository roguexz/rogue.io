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

import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.view.PaginationHelper;

import javax.enterprise.context.RequestScoped;
import java.util.List;
import java.util.Map;

/**
 * Extension of the paginated data model interface that fetches its data using JPA queries.
 */
public class PaginatedQueryDataModel<T extends PersistentObject<T>> extends PaginatedDataModel<T>
{
    private final Class<T> appObjClass;
    private final Map<String, Object> queryParams;
    private final String resultQuery;
    private final String countQuery;
    private final boolean isNamedQuery;

    public PaginatedQueryDataModel(JpaController<T> controller, String resultQuery, String countQuery,
                                   Map<String, Object> queryParams, boolean paginated, boolean isNamedQuery)
    {
        super(paginated);
        this.appObjClass = controller.getAppObjectClass();
        this.resultQuery = resultQuery;
        this.countQuery = countQuery;
        this.queryParams = queryParams;
        this.isNamedQuery = isNamedQuery;

        PaginationHelper paginationHelper = new PaginationHelper()
        {
            public int getItemCount()
            {
                Long count = ScopedStateHolder.getStateHolder(RequestScoped.class).get(getCountQuery());

                if (count == null)
                {
                    if (isNamedQuery())
                    {
                        count = getController().executeNamedQuerySingleResult(getCountQuery(), getQueryParams());
                    }
                    else
                    {
                        count = getController().executeQuerySingleResult(getCountQuery(), getQueryParams());
                    }

                    if (count == null)
                    {
                        count = 0L;
                    }

                    ScopedStateHolder.getStateHolder(RequestScoped.class).put(getCountQuery(), count);
                    super.setItemCount(count.intValue());
                }
                return count.intValue();
            }

            @Override
            public void setItemCount(int itemCount)
            {
                // No-op
            }

            @Override
            public void setPageIndex(int pageIndex) throws IllegalStateException, IndexOutOfBoundsException
            {
                // Make sure that the total count has been setup properly
                clearData();
                getItemCount();
                super.setPageIndex(pageIndex);
            }

        };
        setPagination(paginationHelper);

    }

    @Override
    protected List<T> loadData(int start, int pageSize) throws AppRuntimeException
    {
        return isNamedQuery ?
               getController().<T>executeNamedQueryListResult(resultQuery, start, pageSize, queryParams) :
               getController().<T>executeQueryListResult(resultQuery, start, pageSize, queryParams);
    }

    private boolean isNamedQuery()
    {
        return isNamedQuery;
    }

    private String getCountQuery()
    {
        return countQuery;
    }

    private Map<String, Object> getQueryParams()
    {
        return queryParams;
    }

    private JpaController<T> getController()
    {
        return JpaController.getController(appObjClass);
    }
}
