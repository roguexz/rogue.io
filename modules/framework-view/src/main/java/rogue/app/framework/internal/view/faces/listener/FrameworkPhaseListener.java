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

package rogue.app.framework.internal.view.faces.listener;

import rogue.app.framework.search.SearchIndexer;
import rogue.app.framework.view.util.SecurityUtils;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Phase listener that is meant for handling framework related operations.
 * <ul>
 * <li>Setup the security context before RESTORE_VIEW</li>
 * <li>Process pending search indexing requests after INVOKE_APPLICATION</li>
 * </ul>
 * TODO: Do I still need this?
 */
public class FrameworkPhaseListener implements PhaseListener
{
    private static final String CLASS_NAME = FrameworkPhaseListener.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public void afterPhase(PhaseEvent event)
    {
        if (PhaseId.INVOKE_APPLICATION == event.getPhaseId())
        {
            processSearchQueue(event);
        }
    }

    @Override
    public void beforePhase(PhaseEvent event)
    {
        if (PhaseId.RESTORE_VIEW == event.getPhaseId())
        {
            // Setup the security context.
            setupUserContext(event);
        }
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.ANY_PHASE;
    }

    /**
     * Set up the user context. This operation should be executed before RESTORE_VIEW kicks in.
     *
     * @param event the phase event object.
     */
    private void setupUserContext(PhaseEvent event)
    {
        // To be executed ONLY during restore view.
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW)
        {
            try
            {
                SecurityUtils.initUserContext(
                        (HttpServletRequest) event.getFacesContext().getExternalContext().getRequest());
            }
            catch (IOException e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, "beforePhase", "Failed to setup security context", e);
            }
        }
    }

    /**
     * Process any pending search indexing requests. Typically, all new / modified items that need to be indexed will
     * be added to the queue during the INVOKE_APPLICATION phase. It is safe to execute this call AFTER the completion
     * of the INVOKE_APPLICATION phase.
     *
     * @param event the phase event object.
     */
    private void processSearchQueue(PhaseEvent event)
    {
        // To be executed ONLY after INVOKE_APPLICATION
        if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION)
        {
            // Clear out pending requests in the SearchIndexer queue.
            SearchIndexer.processQueue();
        }
    }
}
