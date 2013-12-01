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

package rogue.app.framework.search;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class for updating search indices.
 */
public final class SearchIndexer
{
    private static final String SEARCH_TASK_URL = "/tasks/searchIndexer";
    private static final String SEARCH_QUEUE_NAME = "SearchIndexerQueue";
    private static final String QUEUE_INDEX_ADD_KEY = "_search_add_to_index";
    private static final String QUEUE_INDEX_REMOVE_KEY = "_search_remove_from_index";

    /**
     * Private constructor.
     */
    private SearchIndexer()
    {
    }


    /**
     * Enqueue keys for indexing.
     *
     * @param clazz the application object class.
     * @param add   true if the keys should be added to the index, false otherwise
     * @param keys  the ids of the application objects.
     */
    public static void queue(Class<? extends PersistentObject> clazz, boolean add, Key... keys)
    {
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        String scopeKey = add ? QUEUE_INDEX_ADD_KEY : QUEUE_INDEX_REMOVE_KEY;

        Map<Class<? extends AppObject>, List<Key>> queueMap = stateHolder.get(scopeKey);
        if (queueMap == null)
        {
            queueMap = new HashMap<>();
            stateHolder.put(scopeKey, queueMap);
        }

        List<Key> keyList = queueMap.get(clazz);
        if (keyList == null)
        {
            keyList = new ArrayList<Key>();
            queueMap.put(clazz, keyList);
        }

        for (Key k : keys)
        {
            if (k != null && !keyList.contains(k))
            {
                keyList.add(k);
            }
        }
    }

    /**
     * Process the queued up requests for search indexing.
     */
    @SuppressWarnings("unchecked")
    public static void processQueue()
    {
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        processQueueMap((Map<Class<? extends AppObject>, List<Key>>) stateHolder.get(QUEUE_INDEX_REMOVE_KEY), true);
        processQueueMap((Map<Class<? extends AppObject>, List<Key>>) stateHolder.get(QUEUE_INDEX_ADD_KEY), false);
    }

    private static void processQueueMap(Map<Class<? extends AppObject>, List<Key>> queueMap, boolean remove)
    {
        if (queueMap == null || queueMap.isEmpty())
        {
            return;
        }

        for (Map.Entry<Class<? extends AppObject>, List<Key>> entry : queueMap.entrySet())
        {
            List<Key> keyList = entry.getValue();
            if (keyList == null || keyList.isEmpty())
            {
                return;
            }

            Queue queue = QueueFactory.getQueue(SEARCH_QUEUE_NAME);
            if (queue != null)
            {
                final int THRESHOLD = 20; // not more than 20 keys per queue request.
                int start = 0;
                while (start < keyList.size())
                {
                    StringBuilder bldr = new StringBuilder(100);
                    int counter = 0;
                    for (int i = start; i < keyList.size() && counter < THRESHOLD; start++, i++, counter++)
                    {
                        Key k = keyList.get(i);
                        if (k != null)
                        {
                            bldr.append(k.getId()).append(",");
                        }
                    }
                    queue.add(TaskOptions.Builder.withUrl(SEARCH_TASK_URL)
                                         .param("appObjClass", entry.getKey().getName())
                                         .param("keys", bldr.toString())
                                         .param("remove", Boolean.toString(remove))
                                         .method(TaskOptions.Method.POST));
                }
            }
        }
    }
}
