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

package rogue.app.store.catalog.model;

/**
 * Enumeration of image types (sizes) that are used within the system. *
 */
public enum ImageType
{
    /**
     * Constant for identifying a regular image.
     */
    GENERAL,

    /**
     * Constant for identifying the default image.
     */
    DEFAULT,

    /**
     * Constant for identifying the thumbnail image.
     */
    THUMBNAIL,
}
