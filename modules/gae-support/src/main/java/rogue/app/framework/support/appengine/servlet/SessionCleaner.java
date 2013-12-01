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

package rogue.app.framework.support.appengine.servlet;

import com.google.appengine.api.datastore.Key;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.persistence.JpaCallback;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.support.appengine.AhSession;
import rogue.app.framework.view.servlet.AppQueueServlet;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Framework class that handles cleaning up of old sessions. The GAE/J platform did not provide a session cleanup
 * mechanism as of version 1.6. This servlet is typically triggered by a cron job.
 */
public class SessionCleaner extends AppQueueServlet
{
    private static final String CLASS_NAME = SessionCleaner.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        final String METHOD_NAME = "processRequest";
        // get the session ttl in milliseconds.
        long ttl = request.getSession().getMaxInactiveInterval() * 1000;
        // double up that value. We will delete any record that goes beyond that value.
        ttl += ttl;
        try
        {
            List<Key> keys = getStaleSessionKeys(ttl);
            deleteSessionEntries(keys);
        }
        catch (Exception e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to clean up stale sessions.", e);
        }
    }

    private List<Key> getStaleSessionKeys(long maxTimeToLive)
    {
        return PersistenceUtils.executeJpaCallback(FIND_STALE_SESSION_ENTRIES, false, maxTimeToLive);
    }

    private void deleteSessionEntries(List<Key> keys)
    {
        final String METHOD_NAME = "deleteSessionEntries";
        if (keys != null && !keys.isEmpty())
        {
            for (Key k : keys)
            {
                PersistenceUtils.executeJpaCallback(DELETE_STALE_SESSION_ENTRIES, true, k);
            }
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        String.format("Deleted %d stale session entries.", keys.size()));
        }
    }

    private static final JpaCallback<List<Key>> FIND_STALE_SESSION_ENTRIES = new JpaCallback<List<Key>>()
    {
        @Override
        public List<Key> execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            final String METHOD_NAME = "FIND_STALE_SESSION_ENTRIES_CALLBACK";
            List<Key> keys = Collections.emptyList();

            if (params == null || params.length == 0)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Input parameters are null! Aborting operation.");
                return keys;
            }

            Long ttl = (Long) params[0];
            if (ttl == null)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Time to live parameter is null! Aborting operation.");
                return keys;
            }

            Query query = entityManager.createNamedQuery("Framework.ListStaleSessions");
            query.setParameter("expiryTime", ttl);

            List result = query.getResultList();
            if (!result.isEmpty())
            {
                keys = new ArrayList<>();
                for (Object session : result)
                {
                    if (session == null)
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                    "Session object is null! This should never happen. Ignoring this entry.");
                        continue;
                    }

                    if (session instanceof AhSession)
                    {
                        keys.add(((AhSession) session).getId());
                    }
                    else
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                    "Object instance is not of type AhSession! Ignoring object. Actual type: " + session
                                            .getClass());
                    }
                }
            }

            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        String.format("Found %d sessions that need to be cleaned up.", keys.size()));

            return keys;
        }
    };

    private static final JpaCallback<Object> DELETE_STALE_SESSION_ENTRIES = new JpaCallback<Object>()
    {
        @Override
        public Object execute(EntityManager entityManager, Object... params) throws AppRuntimeException
        {
            final String METHOD_NAME = "DELETE_STALE_SESSION_ENTRIES_CALLBACK";
            if (params == null || params.length == 0)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Input parameters are null! Aborting operation.");
                return null;
            }

            Key key = (Key) params[0];
            if (key == null)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Session Key is null or empty. Aborting operation.");
                return null;
            }

            Object entity = entityManager.getReference(AhSession.class, key);
            entityManager.remove(entity);
            return null;
        }
    };
}
