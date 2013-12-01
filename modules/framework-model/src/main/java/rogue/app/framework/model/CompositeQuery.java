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
 * The composite query can be used to identify objects associated with a given parent resource.
 */
public class CompositeQuery extends AbstractQuery
{
    private final AppObject parent;

    /**
     * Creates a new instance of <code>CompositeQuery</code>.
     *
     * @param parent the parent object for the the resources that are being queried.
     */
    public CompositeQuery(AppObject parent)
    {
        super("Composite Query");
        this.parent = parent;
    }

    /**
     * Get the parent object for the the resources that are being queried.
     *
     * @return the parent object for the the resources that are being queried.
     */
    public AppObject getParent()
    {
        return parent;
    }
}
