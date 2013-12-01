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
import rogue.app.framework.internal.persistence.attr.AttributesManager;
import rogue.app.framework.model.AuditableResource;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.model.attr.CustomizedAttributes;
import rogue.app.framework.model.attr.Layer;
import rogue.app.framework.security.AppPrincipal;
import rogue.app.framework.security.UserContext;

import javax.persistence.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the parent class of all persistent entities within the system.
 */
@Entity
@MappedSuperclass
public abstract class PersistentEntity<T extends PersistentObject<T>> implements PersistentObject<T>, AuditableResource
{
    private static final String CLASS_NAME = PersistentEntity.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key id;

    private Date creationDate;
    private String createdBy;
    private Date lastModificationDate;
    private String lastModifiedBy;

    @Transient
    private boolean mutable;
    @Transient
    private boolean dirty;
    @Transient
    private T baseResource;
    @Transient
    private AttributesManager attributesManager;

    /**
     * Default constructor.
     */
    protected PersistentEntity()
    {
    }

    /**
     * Construct a new object based on the properties of the passed in object. This constructor implicitly sets the
     * mutable state to true.
     *
     * @param entity the base resource.
     */
    @SuppressWarnings("unchecked")
    protected PersistentEntity(PersistentEntity<T> entity)
    {
        final String METHOD_NAME = "<ctor>(PersistentEntity)";

        this.mutable = true;
        this.baseResource = (T) entity;

        if (entity != null)
        {
            Class clazz = entity.getClass();
            while (clazz != null && PersistentEntity.class.isAssignableFrom(clazz))
            {
                // Get the declared fields.
                Field[] fields = clazz.getDeclaredFields();
                for (Field f : fields)
                {
                    int fieldModifiers = f.getModifiers();
                    // Ignore fields that are:
                    // 1. marked as transient - JPA as well as field lifecycle
                    // 2. Static or final in nature
                    // 3. field name begins with jdo - those are internal to JDO.
                    if (Modifier.isTransient(fieldModifiers) || Modifier.isFinal(fieldModifiers)
                            || Modifier.isStatic(fieldModifiers) || f.getName().startsWith("jdo")
                            || f.isAnnotationPresent(Transient.class))
                    {
                        continue;
                    }

                    // Get the value for this field from the entity object and copy it over to
                    // the current object.
                    if (LOGGER.isLoggable(Level.FINEST))
                    {
                        LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                                    "Copying over the field value for: " + f.getName());
                    }

                    try
                    {
                        // Suppress accessibility checks
                        f.setAccessible(true);

                        f.set(this, f.get(entity));

                        // Enforce accessibility checks.
                        f.setAccessible(false);
                    }
                    catch (IllegalAccessException e)
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "This should not occur.", e);
                    }
                }

                // Move to the parent clazz
                clazz = clazz.getSuperclass();
            }
        }
    }

    /**
     * Get this entity's id.
     *
     * @return the unique id for this entity object within the system.
     */
    @Override
    public Key getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdAsString()
    {
        return id != null ? Long.toString(id.getId()) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCreationDate()
    {
        return creationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCreationDate(Date creationDate)
    {
        if (isMutable() && !Objects.equals(this.creationDate, creationDate))
        {
            this.creationDate = creationDate;
            markDirty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCreatedBy()
    {
        return createdBy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCreatedBy(String createdBy)
    {
        if (isMutable() && !Objects.equals(this.createdBy, createdBy))
        {
            this.createdBy = createdBy;
            markDirty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getLastModificationDate()
    {
        return lastModificationDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastModificationDate(Date lastModificationDate)
    {
        if (isMutable() && !Objects.equals(this.lastModificationDate, lastModificationDate))
        {
            this.lastModificationDate = lastModificationDate;
            markDirty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastModifiedBy()
    {
        return lastModifiedBy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastModifiedBy(String lastModifiedBy)
    {
        if (isMutable() && !Objects.equals(this.lastModifiedBy, lastModifiedBy))
        {
            this.lastModifiedBy = lastModifiedBy;
            markDirty();
        }
    }

    @Override
    public boolean isMutable()
    {
        return mutable;
    }

    @Override
    public T getBaseResource()
    {
        return baseResource;
    }

    @Override
    public boolean isDirty()
    {
        return dirty || (attributesManager != null && attributesManager.isDirty());
    }

    /**
     * Utilitarian method for sub-classes to mark this instance as dirty. This method is effective if and only if the
     * current object has been marked as mutable.
     */
    protected void markDirty()
    {
        if (isMutable())
        {
            lastModificationDate = Calendar.getInstance().getTime();
            this.dirty = true;
        }
    }

    /**
     * Utilitarian method for sub-classes to mark this object as mutable.
     *
     * @param value true to mark the current object as mutable.
     */
    protected void setMutable(boolean value)
    {
        this.mutable = value;
    }

    /* ============================================================================================================== *
     *                                                                                                                *
     *                                   Implementation of Attributes                                                 *
     * Instead of implementing all over the place, it makes it convenient to implement this feature as part of the    *
     * base class itself.                                                                                             *
     * ============================================================================================================== *
     */

    private void initLocalAttributeCache()
    {
        if (attributesManager == null)
        {
            attributesManager = new AttributesManager(this);
        }
    }

    /**
     * Convenience method to get hold of the delegate object handling the implementation of
     * {@link rogue.app.framework.model.attr.Attributes}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @return the delegate for handling attributes.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected Attributes getAttributesDelegate()
    {
        initLocalAttributeCache();
        return attributesManager;
    }

    /**
     * Convenience method to get hold of the delegate object handling the implementation of
     * {@link rogue.app.framework.model.attr.CustomizedAttributes}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @return the delegate for handling layered attributes.
     */
    protected CustomizedAttributes getCustomizedAttributesDelegate()
    {
        initLocalAttributeCache();
        return attributesManager;
    }

    /**
     * Convenience implementation of {@link rogue.app.framework.model.attr.Attributes#getAttributeNames()}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @return a list of attribute names.
     */
    public List<String> getAttributeNames()
    {
        initLocalAttributeCache();
        return attributesManager.getAttributeNames();
    }

    /**
     * Convenience implementation of {@link rogue.app.framework.model.attr.Attributes#isAttributeDefined(String)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name the name of the attribute
     * @return a named attribute associated with the current object or null if no such attribute was found.
     */
    public boolean isAttributeDefined(String name)
    {
        initLocalAttributeCache();
        return attributesManager.isAttributeDefined(name);
    }

    /**
     * Convenience implementation of {@link rogue.app.framework.model.attr.Attributes#getAttribute(String)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name the name of the attribute
     * @return a named attribute associated with the current object or null if no such attribute was found.
     */
    public String getAttribute(String name)
    {
        initLocalAttributeCache();
        return attributesManager.getAttribute(name);
    }

    /**
     * Convenience implementation of {@link rogue.app.framework.model.attr.Attributes#setAttribute(String, String)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name  name of the attribute.
     * @param value value of the attribute.
     */
    public void setAttribute(String name, String value)
    {
        initLocalAttributeCache();
        attributesManager.setAttribute(name, value);
    }

    /**
     * Convenience implementation of {@link rogue.app.framework.model.attr.Attributes#removeAttribute(String)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name the name of the attribute.
     */
    public void removeAttribute(String name)
    {
        initLocalAttributeCache();
        attributesManager.removeAttribute(name);
    }

    /**
     * Convenience implementation of
     * {@link rogue.app.framework.model.attr.CustomizedAttributes#isAttributeCustomized(String, rogue.app.framework.model.attr.Layer)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name  the name of the attribute.
     * @param layer the customization layer.
     * @return true if the specified named attribute exists and has been customized at the given layer.
     */
    public boolean isAttributeCustomized(String name, Layer layer)
    {
        initLocalAttributeCache();
        return attributesManager.isAttributeCustomized(name, layer);
    }

    /**
     * Convenience implementation of
     * {@link rogue.app.framework.model.attr.CustomizedAttributes#getAttribute(String, rogue.app.framework.model.attr.Layer)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name  the name of the attribute.
     * @param layer the layer where to start looking from.
     * @return the named attribute, or null if no such attribute was found.
     */
    public String getAttribute(String name, Layer layer)
    {
        initLocalAttributeCache();
        return attributesManager.getAttribute(name, layer);
    }

    /**
     * Convenience implementation of
     * {@link rogue.app.framework.model.attr.CustomizedAttributes#setAttribute(String, String, rogue.app.framework.model.attr.Layer)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name  name of the attribute.
     * @param value value of the attribute.
     * @param layer the customization layer at which the value should be persisted.
     */
    public void setAttribute(String name, String value, Layer layer)
    {
        initLocalAttributeCache();
        attributesManager.setAttribute(name, value, layer);
    }

    /**
     * Convenience implementation of
     * {@link rogue.app.framework.model.attr.CustomizedAttributes#removeAttribute(String, rogue.app.framework.model.attr.Layer)}.
     * A AppRuntimeException is raised if the object's class does not implement the interface.
     *
     * @param name  the name of the attribute.
     * @param layer the customization layer.
     */
    public void removeAttribute(String name, Layer layer)
    {
        initLocalAttributeCache();
        attributesManager.removeAttribute(name, layer);
    }


    /* ============================================================================================================== *
     *                                   End of Attributes Support                                                    *
     * ============================================================================================================== *
     */


    @PreExecute
    protected void executeBeforePersist(boolean entityDeleted)
    {
    }

    @SuppressWarnings("unchecked")
    @PostExecute
    protected void executeAfterPersist(boolean entityDeleted)
    {
        if (this instanceof Attributes)
        {
            // If the object has been deleted, then wipe out all the associated attributes as well.
            if (entityDeleted)
            {
                JpaController controller = JpaController.getController(Attribute.class);
                Map<String, Object> params = new HashMap<>(2);
                params.put("nameSpace", PersistenceUtils.getAppObjectNameSpace(this));
                params.put("appObjRefKey", getId());
                List<Attribute> list = controller.executeNamedQueryListResult("AttributeEntity.findAttributes.alLayers",
                                                                              0, -1, params);
                List<Key> keyList = new ArrayList<>();
                for (Attribute a : list)
                {
                    keyList.add(PersistenceUtils.getEntityKey(a));
                }
                controller.deleteById(keyList.toArray(new Key[keyList.size()]));
            }
            else if (attributesManager != null)
            {
                attributesManager.persist();
            }

            // Clear out the local attribute cache.
            attributesManager = null;
        }
    }

    @PrePersist
    @PreUpdate
    protected void prePersist()
    {
        Date currentDate = Calendar.getInstance().getTime();

        String user = AppPrincipal.SYSTEM_USER;
        UserContext ctx = UserContext.getContext();
        if (ctx != null && !AppPrincipal.ANONYMOUS_USER.equals(ctx.getUsername()))
        {
            user = ctx.getUsername();
        }

        // Update the dates and user details.
        if (creationDate == null)
        {
            // This would be the case for newly created resources.
            creationDate = currentDate;
            createdBy = user;
        }
        else
        {
            // This would be the case of updated resources.
            lastModificationDate = currentDate;
            lastModifiedBy = user;
        }
    }

    @PostPersist
    @PostUpdate
    protected void postPersist()
    {
        // Mark the object as immutable.
        setMutable(false);
        // Clear the dirty flag
        dirty = false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getMutableInstance() throws AppRuntimeException
    {
        final String METHOD_NAME = "getMutableInstance";

        if (isMutable())
        {
            return (T) this;
        }

        // Get hold of the constructor
        try
        {
            Constructor[] constructors = this.getClass().getDeclaredConstructors();
            Constructor ctor = null;
            for (Constructor c : constructors)
            {
                Class[] paramTypes = c.getParameterTypes();
                if (paramTypes.length == 1 && PersistentEntity.class.isAssignableFrom(paramTypes[0]))
                {
                    ctor = c;
                    break;
                }
            }

            if (ctor != null)
            {
                if (LOGGER.isLoggable(Level.FINE))
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                "Creating mutable instance based on constructor: " + ctor);
                }
                ctor.setAccessible(true);
                T instance = (T) ctor.newInstance(this);
                ctor.setAccessible(false);
                return instance;
            }
        }
        catch (Exception e)
        {
            throw new AppRuntimeException(e);
        }

        throw new AppRuntimeException(
                "Unable to locate appropriate constructor. Did you forget to override the method?");
    }

    ///
    /// Override of Equals & hashCode
    ///

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PersistentEntity))
        {
            return false;
        }

        PersistentEntity that = (PersistentEntity) o;

        if (dirty != that.dirty)
        {
            return false;
        }
        if (mutable != that.mutable)
        {
            return false;
        }
        if (baseResource != null ? !baseResource.equals(that.baseResource) : that.baseResource != null)
        {
            return false;
        }
        if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null)
        {
            return false;
        }
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (lastModificationDate != null ? !lastModificationDate.equals(that.lastModificationDate) :
            that.lastModificationDate != null)
        {
            return false;
        }
        if (lastModifiedBy != null ? !lastModifiedBy.equals(that.lastModifiedBy) : that.lastModifiedBy != null)
        {
            return false;
        }
        if (attributesManager != null ? !attributesManager.equals(that.attributesManager) :
            that.attributesManager != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (lastModificationDate != null ? lastModificationDate.hashCode() : 0);
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0);
        result = 31 * result + (mutable ? 1 : 0);
        result = 31 * result + (dirty ? 1 : 0);
        result = 31 * result + (baseResource != null ? baseResource.hashCode() : 0);
        result = 31 * result + (attributesManager != null ? attributesManager.hashCode() : 0);
        return result;
    }
}
