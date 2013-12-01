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

package rogue.app.framework;

/**
 * The base exception for the entire application.
 */
public class AppRuntimeException extends RuntimeException
{
    /**
     * Create a new instance of <code>AppRuntimeException</code>.
     */
    public AppRuntimeException()
    {
    }

    /**
     * Create a new instance of <code>AppRuntimeException</code> with the specified message.
     *
     * @param message the detail message.
     */
    public AppRuntimeException(String message)
    {
        super(message);
    }

    /**
     * Create a new instance of <code>AppRuntimeException</code> with the specified message and underlying cause.
     *
     * @param message the detail message.
     * @param cause   the underlying cause
     */
    public AppRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Create a new instance of <code>AppRuntimeException</code> with the specified underlying cause.
     *
     * @param cause the underlying cause
     */
    public AppRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
