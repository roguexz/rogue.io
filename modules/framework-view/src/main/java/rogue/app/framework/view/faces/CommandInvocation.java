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

package rogue.app.framework.view.faces;

import rogue.app.framework.model.ResourceSelection;
import rogue.app.framework.model.ResourceSelectionHolder;

import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A <code>CommandInvocation</code> helps in encapsulating the basic command invocations within the UI.
 *
 * @param <T> the type of the object encapsulated.
 */
public class CommandInvocation<T> implements ResourceSelection<T>, Serializable
{
    private String name;
    private ResourceSelection<T> resourceSelectionHolder;

    /**
     * Construct a new instance of CommandInvocation.
     */
    public CommandInvocation()
    {
        this(null, null);
    }

    /**
     * Constructs a new CommandInvocation instance and initializes it with the specified name & resource selection
     * holder. This constructor should be used by classes that wish to share a common {@link ResourceSelectionHolder}
     * across multiple command invocation instances.
     *
     * @param name                    the name of this of this instance.
     * @param resourceSelectionHolder the resource selection holder to use.
     */
    public CommandInvocation(String name, ResourceSelection<T> resourceSelectionHolder)
    {
        this.name = name;
        if (resourceSelectionHolder == null)
        {
            resourceSelectionHolder = new ResourceSelectionHolder<>();
        }
        this.resourceSelectionHolder = resourceSelectionHolder;
    }

    /**
     * Get the optional name assigned to this command invocation instance.
     *
     * @return the optional name assigned to this command invocation instance.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the optional name assigned to this command invocation instance.
     *
     * @param name the optional name assigned to this command invocation instance.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Identifies if the command invocation should be allowed or not in the first place. This is merely an indicator
     * to the UI, the actual enforcement should be done by the model tier.
     *
     * @return true if the command invocation can be allowed, false otherwise.
     */
    public boolean isAllowed()
    {
        return true;
    }

    /**
     * The actionListener. The default implementation does nothing.
     *
     * @param event the framework event.
     */
    public void invokeActionListener(ActionEvent event)
    {
        // Do nothing.
    }

    /**
     * The action. The default implementation return null.
     *
     * @return navigation outcome.
     */
    public String invokeAction()
    {
        return null;
    }

    /**
     * Identifies if the command invocation should be marked as immediate or not.
     *
     * @return true if the action should be immediate. By default this method returns false.
     */
    public boolean isImmediate()
    {
        return false;
    }

    /**
     * Identifies if the current invocation is being performed on multiple targets or not.
     *
     * @return true if more than one value needs to be operated on, false otherwise.
     */
    public boolean isBulkOperation()
    {
        T[] resources = getSelectedResources();
        return resources != null && resources.length != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getSelectedResource()
    {
        return resourceSelectionHolder.getSelectedResource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedResource(T value)
    {
        resourceSelectionHolder.setSelectedResource(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T[] getSelectedResources()
    {
        return resourceSelectionHolder.getSelectedResources();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedResources(T[] values)
    {
        resourceSelectionHolder.setSelectedResources(values);
    }

    /**
     * Convenience method to get hold of the values (single or multiple) as a list.
     *
     * @return a list of encapsulated values.
     */
    public List<T> getValuesAsList()
    {

        List<T> listOfValues = new ArrayList<>();
        if (isBulkOperation())
        {
            Collections.addAll(listOfValues, getSelectedResources());
        }
        else
        {
            if (getSelectedResource() != null)
            {

                listOfValues.add(getSelectedResource());
            }
        }
        return listOfValues;
    }

}
