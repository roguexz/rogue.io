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

import rogue.app.framework.view.urisupport.URIAction;

import javax.faces.application.NavigationCase;
import java.util.List;
import java.util.Map;

/**
 * A navigation case that represents a URL resource within the application.
 */
public class URINavigationCase extends NavigationCase
{
    private URIAction uriAction;

    public URINavigationCase(URIAction uriAction, String fromViewId, String fromAction, String fromOutcome,
                             String condition, String toViewId, Map<String, List<String>> parameters, boolean redirect,
                             boolean includeViewParams)
    {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, parameters, redirect, includeViewParams);
        this.uriAction = uriAction;
    }

    public URINavigationCase(URIAction uriAction, String fromViewId, String fromAction, String fromOutcome,
                             String condition,
                             String toViewId, String toFlowDocumentId,
                             Map<String, List<String>> parameters, boolean redirect,
                             boolean includeViewParams)
    {
        super(fromViewId, fromAction, fromOutcome, condition, toViewId, toFlowDocumentId, parameters, redirect,
              includeViewParams);
        this.uriAction = uriAction;
    }

    /**
     * Get the {@link URIAction} associated with this navigation case.
     *
     * @return the URIAction associated with this navigation case.
     */
    public URIAction getURIAction()
    {
        return uriAction;
    }
}
