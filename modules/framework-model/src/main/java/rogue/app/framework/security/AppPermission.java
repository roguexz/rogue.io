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

import java.security.acl.Permission;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents an permission applicable on an application object.
 */
public final class AppPermission implements Permission
{

    /**
     * Read permission.
     */
    public static final AppPermission READ = new AppPermission(1, "Read");

    /**
     * Write permission.
     */
    public static final AppPermission WRITE = new AppPermission(2, "Write");

    /**
     * Create permission.
     */
    public static final AppPermission CREATE = new AppPermission(4, "Create");

    /**
     * Delete permission.
     */
    public static final AppPermission DELETE = new AppPermission(8, "Delete");

    /**
     * Administrative permission.
     */
    public static final AppPermission ADMINISTRATION = new AppPermission(15, "Administration");

    /**
     * Convenience method to get hold of all the permissions.
     */
    public static final AppPermission[] PERMISSIONS = new AppPermission[]{READ, WRITE, CREATE, DELETE, ADMINISTRATION};

    private long code;
    private String name;

    protected AppPermission(long code, String name)
    {
        this.code = code;
        this.name = checkNotNull(name, "Permission name cannot be null!");
    }

    public long getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AppPermission))
        {
            return false;
        }

        AppPermission that = (AppPermission) o;

        if (code != that.code)
        {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (code ^ (code >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
