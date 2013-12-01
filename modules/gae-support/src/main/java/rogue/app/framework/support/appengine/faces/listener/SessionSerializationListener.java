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

package rogue.app.framework.support.appengine.faces.listener;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Nifty hack: http://java.zacheusz.eu/google-app-engine-http-session-vs-jsf-en/394/
 * <p/>
 * Check out StackOverflow for my explanation:
 * http://stackoverflow.com/questions/19259457/session-lost-in-google-app-engine-using-jsf
 */
public class SessionSerializationListener implements PhaseListener
{
    final String CLASS_NAME = SessionSerializationListener.class.getName();
    Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Override
    public void afterPhase(PhaseEvent event)
    {
        // Set an attribute on to the session just so that we can force the session to get serialized
        // We target the invoke application phase as well since the redirects get issued at that stage.
        if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION || event.getPhaseId() == PhaseId.RENDER_RESPONSE)
        {
            final String METHOD_NAME = "afterPhase";
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Adding a random attribute to the session map.");
            Map<String, Object> sessionMap = event.getFacesContext().getExternalContext().getSessionMap();
            if (sessionMap != null)
            {
                sessionMap.put("__timestamp__", System.currentTimeMillis());
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent event)
    {
        // Do nothing.
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }
}
