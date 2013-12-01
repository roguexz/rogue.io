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

package rogue.app.framework.model;

/**
 * A generic query interface that can be used with an application object controller while finding for data.
 */
public interface Query
{
    /**
     * The type of query represented by this object.
     *
     * @return a string representation of the query type represented by this object.
     */
    public String getType();

    /**
     * Get the starting index, within the matching results, after which the results should be returned.
     *
     * @return the starting index, within the matching results, after which the results should be returned.
     */
    public int getStartIndex();

    /**
     * Set the starting index, within the matching results, after which the results should be returned.
     *
     * @param index the starting index, within the matching results, after which the results should be returned.
     */
    public void setStartIndex(int index);

    /**
     * Get the maximum number of results that can be returned.
     *
     * @return the maximum number of results that can be returned.
     */
    public int getMaxResults();

    /**
     * Set the maximum number of results that can be returned.
     *
     * @param results the maximum number of results that can be returned.
     */
    public void setMaxResults(int results);

}
