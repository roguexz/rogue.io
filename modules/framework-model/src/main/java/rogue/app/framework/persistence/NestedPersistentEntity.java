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

import com.google.appengine.api.datastore.Key;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.internal.persistence.nested.NestedResourceRoot;
import rogue.app.framework.model.NestedResource;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.annotations.ParentRef;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.*;

/**
 * Extension of the PersistentEntity that implements the NestedResource interface and takes care of most of the boiler
 * place code away from the actual implementations.
 * <p/>
 * The <code>NestedPersistentEntity</code> class provides a concrete implementation of using rational numbers for keying
 * nested sets.
 *
 * @see <a href="http://arxiv.org/abs/0806.3115">Using rational numbers to key nested sets, by Dan Hazel</a>
 */
@Entity
@MappedSuperclass
public abstract class NestedPersistentEntity<T extends PersistentObject<T> & NestedResource<T>>
        extends PersistentEntity<T>
        implements NestedResource<T>, NestedSet
{

    ///
    /// Queries
    ///
    private static final String GET_IMMEDIATE_CHILDREN_QUERY = "SELECT e FROM %s e WHERE e.parentKey = :parentKey";
    private static final String GET_IMMEDIATE_CHILDREN_COUNT_QUERY =
            "SELECT COUNT(e) FROM %s e WHERE e.parentKey = :parentKey";
    /*
     * GAE does not support operators during query time. Hence the need to store the values explicitly.
     *
    private static final String GET_ALL_CHILDREN_QUERY =
            "SELECT e FROM %s e WHERE (:nvMe * e.dv) < (e.nv * :dvMe) AND (e.nv * :sdvMe) < (:snvMe * e.dv)";
    private static final String GET_ALL_CHILDREN_COUNT_QUERY =
            "SELECT COUNT(e) FROM %s e WHERE (:nvMe * e.dv) < (e.nv * :dvMe) AND (e.nv * :sdvMe) < (:snvMe * e.dv)";
     */
    private static final String GET_ALL_CHILDREN_QUERY =
            "SELECT e FROM %s e WHERE (e.nodeValue > :meNodeValue) AND (e.nodeValue < :meSibNodeValue)";
    private static final String GET_ALL_CHILDREN_COUNT_QUERY =
            "SELECT COUNT(e) FROM %s e WHERE (e.nodeValue > :meNodeValue) AND (e.nodeValue < :meSibNodeValue)";

    private long nv;
    private long dv;
    private long snv;
    private long sdv;
    private double nodeValue;
    private double sibNodeValue;
    // Last child index issued by this entity. This value is always >= child count.
    private int counter;
    // A value of -1 => this is a newly created resource.
    private int idx;

    @ParentRef
    protected Key parentKey;

    @Transient
    private boolean parentChanged;
    @Transient
    private boolean recomputeChildrenBounds;

    protected NestedPersistentEntity()
    {
        super();
        counter = 0;
        idx = -1;
    }

    protected NestedPersistentEntity(NestedPersistentEntity<T> entity)
    {
        super(entity);
        if (entity == null)
        {
            counter = 0;
            idx = -1;
        }
    }

    @Override
    public long getNodeNumerator()
    {
        return nv;
    }

    private void setNodeNumerator(long numerator)
    {
        if (isMutable() && this.nv != numerator)
        {
            this.nv = numerator;
            markDirty();
        }
    }

    @Override
    public long getNodeDenominator()
    {
        return dv;
    }

    private void setNodeDenominator(long denominator)
    {
        if (isMutable() && this.dv != denominator)
        {
            this.dv = denominator;
            markDirty();
        }
    }

    @Override
    public long getSiblingNodeNumerator()
    {
        return snv;
    }

    private void setSiblingNodeNumerator(long siblingNumerator)
    {
        if (isMutable() && this.snv != siblingNumerator)
        {
            this.snv = siblingNumerator;
            markDirty();
        }
    }

    @Override
    public long getSiblingNodeDenominator()
    {
        return sdv;
    }

    private void setSiblingNodeDenominator(long siblingDenominator)
    {
        if (isMutable() && this.sdv != siblingDenominator)
        {
            this.sdv = siblingDenominator;
            markDirty();
        }
    }

    @Override
    public int getNodeIndex()
    {
        return idx;
    }

    @Override
    public void setNodeIndex(int idx)
    {
        if (isMutable() && this.idx != idx)
        {
            this.idx = idx;
            markDirty();
        }
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

    public Key getParentKey()
    {
        return parentKey;
    }

    public void setParentKey(Key parentKey)
    {
        if (isMutable() && !Objects.equals(this.parentKey, parentKey))
        {
            this.parentKey = parentKey;
            parentChanged = true;
            markDirty();
        }
    }

    ///
    /// Non-entity implementation
    ///

    @Override
    public T getParent()
    {
        return getController().findById(getParentKey());
    }

    @Override
    public void setParent(T parent)
    {
        setParentKey(PersistenceUtils.getEntityKey(parent));
    }

    @Override
    public List<T> getChildren()
    {
        return getChildren(false);
    }

    @Override
    public int getChildCount()
    {
        return getChildCount(false);
    }

    @Override
    public List<T> getChildren(boolean recursive)
    {
        String query = String.format(recursive ? GET_ALL_CHILDREN_QUERY : GET_IMMEDIATE_CHILDREN_QUERY,
                                     this.getClass().getSimpleName());
        Map<String, Object> params = new HashMap<>(3);
        params.put("parentKey", getId());
        params.put("meNodeValue", this.nodeValue);
        params.put("meSibNodeValue", this.sibNodeValue);
        /*
        params.put("nvMe", getNodeNumerator());
        params.put("dvMe", getNodeDenominator());
        params.put("snvMe", getSiblingNodeNumerator());
        params.put("sdvMe", getSiblingNodeDenominator());
        */

        return getController().executeQueryListResult(query, 0, -1, params);
    }

    @Override
    public int getChildCount(boolean recursive)
    {
        String query = String.format(recursive ? GET_ALL_CHILDREN_COUNT_QUERY : GET_IMMEDIATE_CHILDREN_COUNT_QUERY,
                                     this.getClass().getSimpleName());
        Map<String, Object> params = new HashMap<>(3);
        params.put("parentKey", getId());
        params.put("meNodeValue", this.nodeValue);
        params.put("meSibNodeValue", this.sibNodeValue);
        /*
        params.put("nvMe", getNodeNumerator());
        params.put("dvMe", getNodeDenominator());
        params.put("snvMe", getSiblingNodeNumerator());
        params.put("sdvMe", getSiblingNodeDenominator());
        */

        Long val = getController().executeQuerySingleResult(query, params);
        return val == null ? 0 : val.intValue();
    }

    @SuppressWarnings("unchecked")
    protected void executeBeforePersist(boolean entityDeleted)
    {
        super.executeBeforePersist(entityDeleted);

        if (parentChanged || this.idx == -1)
        {
            updateBounds((NestedPersistentEntity<T>) getParent());
        }
    }

    @SuppressWarnings("unchecked")
    protected void executeAfterPersist(boolean entityDeleted)
    {
        super.executeAfterPersist(entityDeleted);

        if (recomputeChildrenBounds)
        {
            List<T> childNodesToPersist = new ArrayList<>();
            // Update the children recursively.
            List<T> children = getChildren(false);
            if (children != null && !children.isEmpty())
            {
                for (T child : children)
                {
                    if (child instanceof NestedPersistentEntity)
                    {
                        child = child.getMutableInstance();
                        NestedPersistentEntity<T> childEntity = (NestedPersistentEntity<T>) child;
                        childEntity.updateBounds(this);
                        if (childEntity.isDirty())
                        {
                            childNodesToPersist.add(child);
                        }
                    }
                }
            }

            if (!childNodesToPersist.isEmpty())
            {
                for (T child : childNodesToPersist)
                {
                    getController().save(child);
                }
            }
        }

        parentChanged = false;
        recomputeChildrenBounds = false;
    }

    @SuppressWarnings("unchecked")
    private JpaController<T> getController()
    {
        return (JpaController<T>) JpaController.getController(this.getClass());
    }

    /**
     * Update the bounds for this node. This method should be invoked only during the pre-persist / pre-update phase.
     */
    @SuppressWarnings("unchecked")
    private void updateBounds(NestedPersistentEntity<T> parentEntity)
    {
        Map<String, Object> params = new HashMap<>(1);
        params.put("queryString", this.getClass().getName());
        JpaController<NestedResourceRoot> entityRootController = JpaController.getController(NestedResourceRoot.class);
        NestedResourceRoot entityRoot = entityRootController
                .executeNamedQuerySingleResult("NestedResourceRootEntity.findRoot", params);

        if (entityRoot == null)
        {
            // Create a new entity.
            entityRoot = entityRootController.getNewInstance();
            entityRoot.setRootClass(this.getClass());
            entityRootController.save(entityRoot);

            // query it again.
            entityRoot = entityRootController.executeNamedQuerySingleResult("NestedResourceRootEntity.findRoot",
                                                                            params);

            if (entityRoot == null)
            {
                throw new AppRuntimeException(
                        "Cannot proceed with entity persistence. Entity Root object is null though it was just created.");
            }
        }

        long pNv, pDv, pSNv, pSDv;

        if (idx == -1 && !parentChanged)
        {
            // Implies newly created entity under the root. Get hold of the entity root and add this to it.
            entityRoot = entityRoot.getMutableInstance();
            setNodeIndex(entityRoot.incrementChildCounter());
            entityRootController.save(entityRoot);

            pNv = entityRoot.getNodeNumerator();
            pDv = entityRoot.getNodeDenominator();
            pSNv = entityRoot.getSiblingNodeNumerator();
            pSDv = entityRoot.getSiblingNodeDenominator();

        }
        else if (parentChanged)
        {
            // Normal updates. This should only be honored if the parent was changed.
            if (parentEntity == null)
            {
                // entity was moved out to the root entity
                // reset this object's idx value
                entityRoot = entityRoot.getMutableInstance();
                setNodeIndex(entityRoot.incrementChildCounter());
                entityRootController.save(entityRoot);

                pNv = entityRoot.getNodeNumerator();
                pDv = entityRoot.getNodeDenominator();
                pSNv = entityRoot.getSiblingNodeNumerator();
                pSDv = entityRoot.getSiblingNodeDenominator();

            }
            else
            {
                // update idx value and also the parent's counters
                parentEntity = (NestedPersistentEntity<T>) parentEntity.getMutableInstance();
                setNodeIndex(parentEntity.incrementChildCounter());
                getController().save((T) parentEntity);

                pNv = parentEntity.getNodeNumerator();
                pDv = parentEntity.getNodeDenominator();
                pSNv = parentEntity.getSiblingNodeNumerator();
                pSDv = parentEntity.getSiblingNodeDenominator();

            }
        }
        else
        {

            // just verify if the bounds have changed or not. Initialize the variables.
            pNv = parentEntity.getNodeNumerator();
            pDv = parentEntity.getNodeDenominator();
            pSNv = parentEntity.getSiblingNodeNumerator();
            pSDv = parentEntity.getSiblingNodeDenominator();
        }

        // Recompute the bounds and verify if they have changed or not.
        recomputeChildrenBounds = computeBounds(pNv, pDv, pSNv, pSDv);
    }

    private boolean computeBounds(long pNv, long pDv, long pSNv, long pSDv)
    {
        boolean valueChanged = false;
        if (isMutable())
        {
            int c = getNodeIndex();
            long val = pNv + c * pSNv;
            if (val != getNodeNumerator())
            {
                valueChanged = true;
                setNodeNumerator(val);
            }

            val = pDv + c * pSDv;
            if (val != getNodeDenominator())
            {
                valueChanged = true;
                setNodeDenominator(val);
            }

            val = pNv + (c + 1) * pSNv;
            if (val != getSiblingNodeNumerator())
            {
                valueChanged = true;
                setSiblingNodeNumerator(val);
            }

            val = pDv + (c + 1) * pSDv;
            if (val != getSiblingNodeDenominator())
            {
                valueChanged = true;
                setSiblingNodeDenominator(val);
            }

            this.nodeValue = ((double) getNodeNumerator()) / ((double) getNodeDenominator());
            this.sibNodeValue = ((double) getSiblingNodeNumerator()) / ((double) getSiblingNodeDenominator());
        }
        return valueChanged;
    }

    ///
    /// Overrides for equals & hashCode
    ///


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof NestedPersistentEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        NestedPersistentEntity that = (NestedPersistentEntity) o;

        if (counter != that.counter)
        {
            return false;
        }
        if (dv != that.dv)
        {
            return false;
        }
        if (idx != that.idx)
        {
            return false;
        }
        if (nv != that.nv)
        {
            return false;
        }
        if (sdv != that.sdv)
        {
            return false;
        }
        if (snv != that.snv)
        {
            return false;
        }
        if (parentKey != null ? !parentKey.equals(that.parentKey) : that.parentKey != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (int) (nv ^ (nv >>> 32));
        result = 31 * result + (int) (dv ^ (dv >>> 32));
        result = 31 * result + (int) (snv ^ (snv >>> 32));
        result = 31 * result + (int) (sdv ^ (sdv >>> 32));
        result = 31 * result + counter;
        result = 31 * result + idx;
        result = 31 * result + (parentKey != null ? parentKey.hashCode() : 0);
        return result;
    }
}
