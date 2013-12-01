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

package rogue.app.framework.event;

import java.lang.ref.WeakReference;
import java.util.EventObject;

/**
 * Raised when an application object has been modified.
 */
public class ChangeEvent<T> extends EventObject
{
    private WeakReference<T> oldValue;
    private WeakReference<T> newValue;

    /**
     * Constructs a new instance of <code>ChangeEvent</code>.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public ChangeEvent(Object source, T oldValue, T newValue)
    {
        super(source);
        this.oldValue = new WeakReference<>(oldValue);
        this.newValue = new WeakReference<>(newValue);
    }

    /**
     * Get the previous value of the application object.
     *
     * @return the previous value of the application object.
     */
    public T getOldValue()
    {
        return oldValue.get();
    }

    /**
     * Get the current value of the application object.
     *
     * @return the current value of the application object.
     */
    public T getNewValue()
    {
        return newValue.get();
    }
}
