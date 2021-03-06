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

import java.io.Serializable;

/**
 * An abstract implementation of the {@link Query} interface.
 */
public abstract class AbstractQuery implements Query, Serializable
{
    private String type;
    private int startIndex;
    private int maxResults = 20;

    /**
     * Constructs a new instance of <code>AbstractQuery</code> with the given query type information.
     *
     * @param type the type of query represented by the concrete implementation.
     */
    protected AbstractQuery(String type)
    {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType()
    {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStartIndex()
    {
        return startIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartIndex(int startIndex)
    {
        this.startIndex = startIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxResults()
    {
        return maxResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxResults(int maxResults)
    {
        this.maxResults = maxResults;
    }
}
