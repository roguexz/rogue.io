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
import rogue.app.framework.view.Pagination;

import javax.faces.model.DataModel;
import javax.faces.model.DataModelEvent;
import javax.faces.model.DataModelListener;
import java.util.List;

/**
 * An extension of the <code>javax.faces.model.DataModel</code> that supports
 * {@link rogue.app.framework.view.Pagination Pagination}.
 *
 * @param <E> the type of data encapsulated by this data model.
 */
public abstract class PaginatedDataModel<E> extends DataModel<E> implements Pagination
{
    private int rowIndex = -1;
    private int rowCount = 0;
    private List<E> wrappedData;
    private Pagination paginationHelper;
    private boolean paginationEnabled;

    protected PaginatedDataModel(boolean paginationEnabled)
    {
        this.paginationEnabled = paginationEnabled;
    }

    /**
     * Identifies if the current model supports pagination or not.
     *
     * @return true if this instance supports pagination, false otherwise.
     */
    public boolean isPaginationEnabled()
    {
        return paginationEnabled;
    }

    protected Pagination getPagination()
    {
        return paginationHelper;
    }

    protected void setPagination(Pagination paginationHelper)
    {
        this.paginationHelper = paginationHelper;
    }

    /**
     * Load the data objects for the specified range.
     *
     * @param start    the start index of the range (inclusive)
     * @param pageSize the maximum number of items to fetch.
     * @return a list of data objects fetched from the backend.
     * @throws rogue.app.framework.AppRuntimeException
     *          if the operation fails.
     */
    protected abstract List<E> loadData(int start, int pageSize) throws AppRuntimeException;

    /**
     * Clear the local state variables.
     */
    protected void clearData()
    {
        this.rowCount = 0;
        this.wrappedData = null;
    }

    /**
     * Internal method that checks if the wrappedData is null or not and call the loadData method with the appropriate
     * arguments.
     */
    protected void loadData()
    {
        if (wrappedData == null)
        {
            wrappedData = loadData(isPaginationEnabled() ? getItemStartIndex() : 0,
                                   isPaginationEnabled() ? getItemsPerPage() : -1);
            rowCount = wrappedData == null ? 0 : wrappedData.size();
        }
    }

    /* ============================================================================================================== *
     *                                                                                                                *
     *  Implementation of the DataModel methods                                                                       *
     *                                                                                                                *
     * ============================================================================================================== *
     */

    @Override
    public boolean isRowAvailable()
    {
        loadData();
        return wrappedData != null && rowIndex >= 0 && rowIndex < wrappedData.size();
    }

    @Override
    public int getRowCount()
    {
        loadData();
        return rowCount;
    }

    @Override
    public E getRowData()
    {
        loadData();
        return (wrappedData != null && isRowAvailable()) ? wrappedData.get(rowIndex) : null;
    }

    @Override
    public int getRowIndex()
    {
        loadData();
        return rowIndex;
    }

    @Override
    public void setRowIndex(int index)
    {
        if (rowIndex < -1)
        {
            throw new IllegalArgumentException();
        }
        int old = this.rowIndex;
        this.rowIndex = index;

        loadData();

        if (wrappedData != null && this.rowIndex != old)
        {
            DataModelListener[] listeners = getDataModelListeners();
            if (listeners != null)
            {
                Object rowData = null;
                if (isRowAvailable())
                {
                    rowData = getRowData();
                }
                DataModelEvent event =
                        new DataModelEvent(this, index, rowData);
                for (DataModelListener listener : listeners)
                {
                    if (null != listener)
                    {
                        listener.rowSelected(event);
                    }
                }
            }
        }
    }

    @Override
    public Object getWrappedData()
    {
        loadData();
        return wrappedData;
    }

    @Override
    public void setWrappedData(Object data)
    {
        clearData();
        if (data == null)
        {
            wrappedData = null;
            setRowIndex(-1);
        }
        else
        {
            wrappedData = (List<E>) data;
            this.rowIndex = -1;
            setRowIndex(0);
        }
    }

    /* ============================================================================================================== *
     *                                                                                                                *
     *  Implementation of the Pagination methods                                                                      *
     *                                                                                                                *
     * ============================================================================================================== *
     */

    @Override
    public int getItemCount()
    {
        return paginationHelper != null ? paginationHelper.getItemCount() : 0;
    }

    @Override
    public void setItemCount(int itemCount)
    {
        if (paginationHelper != null)
        {
            paginationHelper.setItemCount(itemCount);
        }
    }

    @Override
    public int getItemsPerPage()
    {
        return paginationHelper != null ? paginationHelper.getItemsPerPage() : 0;
    }

    @Override
    public void setItemsPerPage(int itemsPerPage)
    {
        if (paginationHelper != null)
        {
            paginationHelper.setItemsPerPage(itemsPerPage);
        }
    }

    @Override
    public int getPagesCount()
    {
        return paginationHelper != null ? paginationHelper.getPagesCount() : 0;
    }

    @Override
    public int getPageIndex()
    {
        return paginationHelper != null ? paginationHelper.getPageIndex() : 0;
    }

    @Override
    public void setPageIndex(int pageIndex) throws IllegalStateException, IndexOutOfBoundsException
    {
        if (paginationHelper != null)
        {
            clearData();
            paginationHelper.setPageIndex(pageIndex);
        }
    }

    @Override
    public int getItemStartIndex()
    {
        return paginationHelper != null ? paginationHelper.getItemStartIndex() : 0;
    }

    @Override
    public int getItemEndIndex()
    {
        return paginationHelper != null ? paginationHelper.getItemEndIndex() : 0;
    }

    @Override
    public boolean isFirstPageAvailable()
    {
        return paginationHelper != null && paginationHelper.isFirstPageAvailable();
    }

    @Override
    public void firstPage()
    {
        if (paginationHelper != null)
        {
            clearData();
            paginationHelper.firstPage();
        }
    }

    @Override
    public boolean isPreviousPageAvailable()
    {
        return paginationHelper != null && paginationHelper.isPreviousPageAvailable();
    }

    @Override
    public void previousPage()
    {
        if (paginationHelper != null)
        {
            clearData();
            paginationHelper.previousPage();
        }
    }

    @Override
    public boolean isNextPageAvailable()
    {
        return paginationHelper != null && paginationHelper.isNextPageAvailable();
    }

    @Override
    public void nextPage()
    {
        if (paginationHelper != null)
        {
            clearData();
            paginationHelper.nextPage();
        }
    }

    @Override
    public boolean isLastPageAvailable()
    {
        return paginationHelper != null && paginationHelper.isLastPageAvailable();
    }

    @Override
    public void lastPage()
    {
        if (paginationHelper != null)
        {
            clearData();
            paginationHelper.lastPage();
        }
    }
}
