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

package rogue.app.framework.view;

/**
 * A concrete implementation of the <code>Pagination</code> interface. The <code>PaginationHelper</code> class provides
 * a simple implementation and can be used internally by other classes that wish to support the <code>Pagination</code>
 * interface.
 */
public class PaginationHelper implements Pagination
{
    private int itemCount = -1;
    private int itemsPerPage = DEFAULT_PAGE_SIZE;
    private int pageIndex;

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount()
    {
        return itemCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setItemCount(int itemCount)
    {
        this.itemCount = itemCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemsPerPage()
    {
        return itemsPerPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setItemsPerPage(int itemsPerPage)
    {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPagesCount()
    {
        return getItemCount() > 0 ? (int) (Math.ceil((float) getItemCount() / (float) getItemsPerPage())) : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageIndex()
    {
        return pageIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageIndex(int pageIndex) throws IllegalStateException, IndexOutOfBoundsException
    {
        if (getItemCount() == -1)
        {
            throw new IllegalStateException("Item count must be set before invoking this operation.");
        }

        if (pageIndex * getItemsPerPage() > (getItemCount() - 1))
        {
            throw new IndexOutOfBoundsException("Page index cannot be set to point beyond the total item count.");
        }

        this.pageIndex = pageIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemStartIndex()
    {
        return getPageIndex() * getItemsPerPage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemEndIndex()
    {
        int idx = ((getPageIndex() + 1) * getItemsPerPage()) - 1;
        return idx < getItemCount() ? idx : getItemCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirstPageAvailable()
    {
        return getPageIndex() != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firstPage()
    {
        if (isFirstPageAvailable())
        {
            setPageIndex(0);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPreviousPageAvailable()
    {
        return getPageIndex() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void previousPage()
    {
        if (isPreviousPageAvailable())
        {
            setPageIndex(getPageIndex() - 1);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNextPageAvailable()
    {
        return getPageIndex() < (getPagesCount() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nextPage()
    {
        if (isNextPageAvailable())
        {
            setPageIndex(getPageIndex() + 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLastPageAvailable()
    {
        return getPageIndex() != (getPagesCount() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lastPage()
    {
        if (isLastPageAvailable())
        {
            setPageIndex(getPagesCount() - 1);
        }
    }

}
