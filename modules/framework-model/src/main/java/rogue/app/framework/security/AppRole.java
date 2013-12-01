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

import java.security.acl.Group;

/**
 * Represents an application specific role.
 */
public interface AppRole extends AppPrincipal<AppRole>, Group
{
    /**
     * Role name that represents all anonymous users. This role complements the {@link AppPrincipal#ANONYMOUS_USER}
     * entry. It is present so as to allow for those systems that wish to configure all security based solely on roles.
     */
    public static final String ANONYMOUS_USERS = "Anonymous users";

    /**
     * Role name that represents all authenticated users.
     */
    public static final String AUTHENTICATED_USERS = "Authenticated Users";

    /**
     * Set the name of this role.
     *
     * @param name the name of this role.
     */
    public void setName(String name);
}
