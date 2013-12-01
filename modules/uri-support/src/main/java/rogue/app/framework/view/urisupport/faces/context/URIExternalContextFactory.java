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

package rogue.app.framework.view.urisupport.faces.context;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextFactory;

/**
 * ExternalContextFactory implementation for supporting pretty URIs for the application.
 */
public class URIExternalContextFactory extends ExternalContextFactory
{
    private ExternalContextFactory wrapped;

    public URIExternalContextFactory(ExternalContextFactory wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ExternalContextFactory getWrapped()
    {
        return wrapped;
    }

    @Override
    public ExternalContext getExternalContext(Object context, Object request, Object response) throws FacesException
    {
        ExternalContext extCtx = getWrapped().getExternalContext(context, request, response);
        return new URIExternalContext(extCtx);
    }
}
