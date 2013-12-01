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

package rogue.app.framework.internal.persistence.nested;

import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.NestedSet;

/**
 * Framework API for providing support for {@link rogue.app.framework.model.NestedResource NestedResource}
 * implementations. For implementing nested sets, we need a root node, under which all other resources are nested.
 * In most of the cases, having a root node might not be suitable from a design perspective. To avoid mandating a
 * design restriction the root node information is stored in a separate table. Additionally, in order to reuse most of
 * the framework code, it is necessary to make NestedResource extend from AppObject.
 * <p/>
 * The <code>NestedResourceRoot</code> extends the {@link rogue.app.framework.persistence.NestedSet NestedSet} with the
 * only sub clause that setting the child index values for root nodes, throws an <code>AppRuntimeException</code>.
 *
 * @see rogue.app.framework.persistence.NestedPersistentEntity
 */
public interface NestedResourceRoot extends PersistentObject<NestedResourceRoot>, NestedSet
{
    /**
     * Get the entity class for which this root has been identified.
     *
     * @return the entity class for which this root has been identified.
     */
    public Class<? extends AppObject> getRootClass();

    /**
     * Set the entity class for which this root has been identified.
     *
     * @param rootClass the entity class for which this root has been identified.
     */
    public void setRootClass(Class<? extends AppObject> rootClass);

}
