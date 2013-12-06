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

package rogue.app.framework.internal.view.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * INTERNAL: Provides easy access to the ServletRequest & ServletResponse objects.
 */
public final class WebContext
{
    private static ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<>();
    private static ThreadLocal<HttpServletResponse> responseHolder = new ThreadLocal<>();

    public static void init(HttpServletRequest request, HttpServletResponse response)
    {
        requestHolder.set(request);
        responseHolder.set(response);
    }

    public static HttpServletRequest getRequest()
    {
        return requestHolder.get();
    }

    public static HttpServletResponse getResponse()
    {
        return responseHolder.get();
    }

    public static void clear()
    {
        requestHolder.remove();
        responseHolder.remove();
    }
}
