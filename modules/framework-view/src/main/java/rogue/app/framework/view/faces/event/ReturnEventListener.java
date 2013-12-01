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

package rogue.app.framework.view.faces.event;

import javax.faces.event.FacesListener;
import java.util.EventListener;

/**
 * Defines the listener for receiving {@link ReturnEvent}s.
 */
public interface ReturnEventListener extends EventListener, FacesListener
{
    public void processReturn(ReturnEvent event);
}
