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

import com.google.appengine.api.datastore.Blob;
import rogue.app.framework.patterns.Association;

/**
 * Represents a binary object stored in the backend. Each binary resource is associated with a name space that helps in
 * categorizing resource objects. For e.g., all images belonging to a product could be classified as product-images.
 */
public interface BinaryResource extends PersistentObject<BinaryResource>, NamedResource, Association<PersistentObject>
{
    /**
     * Get the name space associated with this resource.
     *
     * @return the name space associated with this resource.
     */
    public String getNameSpace();

    /**
     * Set the name space associated with this resource.
     *
     * @param nameSpace the name space associated with this resource.
     */
    public void setNameSpace(String nameSpace);

    /**
     * Get the binary object used to store this image.
     *
     * @return the binary object used to store this image.
     */
    public Blob getBlob();

    /**
     * Set the binary object used for storing this image.
     *
     * @param blob the binary object used for storing this image.
     */
    public void setBlob(Blob blob);

    /**
     * Get the file extension if available.
     *
     * @return get the file extension if available.
     */
    public String getFileExtension();

    /**
     * Get the mime type if available.
     *
     * @return the mime type if available.
     */
    public String getMimeType();

    /**
     * Get the user-defined classification string associated with this resource. For e.g., a <code>Product</code> might
     * want to designate an image as either being the 'thumbnail', or 'default', or 'generic'.
     *
     * @return the user-defined classification string associated with this resource.
     */
    public String getResourceType();

    /**
     * Set the user-defined classification string associated with this resource. For e.g., a <code>Product</code> might
     * want to designate an image as either being the 'thumbnail', or 'default', or 'generic'.
     *
     * @param resourceType the user-defined classification string associated with this resource.
     */
    public void setResourceType(String resourceType);
}
