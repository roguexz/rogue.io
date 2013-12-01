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

package rogue.app.framework.model;

import javax.inject.Named;
import java.io.Serializable;

/**
 * An abstract implementation of {@link AppObjectControllerCollection} that derives the collection name from the
 * {@link Named} annotation set on the child classes.
 */
public abstract class NamedAppObjectControllerCollection implements AppObjectControllerCollection, Serializable
{
    private String collectionName;
    private boolean collectionNameLookupComplete;

    @Override
    public String getCollectionName()
    {
        if (collectionName == null && !collectionNameLookupComplete)
        {
            Named named = getClass().getAnnotation(Named.class);
            if (named != null)
            {
                collectionName = named.value();
                if (collectionName == null || collectionName.isEmpty())
                {
                    collectionName = getClass().getSimpleName();
                    collectionName = collectionName.substring(0, 1).toLowerCase() + collectionName.substring(1);
                }
            }
            collectionNameLookupComplete = true;
        }

        return collectionName;
    }
}
