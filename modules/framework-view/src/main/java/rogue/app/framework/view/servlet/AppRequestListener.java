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

package rogue.app.framework.view.servlet;

import rogue.app.framework.security.UserContext;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * Request listener for performing initialization & cleanup tasks.
 */
public class AppRequestListener implements ServletRequestListener
{
    public void requestDestroyed(ServletRequestEvent servletRequestEvent)
    {
        // Clean up the user context.
        UserContext.clearContext();
    }

    @Override
    public void requestInitialized(ServletRequestEvent event)
    {
        // TODO: Revisit if we still need this listener.
    }
}
