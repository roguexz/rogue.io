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

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class that implements the {@link ResourceSelection} interface, providing the optional ability to store selected
 * resource at a specified scope for potential data sharing. The <code>ResourceSelectionHolder</code> supports the notion
 * of a binary mode for storing the selection information -- meaning, if {@link #isBinaryMode()} returns true, then only
 * one set of values will be stored, i.e., either the {@link #getSelectedResource() selected resource} or the
 * {@link #getSelectedResources() selected resources}. When {@link #isBinaryMode()} returns false, then both values can
 * be stored independent of each other in the same holder.
 */
public class ResourceSelectionHolder<T> implements ResourceSelection<T>, Serializable
{
    private static final String CLASS_NAME = ResourceSelectionHolder.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * The key used to store the single resource.
     */
    public static final String KEY_SINGLE_VALUE = "SingleValue";
    /**
     * The key used to store the multiple resources.
     */
    public static final String KEY_MULTIPLE_VALUE = "MultipleValues";
    /**
     * The prefix used to construct the keys within this holder.
     */
    private String prefix;
    /**
     * The key used to store the single selection value.
     */
    private String computedSingleValueKey;
    /**
     * The key used to store the multi-selection value.
     */
    private String computedMultiValueKey;
    /**
     * The scope at which stateful data should be stored.
     */
    private Class<? extends Annotation> scope;
    /**
     * Member variable to store the single value selection, if the scope is explicitly set to none.
     */
    private T singleSelectionValue;
    /**
     * Member variable to store the multiple value selection, if the scope is explicitly set to none.
     */
    private T[] multipleSelectionValues;

    /**
     * Internal variable to be further defined. Currently it identifies if at any given point both
     */
    private boolean binaryMode = true;

    /**
     * Constructs an instance of <code>ResourceSelectionHolder</code> that stores the selection data locally.
     */
    public ResourceSelectionHolder()
    {
        this(null, null);
    }

    /**
     * Constructs an instance of <code>ResourceSelectionHolder</code> that stores the selection data at the specified
     * scope using the given prefix. The prefix is provided to ensure that there are no name clashes between multiple
     * resource selection objects storing data at the same scope.
     * <pre>
     *     e.g., the following instance will use the class name as the prefix of the resource selection keys, which are
     *     used to store data in the Session scope.
     *
     *     ResourceSelection selection = new ResourceSelectionHolder(SessionScoped.class, getClass().getName());
     *
     * </pre>
     *
     * @param scope  the scope at which data should be stored
     * @param prefix the prefix to use when storing data at the specified scope
     */
    public ResourceSelectionHolder(Class<? extends Annotation> scope, String prefix)
    {
        final String METHOD_NAME = "<ctor>";

        this.prefix = prefix;
        this.scope = scope;

        if (scope != null && StringUtils.isEmpty(prefix))
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        "Empty prefix identified for non-null scope. This can lead to data collisions.");
        }

        String prefixKey = "";
        if (!StringUtils.isEmpty(prefix))
        {
            prefixKey = prefix + "_";
        }

        computedSingleValueKey = prefixKey + KEY_SINGLE_VALUE;
        computedMultiValueKey = prefixKey + KEY_MULTIPLE_VALUE;
    }

    /**
     * Identifies if this resource holder is operating in a binary selection mode or not.
     *
     * @return true if this resource holder is operating in a binary selection mode, false otherwise.
     */
    public boolean isBinaryMode()
    {
        return binaryMode;
    }

    /**
     * Set whether this resource holder should operate in a binary selection mode or not.
     *
     * @param binaryMode true if this resource holder should operate in a binary selection mode, false otherwise.
     */
    public void setBinaryMode(boolean binaryMode)
    {
        this.binaryMode = binaryMode;
    }

    /**
     * Get the prefix used for constructing the various keys in this holder.
     *
     * @return the prefix used for constructing the various keys in this holder.
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Get the scope at which the data is stored.
     *
     * @return the scope at which the data is stored.
     */
    public Class<? extends Annotation> getScope()
    {
        return scope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getSelectedResource()
    {
        return scope != null ? (T) ScopedStateHolder.getStateHolder(scope).get(computedSingleValueKey) :
               singleSelectionValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedResource(T resource)
    {
        if (scope != null)
        {
            StateHolder stateHolder = ScopedStateHolder.getStateHolder(scope);
            stateHolder.put(computedSingleValueKey, resource);
            if (binaryMode)
            {
                stateHolder.remove(computedMultiValueKey);
            }
        }
        else
        {
            singleSelectionValue = resource;
            if (binaryMode)
            {
                multipleSelectionValues = null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T[] getSelectedResources()
    {
        return scope != null ? (T[]) ScopedStateHolder.getStateHolder(scope).get(computedMultiValueKey) :
               multipleSelectionValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedResources(T[] resources)
    {
        if (scope != null)
        {
            StateHolder stateHolder = ScopedStateHolder.getStateHolder(scope);
            stateHolder.put(computedMultiValueKey, resources);
            if (binaryMode)
            {
                stateHolder.remove(computedSingleValueKey);
            }
        }
        else
        {
            multipleSelectionValues = resources;
            if (binaryMode)
            {
                singleSelectionValue = null;
            }
        }
    }
}
