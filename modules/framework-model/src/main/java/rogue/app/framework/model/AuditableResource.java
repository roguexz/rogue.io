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

package rogue.app.framework.model;

import java.util.Date;

/**
 * An <code>AppObject</code> marked as auditable will have all changes made to it saved to a separate table for future
 * auditing purposes.
 */
public interface AuditableResource
{
    /**
     * Get the date on which this resource was created.
     */
    public Date getCreationDate();

    /**
     * Set the date on which the resource was created.
     *
     * @param creationDate the date on which the object was created.
     */
    public void setCreationDate(Date creationDate);

    /**
     * Get the user who created this resource.
     */
    public String getCreatedBy();

    /**
     * Set the user who created this resource.
     *
     * @param user the user who created this resource.
     */
    public void setCreatedBy(String user);

    /**
     * Get the date on which this resource was last modified.
     */
    public Date getLastModificationDate();

    /**
     * Set the date on which this resource was last modified.
     *
     * @param lastModificationDate the date on which this resource was last modified.
     */
    public void setLastModificationDate(Date lastModificationDate);

    /**
     * Get the user who modified this resource last.
     */
    public String getLastModifiedBy();

    /**
     * Set the user user who modified this resource last.
     *
     * @param user the user who last modified this resource.
     */
    public void setLastModifiedBy(String user);
}
