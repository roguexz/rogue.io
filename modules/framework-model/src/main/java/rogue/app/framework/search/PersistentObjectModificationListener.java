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

package rogue.app.framework.search;

import rogue.app.framework.internal.util.ClassUtils;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

/**
 * An entity modification listener, that updates the search indices for objects marked with the
 * {@link SearchIndex} annotation.
 * <p/>
 * <i>Being a generic listener, it is defined in the META-INF/orm.xml file.</i>
 */
public class PersistentObjectModificationListener
{
    @PostPersist
    @PostUpdate
    @SuppressWarnings("unchecked")
    protected void entityPersisted(Object entity)
    {
        if (entity != null && entity instanceof AppObject &&
                ClassUtils.getAnnotation(entity.getClass(), SearchIndex.class) != null)
        {
            SearchIndexer.queue((Class<? extends PersistentObject>) entity.getClass(), true,
                                ((PersistentObject<?>) entity).getId());
        }
    }

    @PostRemove
    @SuppressWarnings("unchecked")
    protected void entityRemoved(Object entity)
    {
        if (entity != null && entity instanceof AppObject &&
                ClassUtils.getAnnotation(entity.getClass(), SearchIndex.class) != null)
        {
            SearchIndexer.queue((Class<? extends PersistentObject>) entity.getClass(), false,
                                ((PersistentObject<?>) entity).getId());
        }
    }
}
