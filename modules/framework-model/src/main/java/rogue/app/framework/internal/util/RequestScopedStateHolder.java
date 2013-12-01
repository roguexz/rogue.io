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

package rogue.app.framework.internal.util;

import javax.enterprise.context.RequestScoped;

/**
 * Implementation of {@link rogue.app.framework.util.StateHolder} that is scoped to {@link RequestScoped}
 */
@RequestScoped
@ScopedStateHolderQualifier(RequestScoped.class)
public class RequestScopedStateHolder extends AbstractScopedStateHolder
{
    public RequestScopedStateHolder()
    {
    }
}
