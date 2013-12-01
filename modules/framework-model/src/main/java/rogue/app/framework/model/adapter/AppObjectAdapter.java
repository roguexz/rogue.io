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

package rogue.app.framework.model.adapter;

import com.google.common.base.Preconditions;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.AuditableResource;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.patterns.Delegation;

import java.util.Date;
import java.util.List;

/**
 * An <code>AppObject</code> adapter that wraps over a delegate object. This is useful for classes that wish to extend
 * an existing instance with additional functionality or override a specific method alone. This adapter provides
 * convenience methods for handling the {@link Attributes} interface. If the delegated object does not support the
 * <code>Attributes</code> interface, then the corresponding methods will throw an
 * <code>UnsupportedOperationException</code>.
 */
public class AppObjectAdapter<T extends AppObject<T, K>, K> implements AppObject<T, K>, Delegation<T>
{
    private transient AppObjectAdapter<T, K> baseObject;
    private T appObjectDelegate;

    /**
     * Creates an instance of <code>AppObjectAdapter</code> that delegates all its operations to the given app object
     * instance.
     *
     * @param appObjectDelegate the delegate object instance.
     * @throws NullPointerException if the argument is null.
     */
    public AppObjectAdapter(T appObjectDelegate)
    {
        this.appObjectDelegate = Preconditions.checkNotNull(appObjectDelegate, "Argument cannot be null");
    }

    protected AppObjectAdapter(AppObjectAdapter<T, K> baseObject)
    {
        this.baseObject = Preconditions.checkNotNull(baseObject, "Argument cannot be null");
        this.appObjectDelegate = baseObject.appObjectDelegate.getMutableInstance();
    }

    /**
     * Get access to the delegate object.
     *
     * @return the delegate object.
     */
    @Override
    public T getDelegate()
    {
        return appObjectDelegate;
    }

    ///
    /// Delegated methods
    ///

    /**
     * {@inheritDoc}
     */
    @Override
    public K getId()
    {
        return appObjectDelegate.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdAsString()
    {
        return appObjectDelegate.getIdAsString();
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public Date getCreationDate()
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            return ((AuditableResource) appObjectDelegate).getCreationDate();
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public void setCreationDate(Date creationDate)
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            ((AuditableResource) appObjectDelegate).setCreationDate(creationDate);
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public String getCreatedBy()
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            return ((AuditableResource) appObjectDelegate).getCreatedBy();
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public void setCreatedBy(String user)
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            ((AuditableResource) appObjectDelegate).setCreatedBy(user);
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public Date getLastModificationDate()
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            return ((AuditableResource) appObjectDelegate).getLastModificationDate();
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public void setLastModificationDate(Date lastModificationDate)
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            ((AuditableResource) appObjectDelegate).setLastModificationDate(lastModificationDate);
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public String getLastModifiedBy()
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            return ((AuditableResource) appObjectDelegate).getLastModifiedBy();
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * Convenience method in case the base object implements the {@link AuditableResource} interface.
     */
    public void setLastModifiedBy(String user)
    {
        if (appObjectDelegate instanceof AuditableResource)
        {
            ((AuditableResource) appObjectDelegate).setLastModifiedBy(user);
        }
        else
        {
            throw new UnsupportedOperationException("Application object does not implement the Auditable interface.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMutable()
    {
        return appObjectDelegate.isMutable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getMutableInstance() throws AppRuntimeException
    {
        return (T) (isMutable() ? this : new AppObjectAdapter<>(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getBaseResource()
    {
        return (T) baseObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty()
    {
        return appObjectDelegate.isDirty();
    }

    ///
    /// Convenience delegate methods if the current object implements the Attributes method.
    ///

    /**
     * Convenience method for <code>AppObject</code>s that implement the <code>Attributes</code> interface.
     *
     * @throws UnsupportedOperationException if the current object does not implement the <code>Attributes</code>
     *                                       interface.
     */
    public List<String> getAttributeNames()
    {
        if (appObjectDelegate instanceof Attributes)
        {
            return ((Attributes) appObjectDelegate).getAttributeNames();
        }
        else
        {
            throw new UnsupportedOperationException("This object does not support Attributes");
        }
    }

    /**
     * Convenience method for <code>AppObject</code>s that implement the <code>Attributes</code> interface.
     *
     * @throws UnsupportedOperationException if the current object does not implement the <code>Attributes</code>
     *                                       interface.
     */
    public boolean isAttributeDefined(String name)
    {
        if (appObjectDelegate instanceof Attributes)
        {
            return ((Attributes) appObjectDelegate).isAttributeDefined(name);
        }
        else
        {
            throw new UnsupportedOperationException("This object does not support Attributes");
        }
    }

    /**
     * Convenience method for <code>AppObject</code>s that implement the <code>Attributes</code> interface.
     *
     * @throws UnsupportedOperationException if the current object does not implement the <code>Attributes</code>
     *                                       interface.
     */
    public String getAttribute(String name)
    {
        if (appObjectDelegate instanceof Attributes)
        {
            return ((Attributes) appObjectDelegate).getAttribute(name);
        }
        else
        {
            throw new UnsupportedOperationException("This object does not support Attributes");
        }
    }

    /**
     * Convenience method for <code>AppObject</code>s that implement the <code>Attributes</code> interface.
     *
     * @throws UnsupportedOperationException if the current object does not implement the <code>Attributes</code>
     *                                       interface.
     */
    public void setAttribute(String name, String value)
    {
        if (appObjectDelegate instanceof Attributes)
        {
            ((Attributes) appObjectDelegate).setAttribute(name, value);
        }
        else
        {
            throw new UnsupportedOperationException("This object does not support Attributes");
        }

    }

    /**
     * Convenience method for <code>AppObject</code>s that implement the <code>Attributes</code> interface.
     *
     * @throws UnsupportedOperationException if the current object does not implement the <code>Attributes</code>
     *                                       interface.
     */
    public void removeAttribute(String name)
    {
        if (appObjectDelegate instanceof Attributes)
        {
            ((Attributes) appObjectDelegate).removeAttribute(name);
        }
        else
        {
            throw new UnsupportedOperationException("This object does not support Attributes");
        }
    }

}
