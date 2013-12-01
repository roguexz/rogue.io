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

import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;

import java.security.acl.Acl;

/**
 * Represents an access control list as identified within the application.
 */
public interface AppAcl extends PersistentObject<AppAcl>, Acl
{
    /**
     * Get the application object associated with this Acl entry.
     *
     * @return the application object associated with this Acl entry.
     */
    public AppObject getAppObject();

    /**
     * Set the application object associated with this Acl entry.
     *
     * @param appObject the application object associated with this Acl entry.
     */
    public void setAppObject(AppObject appObject);


}
