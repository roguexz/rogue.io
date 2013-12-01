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

package rogue.app.framework.persistence.security;

import com.google.appengine.api.datastore.Key;
import com.google.common.base.Preconditions;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.persistence.PersistentEntity;
import rogue.app.framework.security.AppAcl;
import rogue.app.framework.security.AppAclEntry;
import rogue.app.framework.security.AppPermission;
import rogue.app.framework.security.AppPrincipal;

import javax.enterprise.context.SessionScoped;
import javax.persistence.*;
import java.security.Principal;
import java.security.acl.Permission;
import java.util.*;

/**
 * Persistent implementation of the <code>AppAclEntry</code> interface.
 */
@Entity
@NamedQueries({
                      @NamedQuery(name = "AppAclEntryEntity.findAllForAcl",
                                  query = "SELECT e FROM AppAclEntryEntity e WHERE e.aclKey = :aclKey"),
                      @NamedQuery(name = "AppAclEntryEntity.findAllForAclPrincipal",
                                  query = "SELECT e FROM AppAclEntryEntity e WHERE e.aclKey = :aclKey AND e.principalKey = :principalKey")
              })
@rogue.app.framework.persistence.Cacheable(preferredScope = SessionScoped.class)
public class AppAclEntryEntity extends PersistentEntity<AppAclEntry> implements AppAclEntry
{
    @Column(nullable = false)
    private Key aclKey;
    @Column(nullable = false)
    private Key principalKey;
    @Column(nullable = false)
    private String principalRefClassName;
    private long permissionsCode;
    private boolean negative;

    @Transient
    private transient Enumeration<Permission> permissionEnumeration;
    @Transient
    private transient List<AppPermission> permissionList;

    public AppAclEntryEntity()
    {
        super();
    }

    public AppAclEntryEntity(AppAclEntryEntity entity)
    {
        super(entity);
    }

    public Key getAclKey()
    {
        return aclKey;
    }

    public void setAclKey(Key aclKey)
    {
        if (isMutable() && !Objects.equals(this.aclKey, aclKey))
        {
            this.aclKey = aclKey;
            markDirty();
        }
    }

    public Key getPrincipalKey()
    {
        return principalKey;
    }

    public void setPrincipalKey(Key principalKey)
    {
        if (isMutable() && !Objects.equals(this.principalKey, principalKey))
        {
            this.principalKey = principalKey;
            markDirty();
        }
    }

    public String getPrincipalRefClassName()
    {
        return principalRefClassName;
    }

    public void setPrincipalRefClassName(String principalRefClassName)
    {
        if (this.isMutable() && !Objects.equals(this.principalRefClassName, principalRefClassName))
        {
            this.principalRefClassName = principalRefClassName;
            markDirty();
        }
    }

    @Override
    public long getPermissionsCode()
    {
        return permissionsCode;
    }

    public void setPermissionsCode(long permissions)
    {
        if (isMutable() && this.permissionsCode != permissions)
        {
            this.permissionsCode = permissions;
            markDirty();
        }
    }

    @Override
    public boolean isNegative()
    {
        return negative;
    }

    public void setNegative(boolean negative)
    {
        if (isMutable() && this.negative != negative)
        {
            this.negative = negative;
            markDirty();
        }
    }

    ///
    /// Non-entity implementations
    ///


    @Override
    public AppAcl getAcl()
    {
        return PersistenceUtils.getAppObject(AppAcl.class, getAclKey());
    }

    @Override
    public void setAcl(AppAcl appAcl)
    {
        setAclKey(PersistenceUtils.getEntityKey(appAcl));
    }

    @Override
    public boolean setPrincipal(Principal principal)
    {
        if (principal != null && !(principal instanceof AppPrincipal))
        {
            throw new IllegalArgumentException("Principal object should be an instance of: " + AppPrincipal.class);
        }

        AppPrincipal appPrincipal = (AppPrincipal) principal;
        setPrincipalKey(PersistenceUtils.getEntityKey(appPrincipal));
        setPrincipalRefClassName(appPrincipal == null ? null : appPrincipal.getClass().getName());

        return true;
    }

    @Override
    public Principal getPrincipal()
    {
        return (AppPrincipal) PersistenceUtils.getAppObject(getPrincipalRefClassName(), getPrincipalKey());
    }

    @Override
    public void setNegativePermissions()
    {
        setNegative(true);
    }

    @Override
    public boolean addPermission(Permission permission)
    {
        Preconditions.checkState(isMutable(), "Persistent object is not mutable.");
        Preconditions.checkNotNull(permission, "Permission object cannot be null.");
        if (!(permission instanceof AppPermission))
        {
            throw new IllegalArgumentException("Permission object is not an instance of " + AppPermission.class
                    .getName() + " [ Received: " + permission.getClass().getName() + " ]");
        }

        AppPermission appPerm = (AppPermission) permission;
        long permValue = getPermissionsCode();
        if ((permValue & appPerm.getCode()) != appPerm.getCode())
        {
            permValue = permValue | appPerm.getCode();
            setPermissionsCode(permValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean removePermission(Permission permission)
    {
        Preconditions.checkState(isMutable(), "Persistent object is not mutable.");
        Preconditions.checkNotNull(permission, "Permission object cannot be null.");
        if (!(permission instanceof AppPermission))
        {
            throw new IllegalArgumentException("Permission object is not an instance of " + AppPermission.class
                    .getName() + " [ Received: " + permission.getClass().getName() + " ]");
        }

        AppPermission appPerm = (AppPermission) permission;
        long permValue = getPermissionsCode();
        if ((permValue & appPerm.getCode()) != appPerm.getCode())
        {
            permValue = permValue ^ appPerm.getCode();
            setPermissionsCode(permValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkPermission(Permission permission)
    {
        Preconditions.checkState(isMutable(), "Persistent object is not mutable.");
        Preconditions.checkNotNull(permission, "Permission object cannot be null.");
        Preconditions.checkArgument(permission instanceof AppPermission,
                                    "Permission object is not an instance of %s [Received %s ]", AppPermission.class,
                                    permission.getClass().getName());

        AppPermission appPerm = (AppPermission) permission;

        return appPerm.getCode() == (appPerm.getCode() & getPermissionsCode());
    }

    @Override
    public Enumeration<Permission> permissions()
    {
        if (permissionEnumeration == null)
        {
            List list = getPermissions();
            permissionEnumeration = Collections.enumeration((List<Permission>) list);
        }
        return permissionEnumeration;
    }

    @Override
    public List<? extends Permission> getPermissions()
    {
        if (permissionList == null)
        {
            permissionList = new ArrayList<AppPermission>();
            long pValue = getPermissionsCode();
            for (AppPermission p : AppPermission.PERMISSIONS)
            {
                if ((pValue & p.getCode()) == p.getCode())
                {
                    permissionList.add(p);
                }
            }
        }

        return permissionList;
    }

    ///
    /// Clone method
    ///

    @Override
    public AppAclEntryEntity clone()
    {
        return new AppAclEntryEntity(this);
    }

    ///
    /// equals & hashCode
    ///

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AppAclEntryEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        AppAclEntryEntity that = (AppAclEntryEntity) o;

        if (negative != that.negative)
        {
            return false;
        }
        if (permissionsCode != that.permissionsCode)
        {
            return false;
        }
        if (aclKey != null ? !aclKey.equals(that.aclKey) : that.aclKey != null)
        {
            return false;
        }
        if (principalKey != null ? !principalKey.equals(that.principalKey) : that.principalKey != null)
        {
            return false;
        }
        if (principalRefClassName != null ? !principalRefClassName.equals(that.principalRefClassName) :
            that.principalRefClassName != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (aclKey != null ? aclKey.hashCode() : 0);
        result = 31 * result + (principalKey != null ? principalKey.hashCode() : 0);
        result = 31 * result + (principalRefClassName != null ? principalRefClassName.hashCode() : 0);
        result = 31 * result + (int) (permissionsCode ^ (permissionsCode >>> 32));
        result = 31 * result + (negative ? 1 : 0);
        return result;
    }
}
