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

package rogue.app.framework.view.urisupport;

import rogue.app.framework.model.AppObjectController;

/**
 * A URIResolutionProvider is consulted by the URIResolver for evaluating a given URI pattern.
 */
public interface URIResolutionProvider
{
    /**
     * Attempt to resolve the given URI to a possible URIAction. Implementations should ensure that they set the
     * {@link URIResolutionContext#setResolved(boolean) resolved attribute} to true on a successful resolution.
     * Otherwise, the returned value will be ignored.
     *
     * @param context the URI resolution context that contains information about the root context, etc.
     * @param uri     the uri that needs resolution.
     * @return the URIAction if applicable, null otherwise.
     */
    public URIAction resolveURI(URIResolutionContext context, String uri);

    /**
     * Attempt to resolve a given URIAction to the appropriate controller object. Implementations should ensure that
     * they set the {@link URIResolutionContext#setResolved(boolean) resolved attribute} to true on a successful
     * resolution. Otherwise, the returned value will be ignored.
     *
     * @param context the URI resolution context that contains information about the root context, etc.
     * @param action  the URIAction that needs to be resolved.
     * @return the resolved controller object, or null if the resolution cannot be done.
     */
    public AppObjectController<?, ?> resolveAction(URIResolutionContext context, URIAction action);

}
