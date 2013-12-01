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

/**
 * The URIResolutionContext contains information required by the individual resolvers to complete their tasks.
 */
public class URIResolutionContext
{
    private String rootContext;
    private boolean resolved;

    /**
     * Creates a new instance with the specified root context.
     *
     * @param rootContext a new instance with the specified root context.
     */
    public URIResolutionContext(String rootContext)
    {
        this.rootContext = rootContext;
    }

    /**
     * Get the root context for the current URI resolution.
     *
     * @return the root context for the current URI resolution.
     */
    public String getRootContext()
    {
        return rootContext;
    }

    /**
     * Is the current URI resolved?
     *
     * @return true if the uri has been fully resolved, false otherwise.
     */
    public boolean isResolved()
    {
        return resolved;
    }

    /**
     * Set the status of the current resolution.
     *
     * @param resolved set this true if the current URI has been fully resolved, false otherwise.
     */
    public void setResolved(boolean resolved)
    {
        this.resolved = resolved;
    }
}
