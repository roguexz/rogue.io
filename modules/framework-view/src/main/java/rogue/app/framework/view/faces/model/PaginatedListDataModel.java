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
import rogue.app.framework.view.PaginationHelper;

import java.util.Collections;
import java.util.List;

/**
 * A list data model that supports pagination.
 */
public class PaginatedListDataModel<E> extends PaginatedDataModel<E>
{
    private List<E> baseData;

    public PaginatedListDataModel(List<E> data)
    {
        super(true);
        this.baseData = data;
        PaginationHelper paginationHelper = new PaginationHelper();
        if (data != null)
        {
            paginationHelper.setItemCount(data.size());
        }
        setPagination(paginationHelper);
    }

    @Override
    protected List<E> loadData(int start, int pageSize) throws AppRuntimeException
    {
        List<E> subList = null;

        if (baseData != null)
        {
            if (start != 0 && start > baseData.size() - 1)
            {
                throw new IllegalArgumentException("Start index cannot be larger than the size of the base data set.");
            }

            int endIndex = start + pageSize;
            if (endIndex > baseData.size())
            {
                endIndex = baseData.size();
            }
            subList = baseData.subList(start, endIndex);
        }

        if (subList == null)
        {
            subList = Collections.emptyList();
        }

        return subList;
    }
}
