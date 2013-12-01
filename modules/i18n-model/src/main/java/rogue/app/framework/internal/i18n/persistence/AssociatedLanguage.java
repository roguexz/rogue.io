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

package rogue.app.framework.internal.i18n.persistence;

import rogue.app.framework.i18n.model.Language;
import rogue.app.framework.model.PersistentObject;

/**
 * Framework interface for handling association of languages with a geographical region.
 * <p/>
 * TODO: For future changes.
 */
public interface AssociatedLanguage<T extends PersistentObject<T>> extends PersistentObject<AssociatedLanguage<T>>
{
    public T getAppObject();

    public void setAppObject(T appObject);

    public Language getLanguage();

    public void setLanguage(Language language);
}
