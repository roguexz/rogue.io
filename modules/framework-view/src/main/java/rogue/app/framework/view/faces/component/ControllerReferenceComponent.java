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

package rogue.app.framework.view.faces.component;

import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.view.faces.AbstractFacesController;

import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience component for encapsulating a controller reference. The controller reference specified to this component
 * can be retrieved by its children by invoking the EL <code>#{controllerCtx.current}</code>.
 * <pre>
 *     &lt;r:controllerReference value="#{Admin.systemProperties}>
 *       <i>&lt;-- All reference to controllerCtx.current within this component will refer to the value Admin.systemProperties --></i>
 *       &lt;inputText value="#{controllerCtx.current.selectedResource.name}" ... />
 *     &lt;/r:controllerReference>
 * </pre>
 * <p/>
 * This component is part of the default namespace - <i>http://xmlns.jcp.org/jsf/component</i>
 *
 * @see ControllerContextHolder
 */
@FacesComponent(value = "controllerReference", createTag = true, tagName = "controllerReference")
public class ControllerReferenceComponent extends UIComponentBase
{
    private static final String CLASS_NAME = ControllerReferenceComponent.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Override
    public String getFamily()
    {
        return "Rogue.IO";
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback)
            throws FacesException
    {
        try
        {
            pushControllerAsCurrent();
            return super.invokeOnComponent(context, clientId, callback);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback)
    {
        try
        {
            pushControllerAsCurrent();
            return super.visitTree(context, callback);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public void processRestoreState(FacesContext context, Object state)
    {
        try
        {
            pushControllerAsCurrent();
            super.processRestoreState(context, state);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public void processDecodes(FacesContext context)
    {
        try
        {
            pushControllerAsCurrent();
            super.processDecodes(context);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public void processValidators(FacesContext context)
    {
        try
        {
            pushControllerAsCurrent();
            super.processValidators(context);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public void processUpdates(FacesContext context)
    {
        try
        {
            pushControllerAsCurrent();
            super.processUpdates(context);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public Object processSaveState(FacesContext context)
    {
        try
        {
            pushControllerAsCurrent();
            return super.processSaveState(context);
        }
        finally
        {
            popCurrentController();
        }
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException
    {
        pushControllerAsCurrent();
        super.encodeBegin(context);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException
    {
        try
        {
            super.encodeEnd(context);
        }
        finally
        {
            popCurrentController();
        }
    }

    /**
     * Push the controller associated with this component on to the stack.
     */
    private void pushControllerAsCurrent()
    {
        ControllerContextHolder holder = ControllerContextHolder.getInstance();
        if (holder != null)
        {
            holder.pushController(getController());
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, "pushControllerAsCurrent",
                        "Unable to acquire the controller context holder instance.");
        }
    }

    /**
     * Pop the current controller and verify if it is the same as that pushed by this component or not.
     */
    private void popCurrentController()
    {
        final String METHOD_NAME = "popCurrentController";
        ControllerContextHolder holder = ControllerContextHolder.getInstance();
        if (holder != null)
        {
            AbstractFacesController<?, ?> controller = holder.popController();
            if (!Objects.equals(controller, getController()))
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Popped controller does not match the controller associated with this component.");
                if (LOGGER.isLoggable(Level.FINE))
                {
                    // Generate a stack trace at FINE level so that the consumer can debug the phase in which this
                    // anamoly is noticed.
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME,
                                "Popped controller does not match the controller associated with this component.",
                                new AppRuntimeException(
                                        "Popped controller does not match the controller associated with this component."));
                }
            }
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                        "Unable to acquire the controller context holder instance.");
        }
    }


    /**
     * Get the controller associated with this component.
     *
     * @return the controller associated with this component.
     */
    public AbstractFacesController getController()
    {
        return (AbstractFacesController<?, ?>) getAttributes().get("value");
    }


}
