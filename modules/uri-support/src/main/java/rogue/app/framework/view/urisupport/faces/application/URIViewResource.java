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

import javax.faces.application.ViewResource;
import java.net.URL;

/**
 * Implementation of {@link ViewResource} because JSF didn't provide one.
 */
public class URIViewResource extends ViewResource
{
    private URL url;

    /**
     * Constructs a new instance of <code>URLViewResource</code> with the specified URL.
     *
     * @param url the url pointing to the actual resource file.
     */
    public URIViewResource(URL url)
    {
        this.url = url;
    }

    @Override
    public URL getURL()
    {
        return url;
    }
}
