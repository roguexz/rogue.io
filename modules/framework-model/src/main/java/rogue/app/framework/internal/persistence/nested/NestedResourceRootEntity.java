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

import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.PersistentEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In order to provide a non-obtrusive support for all the NestedResource objects, we need to create the root
 * entity details in a separate table. This is a framework class and should not be extended.
 */
@Entity
@NamedQueries(
        @NamedQuery(name = "NestedResourceRootEntity.findRoot",
                    query = "SELECT e FROM NestedResourceRootEntity e WHERE e.rootClassName = :queryString")
)
@EntityImplementationFor(NestedResourceRoot.class)
public class NestedResourceRootEntity extends PersistentEntity<NestedResourceRoot> implements NestedResourceRoot
{
    private static final String CLASS_NAME = NestedResourceRootEntity.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Column(nullable = false)
    private String rootClassName;
    private int counter = 0;
    //
    // Since the following values are constants for now, we do not need to store them in the database.
    //
    private static final long nv = 2;
    private static final long dv = 1;
    private static final long snv = 3;
    private static final long sdv = 1;
    private static final int idx = 0;

    @Transient
    private transient Class<? extends AppObject> rootClass;

    public NestedResourceRootEntity()
    {
        super();
    }

    public NestedResourceRootEntity(NestedResourceRootEntity entity)
    {
        super(entity);
        /*
        this.nv = entity.getNodeNumerator();
        this.dv = entity.getNodeDenominator();
        this.snv = entity.getSiblingNodeNumerator();
        this.sdv = entity.getSiblingNodeDenominator();
        this.idx = entity.getNodeIndex();
        */
        if (entity != null)
        {
            this.counter = entity.getChildCounter();
            this.rootClassName = entity.rootClassName;
        }
    }

    @Override
    public Class<? extends AppObject> getRootClass()
    {
        final String METHOD_NAME = "getRootClass";
        if (rootClass == null)
        {
            if (rootClassName != null)
            {
                try
                {
                    rootClass = (Class<? extends AppObject>) Thread.currentThread().getContextClassLoader()
                                                                   .loadClass(rootClassName);
                }
                catch (ClassNotFoundException e)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to load class", e);
                }
            }
        }
        return rootClass;
    }

    @Override
    public void setRootClass(Class<? extends AppObject> rootClass)
    {
        if (isMutable())
        {
            String newClassName = rootClass == null ? null : rootClass.getName();
            if (!Objects.equals(rootClassName, newClassName))
            {
                this.rootClassName = newClassName;
                markDirty();
            }
        }
    }

    @Override
    public long getNodeNumerator()
    {
        return nv;
    }

    @Override
    public long getNodeDenominator()
    {
        return dv;
    }

    @Override
    public long getSiblingNodeNumerator()
    {
        return snv;
    }

    @Override
    public long getSiblingNodeDenominator()
    {
        return sdv;
    }

    @Override
    public int getNodeIndex()
    {
        return idx;
    }

    @Override
    public void setNodeIndex(int idx)
    {
        throw new AppRuntimeException("Index values of root nodes cannot be set.");
    }

    @Override
    public int getChildCounter()
    {
        return counter;
    }

    @Override
    public int incrementChildCounter() throws AppRuntimeException
    {
        if (!isMutable())
        {
            throw new AppRuntimeException("This method can be invoked only on a mutable instance.");
        }
        counter++;
        markDirty();
        return counter;
    }
}
