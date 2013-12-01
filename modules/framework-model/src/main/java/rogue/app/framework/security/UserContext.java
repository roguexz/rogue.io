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

package rogue.app.framework.security;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserContext implements Serializable
{
    private static final String CLASS_NAME = UserContext.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final ThreadLocal<UserContext> threadLocal = new InheritableThreadLocal<>();

    private String username = null;
    private User userObject = null;

    /**
     * Construct a user context object - for admin sessions.
     */
    public UserContext(String username)
    {
        final String METHOD_NAME = "<ctor>";
        if (username == null)
        {
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "No user name passed. Defaulting to guest");
            username = AppPrincipal.ANONYMOUS_USER;
        }

        this.username = username;
    }

    public UserContext(User user)
    {
        final String METHOD_NAME = "<ctor>";
        this.userObject = user;
        if (user == null)
        {
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "No user object passed. Defaulting to guest");
            username = AppPrincipal.ANONYMOUS_USER;
        }
        else
        {
            username = userObject.getPrimaryEmail();
        }
    }

    public String getUsername()
    {
        return username;
    }

    public User getUser()
    {
        return userObject;
    }

    public boolean isGuest()
    {
        return AppPrincipal.ANONYMOUS_USER.equals(username);
    }

    public static void setContext(UserContext context)
    {
        final String METHOD_NAME = "setContext";
        if (context == null)
        {
            throw new IllegalArgumentException("UserContext cannot be null.");
        }

        UserContext oldCtx = threadLocal.get();
        if (oldCtx != null)
        {
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME,
                        "Replacing existing context with a new one: [Old: " + oldCtx
                                .getUsername() + " ] [New: " + context
                                .getUsername() + " ]");
        }
        threadLocal.set(context);
    }

    public static UserContext getContext()
    {
        final String METHOD_NAME = "getContext";
        UserContext ctx = threadLocal.get();
        if (ctx == null)
        {
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        "Initializing a default user context. Ensure that this is the required behavior.");
            ctx = new UserContext((User) null);
            setContext(ctx);
        }
        return ctx;
    }

    public static void clearContext()
    {
        final String METHOD_NAME = "clearContext";
        LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Cleaning up the user context");
        threadLocal.remove();
    }
}
