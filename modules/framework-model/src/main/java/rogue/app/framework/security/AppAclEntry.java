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

package rogue.app.framework.security;

import rogue.app.framework.model.PersistentObject;

import java.security.acl.AclEntry;
import java.security.acl.Permission;
import java.util.List;

/**
 * Represents an entry within the application's access control list. The <code>AppAclEntry</code> does not define any
 * extra methods apart from those present in the parent interface. It is purely to help with the abstraction model
 * defined within the framework.
 */
public interface AppAclEntry extends PersistentObject<AppAclEntry>, AclEntry
{
    /**
     * Get the access control list with which this entry is associated.
     *
     * @return the access control list with which this entry is associated.
     */
    public AppAcl getAcl();

    /**
     * Set the access control list with which this entry is associated.
     *
     * @param appAcl the access control list with which this entry is associated.
     */
    public void setAcl(AppAcl appAcl);

    /**
     * Get the internal numerical representation of the permissions associated with this entry.
     *
     * @return the internal numerical representation of the permissions associated with this entry.
     */
    public long getPermissionsCode();

    /**
     * Return a list of permission objects represented by this entry.
     *
     * @return a list of permission objects represented by this entry.
     */
    public List<? extends Permission> getPermissions();
}
