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

import java.security.Principal;
import java.util.Enumeration;

/**
 * Represents a security principal as understood by the application.
 */
public interface AppPrincipal<E extends AppPrincipal<E>> extends PersistentObject<E>, Principal
{
    /**
     * The username for the system user.
     */
    public static final String SYSTEM_USER = "System";

    /**
     * The username for an anonymous user.
     */
    public static final String ANONYMOUS_USER = "Guest";

    /**
     * Get an enumeration of roles that the current principal belongs to.
     *
     * @return an enumeration of roles that the current principal belongs to.
     */
    public Enumeration<? extends AppPrincipal> getRoles();
}
