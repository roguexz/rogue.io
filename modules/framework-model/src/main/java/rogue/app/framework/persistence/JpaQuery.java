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

import rogue.app.framework.model.AbstractQuery;

import java.util.Map;

/**
 * A concrete implementation of the {@link rogue.app.framework.model.Query} interface that supports JPA style queries.
 */
public class JpaQuery extends AbstractQuery
{
    public static final String QUERY_TYPE = "JPA Query";
    private String query;
    private boolean namedQuery;
    private Map<String, Object> queryParams;

    public JpaQuery(String query, boolean namedQuery, Map<String, Object> queryParams)
    {
        super(QUERY_TYPE);
        this.query = query;
        this.namedQuery = namedQuery;
        this.queryParams = queryParams;
    }

    public String getQuery()
    {
        return query;
    }

    public boolean isNamedQuery()
    {
        return namedQuery;
    }

    public Map<String, Object> getQueryParams()
    {
        return queryParams;
    }
}
