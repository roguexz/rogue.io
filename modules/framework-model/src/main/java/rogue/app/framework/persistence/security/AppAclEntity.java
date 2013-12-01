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
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.Cacheable;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.persistence.PersistentEntity;
import rogue.app.framework.security.AppAcl;
import rogue.app.framework.security.AppAclEntry;
import rogue.app.framework.security.AppPrincipal;
import rogue.app.framework.util.ScopedStateHolder;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.persistence.Entity;
import java.security.Principal;
import java.security.acl.AclEntry;
import java.security.acl.LastOwnerException;
import java.security.acl.NotOwnerException;
import java.security.acl.Permission;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entity implementation of AppAcl
 */
@Entity
@Cacheable(preferredScope = SessionScoped.class)
public class AppAclEntity extends PersistentEntity<AppAcl> implements AppAcl
{
    private static final String CLASS_NAME = AppAclEntity.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private List<Key> ownerKeys;
    private String name;
    // Domain object
    private Key appObjRefKey;
    private String appObjRefClassName;


    public AppAclEntity()
    {
        super();
    }

    public AppAclEntity(AppAclEntity entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return name;
    }

    public Key getAppObjRefKey()
    {
        return appObjRefKey;
    }

    public void setAppObjRefKey(Key appObjRefKey)
    {
        if (this.isMutable() && !Objects.equals(this.appObjRefKey, appObjRefKey))
        {
            this.appObjRefKey = appObjRefKey;
            markDirty();
        }
    }

    public String getAppObjRefClassName()
    {
        return appObjRefClassName;
    }

    public void setAppObjRefClassName(String appObjRefClassName)
    {
        if (this.isMutable() && !Objects.equals(this.appObjRefClassName, appObjRefClassName))
        {
            this.appObjRefClassName = appObjRefClassName;
            markDirty();
        }
    }

    ///
    /// Non-entity implementations
    ///


    @Override
    public AppObject getAppObject()
    {
        return PersistenceUtils.getAppObject(getAppObjRefClassName(), getAppObjRefKey());
    }

    @Override
    public void setAppObject(AppObject appObject)
    {
        setAppObjRefKey(PersistenceUtils.getEntityKey((PersistentObject<?>) appObject));
        setAppObjRefClassName(appObject == null ? null : appObject.getClass().getName());
    }

    @Override
    public void setName(Principal caller, String name) throws NotOwnerException
    {
        Preconditions.checkNotNull(caller, "Caller principal cannot be null.");
        Preconditions.checkArgument(caller instanceof AppPrincipal,
                                    String.format("Caller principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  caller.getClass().getName()));
        if (!isOwnerOrSystemUser(caller))
        {
            throw new AppRuntimeException(
                    String.format("Caller [%s] is not allowed to modify this access control list [%s]",
                                  caller.getName(), getName()));
        }

        if (isMutable() && !Objects.equals(this.name, name))
        {
            this.name = name;
            markDirty();
        }
    }

    @Override
    public boolean addEntry(Principal caller, AclEntry aclEntry) throws NotOwnerException
    {
        Preconditions.checkNotNull(caller, "Caller principal cannot be null.");
        Preconditions.checkArgument(caller instanceof AppPrincipal,
                                    String.format("Caller principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  caller.getClass().getName()));
        if (!isOwnerOrSystemUser(caller))
        {
            throw new AppRuntimeException(
                    String.format("Caller [%s] is not allowed to modify this access control list [%s]",
                                  caller.getName(), getName()));
        }

        List<AppAclEntry> entries = getEntriesForPrincipal(getKeyFromPrincipal(aclEntry.getPrincipal()));

        AppAclEntry entryToModify = null;
        for (AppAclEntry entry : entries)
        {
            if (entry.isNegative() == aclEntry.isNegative())
            {
                entryToModify = entry.getMutableInstance();
            }
        }

        if (entryToModify == null)
        {
            entryToModify = JpaController.getController(AppAclEntry.class).getNewInstance();
            entryToModify.setAcl(this);
            entryToModify.setPrincipal(aclEntry.getPrincipal());
            if (aclEntry.isNegative())
            {
                entryToModify.setNegativePermissions();
            }
        }

        Enumeration<Permission> perms = aclEntry.permissions();
        while (perms.hasMoreElements())
        {
            entryToModify.addPermission(perms.nextElement());
        }

        if (entryToModify.isDirty())
        {
            JpaController.getController(AppAclEntry.class).save(entryToModify);
            return true;
        }

        return false;
    }

    @Override
    public boolean removeEntry(Principal caller, AclEntry aclEntry) throws NotOwnerException
    {
        Preconditions.checkNotNull(caller, "Caller principal cannot be null.");
        Preconditions.checkArgument(caller instanceof AppPrincipal,
                                    String.format("Caller principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  caller.getClass().getName()));
        if (!isOwnerOrSystemUser(caller))
        {
            throw new AppRuntimeException(
                    String.format("Caller [%s] is not allowed to modify this access control list [%s]",
                                  caller.getName(), getName()));
        }

        JpaController.getController(AppAclEntry.class).deleteById(((AppAclEntry) aclEntry).getId());
        return true;
    }

    @Override
    public Enumeration<Permission> getPermissions(Principal principal)
    {
        return Collections.enumeration(getPermissionsSet(principal));
    }

    @Override
    public Enumeration<AclEntry> entries()
    {
        List list = getEntries();
        List<AclEntry> aclEntries = (List<AclEntry>) list;
        return Collections.enumeration(aclEntries);
    }

    @Override
    public boolean checkPermission(Principal principal, Permission permission)
    {
        return getPermissionsSet(principal).contains(permission);
    }

    @Override
    public boolean addOwner(Principal caller, Principal principal) throws NotOwnerException
    {
        Preconditions.checkNotNull(caller, "Caller principal cannot be null.");
        Preconditions.checkNotNull(principal, "Owner principal cannot be null.");
        Preconditions.checkArgument(caller instanceof AppPrincipal,
                                    String.format("Caller principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  caller.getClass().getName()));
        Preconditions.checkArgument(principal instanceof AppPrincipal,
                                    String.format("Owner principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  principal.getClass().getName()));
        if (!isOwnerOrSystemUser(caller))
        {
            throw new AppRuntimeException(
                    String.format("Caller [%s] is not allowed to modify this access control list [%s]",
                                  caller.getName(), getName()));
        }

        if (isMutable())
        {
            if (ownerKeys == null)
            {
                ownerKeys = new ArrayList<Key>();
            }

            Key k = getKeyFromPrincipal(principal);
            if (!ownerKeys.contains(k))
            {
                ownerKeys.add(k);
                markDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteOwner(Principal caller, Principal principal) throws NotOwnerException, LastOwnerException
    {
        Preconditions.checkNotNull(caller, "Caller principal cannot be null.");
        Preconditions.checkNotNull(principal, "Owner principal cannot be null.");
        Preconditions.checkArgument(caller instanceof AppPrincipal,
                                    String.format("Caller principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  caller.getClass().getName()));
        Preconditions.checkArgument(principal instanceof AppPrincipal,
                                    String.format("Owner principal not instance of %s [received: %s ]",
                                                  AppPrincipal.class,
                                                  principal.getClass().getName()));
        if (!isOwnerOrSystemUser(caller))
        {
            throw new AppRuntimeException(
                    String.format("Caller [%s] is not allowed to modify this access control list [%s]",
                                  caller.getName(), getName()));
        }

        Key k = getKeyFromPrincipal(principal);
        if (isMutable() && ownerKeys != null && ownerKeys.contains(k))
        {
            ownerKeys.remove(k);
            markDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean isOwner(Principal principal)
    {
        return isOwnerOrSystemUser(principal);
    }

    private Key getKeyFromPrincipal(Principal principal)
    {
        return principal instanceof AppPrincipal ? ((PersistentEntity) principal).getId() : null;
    }

    /**
     * Identifies if the given principal is either a system user or one of the owners of this Acl object.
     *
     * @param principal the principal to check.
     * @return true if the principal represents either the System user or one of the owners of this Acl object.
     */
    private boolean isOwnerOrSystemUser(Principal principal)
    {
        if (principal != null && principal instanceof AppPrincipal)
        {
            if (AppPrincipal.SYSTEM_USER.equalsIgnoreCase(principal.getName()))
            {
                return true;
            }
            if (ownerKeys != null)
            {
                Key principalKey = getKeyFromPrincipal(principal);
                for (Key k : ownerKeys)
                {
                    if (k.equals(principalKey))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<AppAclEntry> getEntries()
    {
        Map<String, Object> queryParams = new HashMap<>(1);
        queryParams.put("aclKey", getId());
        return JpaController.getController(AppAclEntry.class)
                            .executeNamedQueryListResult("AppAclEntryEntity.findAllForAcl", 0, -1, queryParams);
    }

    private List<AppAclEntry> getEntriesForPrincipal(Key key)
    {
        final String METHOD_NAME = "getEntriesForPrincipal";

        if (key != null)
        {
            Map<String, Object> queryParams = new HashMap<>(2);
            queryParams.put("aclKey", getId());
            queryParams.put("principalKey", key);
            List<AppAclEntry> entries = JpaController.getController(AppAclEntry.class)
                                                     .executeNamedQueryListResult(
                                                             "AppAclEntryEntity.findAllForAclPrincipal", 0, -1,
                                                             queryParams);
            if (entries.size() > 2)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, String.format(
                        "There are more than 2 AclEntries for user principal %s, for the Acl (%s) on the application object %s",
                        key, getName(), getAppObjRefKey()));
            }

            return entries;
        }

        return Collections.emptyList();
    }

    private Set<Permission> getPermissionsSet(Principal principal)
    {
        final String CACHE_KEY =
                new StringBuilder(CLASS_NAME).append("_PermissionsSet_").append(principal).append(appObjRefKey)
                                             .toString();

        Set<Permission> permissionSet = ScopedStateHolder.getStateHolder(RequestScoped.class).get(CACHE_KEY);
        if (principal != null && permissionSet == null)
        {
            AppPrincipal appPrincipal = (AppPrincipal) principal;

            Set<Permission> groupPositiveSet = new HashSet<Permission>();
            Set<Permission> groupNegativeSet = new HashSet<Permission>();

            // Get the group permissions if present.
            Enumeration<? extends AppPrincipal> roles = appPrincipal.getRoles();
            while (roles.hasMoreElements())
            {
                AppPrincipal ap = roles.nextElement();
                populatePermissionsSet(ap, groupPositiveSet, groupNegativeSet);
            }

            Set<Permission> principalPositiveSet = new HashSet<Permission>();
            Set<Permission> principalNegativeSet = new HashSet<Permission>();
            populatePermissionsSet(appPrincipal, principalPositiveSet, principalNegativeSet);

            for (Permission p : principalPositiveSet)
            {
                groupNegativeSet.remove(p);
            }

            for (Permission p : principalNegativeSet)
            {
                groupPositiveSet.remove(p);
            }

            principalPositiveSet.addAll(groupPositiveSet);
            principalNegativeSet.addAll(groupNegativeSet);

            for (Permission p : principalNegativeSet)
            {
                principalPositiveSet.remove(p);
            }

            permissionSet = principalPositiveSet;
            ScopedStateHolder.getStateHolder(RequestScoped.class).put(CACHE_KEY, permissionSet);
        }
        return permissionSet;
    }

    private void populatePermissionsSet(AppPrincipal<?> principal, Set<Permission> positiveSet,
                                        Set<Permission> negativeSet)
    {
        List<AppAclEntry> entries = getEntriesForPrincipal(principal.getId());
        for (AppAclEntry entry : entries)
        {
            if (!entry.isNegative())
            {
                positiveSet.addAll(entry.getPermissions());
            }
            else
            {
                negativeSet.addAll(entry.getPermissions());
            }
        }

        //
        // canonical form
        //
        List<Permission> commons = new ArrayList<Permission>();
        for (Permission p : negativeSet)
        {
            if (positiveSet.contains(p))
            {
                commons.add(p);
            }
        }

        for (Permission p : commons)
        {
            positiveSet.remove(p);
            negativeSet.remove(p);
        }
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
        if (!(o instanceof AppAclEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        AppAclEntity that = (AppAclEntity) o;

        if (appObjRefClassName != null ? !appObjRefClassName.equals(that.appObjRefClassName) :
            that.appObjRefClassName != null)
        {
            return false;
        }
        if (appObjRefKey != null ? !appObjRefKey.equals(that.appObjRefKey) : that.appObjRefKey != null)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }
        if (ownerKeys != null ? !ownerKeys.equals(that.ownerKeys) : that.ownerKeys != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (ownerKeys != null ? ownerKeys.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (appObjRefKey != null ? appObjRefKey.hashCode() : 0);
        result = 31 * result + (appObjRefClassName != null ? appObjRefClassName.hashCode() : 0);
        return result;
    }
}
