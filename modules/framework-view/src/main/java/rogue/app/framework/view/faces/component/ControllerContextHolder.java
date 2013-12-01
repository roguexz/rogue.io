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

import rogue.app.framework.view.faces.AbstractFacesController;
import rogue.app.framework.view.util.FacesUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper object.
 */
@Named("controllerCtx")
@RequestScoped
public class ControllerContextHolder implements Serializable
{
    private static final String CLASS_NAME = ControllerContextHolder.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private Deque<AbstractFacesController<?, ?>> controllerQueue;

    public ControllerContextHolder()
    {
        controllerQueue = new ArrayDeque<>(3);
    }

    /**
     * Push the given controller instance to the top of the stack.
     *
     * @param controller the controller instance to push to the top of the stack.
     */
    public void pushController(AbstractFacesController<?, ?> controller)
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, "pushController",
                        "Pushing a new controller on to the stack: " + controller);
        }
        controllerQueue.push(controller);
    }

    /**
     * Pop the current controller instance.
     *
     * @return the current controller instance.
     */
    public AbstractFacesController<?, ?> popController()
    {
        AbstractFacesController<?, ?> controller = null;
        if (!controllerQueue.isEmpty())
        {
            controller = controllerQueue.pop();
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, "popController",
                            "Popping the current controller from the stack: " + controller);
            }
        }
        return controller;
    }

    /**
     * Peek and return the controller instance that is at the top of the stack.
     *
     * @return the current controller instance.
     */
    public AbstractFacesController<?, ?> getCurrent()
    {
        AbstractFacesController controller = controllerQueue.peek();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (controller == null && facesContext != null && controllerQueue.isEmpty())
        {
            // TODO: Figure a way around this hack - this is very dicey!
            // HACK: Needs to be removed. Probably will work as long as Faces sticks to serial execution.
            // 1. Get hold of the current instance
            UIComponent component = UIComponent.getCurrentComponent(facesContext);
            ControllerReferenceComponent ctrlRefCmp = null;
            while (component != null)
            {
                if (component instanceof ControllerReferenceComponent)
                {
                    ctrlRefCmp = (ControllerReferenceComponent) component;
                    break;
                }
                component = component.getParent();
            }

            if (ctrlRefCmp != null)
            {
                controller = ctrlRefCmp.getController();
            }
        }
        return controller;
    }

    public static ControllerContextHolder getInstance()
    {
        return FacesUtils.resolveObject("#{controllerCtx}");
    }
}
