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

package rogue.app.framework.view.urisupport;

import rogue.app.framework.view.ActionType;

import java.io.Serializable;

/**
 * Represents an action associated with a URI fragment.
 */
public class URIAction implements Serializable, Cloneable
{

    private String context;
    private String controllerName;
    private ActionType actionType;
    private String id;
    private URIAction parent;
    private String viewId;

    /**
     * Creates a new URIAction with the specified root context and controller name.
     *
     * @param context        the prefix context.
     * @param controllerName the controller name.
     */
    public URIAction(String context, String controllerName)
    {
        this.context = context;
        this.controllerName = controllerName;
    }

    /**
     * Creates a new URIAction with the specified controller name and associated parent.
     *
     * @param controllerName the controller name.
     * @param parent         the parent of this URIAction object.
     */
    public URIAction(String controllerName, URIAction parent)
    {
        this.controllerName = controllerName;
        this.parent = parent;
    }

    /**
     * Get the root context associated with this URI action.
     *
     * @return the root context associated with this URI action.
     */
    public String getContext()
    {
        return context == null ? parent != null ? parent.getContext() : null : context;
    }

    /**
     * Get the controller name associated with this URIAction.
     *
     * @return the controller name associated with this URIAction.
     */
    public String getControllerName()
    {
        return controllerName;
    }

    /**
     * Get the parent associated with this URIAction.
     *
     * @return the parent associated with this URIAction.
     */
    public URIAction getParent()
    {
        return parent;
    }

    public ActionType getActionType()
    {
        return actionType;
    }

    public void setActionType(ActionType actionType)
    {
        this.actionType = actionType;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Get the view ID that corresponds to this action. This method should be used in the scenario when resolving the
     * controller will not return a valid page.
     *
     * @return the view ID that corresponds to this action.
     */
    public String getViewId()
    {
        return viewId;
    }

    /**
     * Set the view ID that corresponds to this action. This method should be used in the scenario when resolving the
     * controller will not return a valid page.
     *
     * @param viewId the view ID that corresponds to this action.
     */
    public void setViewId(String viewId)
    {
        this.viewId = viewId;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(100);
        if (parent == null)
        {
            builder.append("/").append(context);
        }
        else
        {
            builder.append(parent.toString());
        }
        builder.append("/").append(controllerName);
        if (id != null)
        {
            builder.append("/").append(id);
        }
        if (actionType != ActionType.VIEW)
        {
            builder.append("/").append(actionType.toString());
        }

        return builder.toString();
    }
}
