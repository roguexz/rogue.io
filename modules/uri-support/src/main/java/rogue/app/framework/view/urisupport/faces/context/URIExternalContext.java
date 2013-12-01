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

import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;

/**
 * External context wrapper that overrides necessary methods for supporting pretty URIs for the application.
 */
public class URIExternalContext extends ExternalContextWrapper
{
    private static final String CLASS_NAME = URIExternalContext.class.getName();
    public static final String OVERRIDE_SERVLET_PATH = CLASS_NAME + "_" + "override.servlet.path";
    public static final String OVERRIDE_PATH_INFO = CLASS_NAME + "_" + "override.path.info";

    private ExternalContext wrapped;

    public URIExternalContext(ExternalContext wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ExternalContext getWrapped()
    {
        return wrapped;
    }

    @Override
    public String getRequestServletPath()
    {
        String attr = (String) getRequestMap().get(OVERRIDE_SERVLET_PATH);
        return attr != null ? attr : super.getRequestServletPath();
    }

    @Override
    public String getRequestPathInfo()
    {
        String attr = (String) getRequestMap().get(OVERRIDE_PATH_INFO);
        return attr != null ? attr : super.getRequestPathInfo();
    }
}
