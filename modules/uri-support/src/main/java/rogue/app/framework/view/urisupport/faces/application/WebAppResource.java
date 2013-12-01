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

package rogue.app.framework.view.urisupport.faces.application;

import javax.faces.application.Resource;
import javax.faces.application.ResourceWrapper;
import javax.faces.context.FacesContext;

/**
 * A <code>WebAppResource</code> represents a file that is local to the web application. This class is primarily used
 * by the resource handler for rendering web assets (CSS/IMG/JavaScript) by returning a direct access URL instead of
 * returning a JSF resource URL.
 * <p/>
 * Consider the following code layout:
 * <pre>
 *     webapp/
 *       |
 *       |-resources/
 *             |
 *             |-bootstrap/
 *                 |
 *                 |-css/
 *                    |
 *                    |- bootstrap.min.css
 * </pre>
 * <p/>
 * Using standard JSF resource handling, the above css file will get rendered as the following URL:
 * <p/>
 * <code>/javax.faces.resource/css/bootstrap.min.css.xhtml?ln=bootstrap</code>
 * <p/>
 * This kind of a URL causes issues when the CSS file has <code>url(..)</code> references in it as they do not get
 * resolved properly.
 * <p/>
 * The <code>WebAppResource</code> object tries to resolve the target URL to a webapp local location and returns that
 * when {@link #getRequestPath()} is invoked. So the same above code will end up in a url that looks like the following:
 * <p/>
 * <code>/resources/bootstrap/css/bootstrap.min.css</code>
 */
public class WebAppResource extends ResourceWrapper
{
    private Resource wrapped;
    private String path;

    public WebAppResource(String path, Resource wrapped)
    {
        this.wrapped = wrapped;
        FacesContext context = FacesContext.getCurrentInstance();
        this.path = context.getApplication().getViewHandler().getResourceURL(context, path);
    }

    @Override
    public String getRequestPath()
    {
        return path;
    }

    @Override
    public Resource getWrapped()
    {
        return wrapped;
    }
}
