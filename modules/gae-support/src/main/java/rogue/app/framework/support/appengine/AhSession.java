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

package rogue.app.framework.support.appengine;

import com.google.appengine.api.datastore.Key;

import javax.persistence.*;

/**
 * Framework class for managing the cleanup of the sessions table. Do not use directly.
 */
@Entity
@NamedQuery(name = "Framework.ListStaleSessions", query = "SELECT e FROM AhSession e WHERE e.expires > :expiryTime")
@Table(name = "_ah_SESSION")
public class AhSession
{
    @Id
    private Key id;
    @Column(name = "_expires")
    private long expires;

    public Key getId()
    {
        return id;
    }

    public long getExpires()
    {
        return expires;
    }
}
