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

package rogue.app.framework.view;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration of recognized action types, that are going to be performed on a target view.
 */
public enum ActionType
{
    /**
     * Create
     */
    CREATE("create"),
    /**
     * Update
     */
    UPDATE("update"),
    /**
     * View
     */
    VIEW("view"),
    /**
     * Delete
     */
    DELETE("delete");

    /**
     * List of all actions defined by this enum.
     */
    public static List<String> ACTIONS = Arrays.asList("create", "update", "view", "delete");

    private String action;

    private ActionType(String action)
    {
        this.action = action;
    }

    @Override
    public String toString()
    {
        return action;
    }

    /**
     * Look up the type based on the name.
     *
     * @param name the name of the enumeration.
     * @return the type represented by the given name.
     */
    public static ActionType lookup(String name)
    {
        switch (name)
        {
            case "create":
                return CREATE;
            case "update":
                return UPDATE;
            case "view":
                return VIEW;
            case "delete":
                return DELETE;
        }
        return valueOf(name);
    }
}
