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

package rogue.app.framework.internal.persistence.security;

import com.google.appengine.api.datastore.Key;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.persistence.PersistentEntity;
import rogue.app.framework.security.AppPrincipal;
import rogue.app.framework.security.AppRole;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

@Entity
@EntityImplementationFor(AppRole.class)
public class AppRoleEntity extends PersistentEntity<AppRole> implements AppRole
{
    private String name;
    // TODO: Use the sharding concept here.
    private List<Key> users;
    private List<Key> roles;

    @Transient
    private transient List<Key> mergedList;

    public AppRoleEntity()
    {
        super();
    }

    public AppRoleEntity(AppRoleEntity entity)
    {
        super(entity);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        if (isMutable() && !Objects.equals(this.name, name))
        {
            this.name = name;
            markDirty();
        }
    }

    ///
    /// Non-entity implementation
    ///


    @Override
    public boolean addMember(Principal principal)
    {
        if (isMutable() && principal != null && principal instanceof AppPrincipal)
        {
            Key k = PersistenceUtils.getEntityKey((AppPrincipal) principal);
            if (k != null)
            {
                List<Key> keyList = null;
                if (principal instanceof AppRole)
                {
                    if (roles == null)
                    {
                        roles = new ArrayList<Key>();
                    }
                    keyList = roles;
                }
                else
                {
                    if (users == null)
                    {
                        users = new ArrayList<Key>();
                    }
                    keyList = users;
                }
                if (!keyList.contains(k))
                {
                    keyList.add(k);
                    markDirty();
                    mergedList = null;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean removeMember(Principal principal)
    {
        if (isMutable() && principal != null && principal instanceof AppPrincipal)
        {
            Key k = PersistenceUtils.getEntityKey((AppPrincipal) principal);
            if (k != null)
            {
                List<Key> keyList = principal instanceof AppRole ? roles : users;
                if (keyList != null && keyList.contains(k))
                {
                    keyList.remove(k);
                    markDirty();
                    mergedList = null;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isMember(Principal principal)
    {
        if (principal != null && principal instanceof AppPrincipal)
        {
            Key k = PersistenceUtils.getEntityKey((AppPrincipal) principal);
            if (k != null)
            {
                List<Key> keyList = principal instanceof AppRole ? roles : users;
                return keyList != null && keyList.contains(k);
            }
        }
        return false;
    }

    @Override
    public Enumeration<? extends Principal> members()
    {
        // TODO: Fix this
        return null;
    }

    @Override
    public Enumeration<? extends AppPrincipal> getRoles()
    {
        // TODO: Fix this
        return null;
    }
}
