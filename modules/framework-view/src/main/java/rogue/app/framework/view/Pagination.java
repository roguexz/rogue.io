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

import java.io.Serializable;

/**
 * The <code>Pagination</code> interface captures the most common set of paging operations expected out large data sets.
 * <p/>
 * Implementations can decide whether or not to support the setXXX(...) operations, depending on their requirements.
 */
public interface Pagination extends Serializable
{
    /**
     * The default number of items per page.
     */
    public int DEFAULT_PAGE_SIZE = 10;

    /**
     * Get the total number of items available.
     *
     * @return the total number of items available.
     */
    public int getItemCount();

    /**
     * Set the total number of items available.
     *
     * @param itemCount the total number of items available.
     */
    public void setItemCount(int itemCount);

    /**
     * Get the number of items to be displayed per page. By default it is set to the value of {@link #DEFAULT_PAGE_SIZE}
     *
     * @return the number of items to be displayed per page.
     */
    public int getItemsPerPage();

    /**
     * Set the number of items to be displayed per page.
     *
     * @param itemsPerPage the number of items to be displayed per page.
     */
    public void setItemsPerPage(int itemsPerPage);

    /**
     * Get the number of pages available for paginating through the associated collection.
     *
     * @return the total number of pages available for the view.
     */
    public int getPagesCount();

    /**
     * Get the index of the current page being displayed.
     *
     * @return the zero based index of the current page being displayed.
     */
    public int getPageIndex();

    /**
     * Set the index of the current page being displayed.
     *
     * @param pageIndex the zero based index of the current page being displayed.
     * @throws IllegalStateException     if the count is not set.
     * @throws IndexOutOfBoundsException if ( pageIndex * itemsPerPage > (count-1) )
     */
    public void setPageIndex(int pageIndex) throws IllegalStateException, IndexOutOfBoundsException;

    /**
     * Get the index (in the collection) of the first item on the current page.
     *
     * @return the index (in the collection) of the first item on the current page.
     */
    public int getItemStartIndex();

    /**
     * Get the index (in the collection) of the last item on the current page.
     *
     * @return the index (in the collection) of the last item on the current page.
     */
    public int getItemEndIndex();

    /**
     * Utility method for identifying if the user can navigate to the first page or not.
     *
     * @return true if the user can navigate, false otherwise.
     */
    public boolean isFirstPageAvailable();

    /**
     * Set the page index to the first page. This is a no-op if the user is already on the first page.
     */
    public void firstPage();

    /**
     * Utility method for identifying if the user can navigate to the previous page or not.
     *
     * @return true if the user can navigate, false otherwise.
     */
    public boolean isPreviousPageAvailable();

    /**
     * Set the page index to the previous page. This is a no-op if the user is already on the first page.
     */
    public void previousPage();

    /**
     * Utility method for identifying if the user can navigate to the next page or not.
     *
     * @return true if the user can navigate, false otherwise.
     */
    public boolean isNextPageAvailable();

    /**
     * Set the page index to the next page. This is a no-op if the user is already on the last page.
     */
    public void nextPage();

    /**
     * Utility method for identifying if the user can navigate to the last page or not.
     *
     * @return true if the user can navigate, false otherwise.
     */
    public boolean isLastPageAvailable();

    /**
     * Set the page index to the last page. This is a no-op if the user is already on the last page.
     */
    public void lastPage();
}
