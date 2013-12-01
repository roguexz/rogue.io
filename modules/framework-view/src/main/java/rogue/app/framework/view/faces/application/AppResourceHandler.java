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

package rogue.app.framework.view.faces.application;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;

/**
 * An implementation of the {@link ResourceHandler} that handles the case of rendering images stored as
 * {@link rogue.app.framework.model.BinaryResource} objects.
 */
public class AppResourceHandler extends ResourceHandlerWrapper
{
    private ResourceHandler wrapped;


    public AppResourceHandler(ResourceHandler resourceHandler)
    {
        this.wrapped = resourceHandler;
    }

    @Override
    public ResourceHandler getWrapped()
    {
        return wrapped;
    }

    @Override
    public boolean libraryExists(String libraryName)
    {
        return AppImageResource.LIBRARY_NAME.equals(libraryName) || super.libraryExists(libraryName);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName)
    {
        Resource resource;

        if (AppImageResource.LIBRARY_NAME.equalsIgnoreCase(libraryName))
        {
            resource = new AppImageResource(resourceName);
        }
        else
        {
            resource = super.createResource(resourceName, libraryName);
        }

        return resource;
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType)
    {
        Resource resource;

        if (AppImageResource.LIBRARY_NAME.equalsIgnoreCase(libraryName))
        {
            resource = new AppImageResource(resourceName);
            resource.setContentType(contentType);
        }
        else
        {
            resource = super.createResource(resourceName, libraryName, contentType);
        }

        return resource;
    }

}
