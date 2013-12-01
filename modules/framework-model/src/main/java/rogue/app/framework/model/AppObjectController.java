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

import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.patterns.Delegation;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An <code>AppObjectController</code> provides an abstract implementation of the
 * {@link rogue.app.framework.model.AppObjectCollection} interface, which serves as the root object for most
 * controllers within the framework. Additionally, it implements the {@link rogue.app.framework.model.ResourceSelection}
 * interface, which provides a stateful notion to the various operations that get executed on the application object
 * itself. The <code>AppObjectController</code> supports the notion of hierarchical (chained) controllers where the
 * state of a child controller is influenced by the currently selected resource of the parent controller.
 */
public abstract class AppObjectController<T extends AppObject<T, K>, K> implements AppObjectCollection<T, K>,
        Delegation<AppObjectCollection<T, K>>, ResourceSelection<T>, Serializable
{
    private static final String CLASS_NAME = AppObjectController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /**
     * The controller collection to which this controller belongs. This information used primarily for constructing
     * pretty URIs.
     */
    private AppObjectControllerCollection controllerCollection;
    /**
     * The preferred name of this controller.
     */
    private String controllerName;
    /**
     * Class reference of the model object.
     */
    private Class<T> appObjClassRef;
    /**
     * The logical parent of this controller.
     */
    private AppObjectController<?, ?> parentController;
    /**
     * Map to store the logic child controllers owned by this controller object.
     */
    private Map<Class<? extends AppObject>, AppObjectController<?, ?>> childControllersMap;
    private Map<String, Class<? extends AppObject>> namedChildControllerTypes;
    private boolean reinitializeChildControllers = true;
    private AppObject<?, ?> parentResource;
    private ResourceSelectionHolder<T> resourceSelectionHolder;
    private boolean singleSelection;
    // Immutable references
    private Map<String, AppObjectController<?, ?>> namedChildControllers = Collections.emptyMap();
    private List<AppObjectController<?, ?>> childControllersList;


    protected AppObjectController()
    {
        // Default no-arg constructor required for CDI.
        processAnnotations();
        this.resourceSelectionHolder = new ResourceSelectionHolder<>();
        resourceSelectionHolder.setBinaryMode(false);
    }

    protected AppObjectController(AppObjectController<?, ?> parentController)
    {
        processAnnotations();
        this.resourceSelectionHolder = new ResourceSelectionHolder<>();
        resourceSelectionHolder.setBinaryMode(false);

        this.parentController = parentController;
    }

    /**
     * Get the {@link AppObjectControllerCollection collection) to which this controller belongs. If this controller is
     * a child to another controller & the controller collection has not been specifically overrident, then the entire
     * hierarchy will be traversed till the controller collection has been found.
     *
     * @return the collection to which this controller belongs.
     */
    public AppObjectControllerCollection getControllerCollection()
    {
        return controllerCollection != null ? controllerCollection :
               parentController != null ? parentController.getControllerCollection() : null;
    }

    /**
     * Set the {@link AppObjectControllerCollection collection) to which this controller belongs.
     *
     * @param controllerCollection the collection to which this controller belongs.
     */
    public void setControllerCollection(AppObjectControllerCollection controllerCollection)
    {
        this.controllerCollection = controllerCollection;
    }

    /**
     * Get the application object class associated with this controller.
     *
     * @return the application object class associated with this controller.
     */
    public Class<T> getAppObjClassRef()
    {
        return appObjClassRef;
    }

    /**
     * Get the preferred name for this controller.
     *
     * @return the preferred name for this controller.
     */
    public String getControllerName()
    {
        return controllerName;
    }

    /**
     * Get the <i>logical</i> parent of this controller.
     *
     * @return the <i>logical</i> parent of this controller.
     */
    public AppObjectController<?, ?> getParentController()
    {
        return parentController;
    }

    public AppObject getParentResource()
    {
        return parentResource;
    }

    public void setParentResource(AppObject<?, ?> parentResource)
    {
        this.parentResource = parentResource;
    }

    /**
     * Identifies if the current controller is intended to work on a single object only or not. If the controller is
     * intended for operating on a single selection, then the {@link #getSelectedResource()} will return the object
     * that is to be manipulated.
     *
     * @return true if the controller is intended to work on a single resource, false otherwise.
     */
    public boolean isSingleSelection()
    {
        return singleSelection;
    }

    public void setSingleSelection(boolean singleSelection)
    {
        this.singleSelection = singleSelection;
    }

    /**
     * Get a map of named child controllers. This is a convenience method that helps with better EL support.
     *
     * @return a map of named child controllers.
     */
    public Map<String, AppObjectController<?, ?>> getChildControllers()
    {
        if (namedChildControllers == null)
        {
            List<AppObjectController<?, ?>> list = getChildControllersList();
            namedChildControllers = new HashMap<>(list.size());
            for (AppObjectController<?, ?> controller : list)
            {
                namedChildControllers.put(controller.getControllerName(), controller);
            }
            namedChildControllers = Collections.unmodifiableMap(namedChildControllers);
        }
        return namedChildControllers;
    }

    /**
     * Get a list of child controllers associated with the currently selected ApplicationObject.
     *
     * @return a list of child controllers associated with the currently selected ApplicationObject.
     */
    public List<AppObjectController<?, ?>> getChildControllersList()
    {
        if (getSelectedResource() == null || childControllersMap == null)
        {
            return Collections.emptyList();
        }

        initChildControllers();

        if (childControllersList == null)
        {
            childControllersList = new ArrayList<>();
            childControllersList.addAll(childControllersMap.values());
            childControllersList = Collections.unmodifiableList(childControllersList);
        }

        return childControllersList;
    }

    /**
     * Get a child controller by its preferred name.
     *
     * @param name the name of the child controller.
     * @return a named child controller if found, or null.
     */
    public AppObjectController<?, ?> getChildController(String name)
    {
        final String METHOD_NAME = "getChildController(name)";

        if (namedChildControllerTypes != null)
        {
            Class<? extends AppObject> type = namedChildControllerTypes.get(name);
            if (type != null)
            {
                return getChildController(type);
            }
            else
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "No child controller found with name: " + name);
            }
        }
        return null;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public AppObjectController<?, ?> getChildController(Class<?> childType)
    {
        initChildControllers();
        return childControllersMap != null ? childControllersMap.get(childType) : null;
    }

    protected void addChildController(AppObjectController<?, ?> controller)
    {
        final String METHOD_NAME = "addChildController";
        if (childControllersMap == null)
        {
            childControllersMap = new LinkedHashMap<>(2);
            namedChildControllerTypes = new HashMap<>(2);
        }

        if (!childControllersMap.containsKey(controller.getAppObjClassRef()))
        {
            childControllersMap.put(controller.getAppObjClassRef(), controller);
            String ccName = controller.getControllerName();

            if (namedChildControllerTypes.containsKey(ccName) && namedChildControllerTypes.get(ccName) != controller
                    .getAppObjClassRef())
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                            "Attempting to add child controller which has a name that conflicts with an existing object type can lead to unexpected results. Controller name: "
                                    + ccName + " is mapped to both " + namedChildControllerTypes
                                    .get(ccName).getName() + " & " + controller.getAppObjClassRef().getName());
            }

            namedChildControllerTypes.put(controller.getControllerName(), controller.getAppObjClassRef());

            // TODO or NOT TODO .. Should I set the child controllers parent as this guy? Kinda makes sense
            controller.parentController = this;
        }
        childControllersList = null;
        namedChildControllers = null;
    }

    protected void removeChildController(AppObjectController<?, ?> controller)
    {
        if (childControllersMap != null && controller != null)
        {
            childControllersMap.remove(controller.getAppObjClassRef());
            namedChildControllerTypes.remove(controller.getControllerName());
            childControllersList = null;
            namedChildControllers = null;
        }
    }

    private void initChildControllers()
    {
        if (reinitializeChildControllers)
        {
            if (childControllersMap != null && !childControllersMap.isEmpty())
            {
                T item = getSelectedResource();
                for (AppObjectController<?, ?> controller : childControllersMap.values())
                {
                    if (controller == null)
                    {
                        if (LOGGER.isLoggable(Level.FINE))
                        {
                            LOGGER.logp(Level.FINE, CLASS_NAME, "initChildControllers",
                                        "A child controller is null. This should never happen. Debug the code.");
                        }
                        continue;
                    }

                    if (!Objects.equals(item, controller.getParentResource()))
                    {
                        controller.setParentResource(item);
                    }
                }
            }
            reinitializeChildControllers = false;
        }
    }

    ///
    /// Methods defined by ResourceSelection
    ///

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
    public void setSelectedResource(T resource)
    {
        resourceSelectionHolder.setSelectedResource(resource);
        reinitializeChildControllers = true;
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
    public void setSelectedResources(T[] resources)
    {
        resourceSelectionHolder.setSelectedResources(resources);
    }

    /**
     * Get the resource selection holder implementation that is used by this controller.
     *
     * @return the resource selection holder implementation that is used by this controller.
     */
    protected ResourceSelection<T> getResourceSelectionHolder()
    {
        return resourceSelectionHolder;
    }

    ///
    /// Methods defined by AppObjectCollection
    ///

    /**
     * {@inheritDoc}
     */
    @Override
    public T getNewInstance() throws AppRuntimeException
    {
        return getDelegate().getNewInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getNewInstance(AppObject<?, ?> parent) throws AppRuntimeException
    {
        return getDelegate().getNewInstance(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getMutableInstance(T appObject) throws AppRuntimeException
    {
        return getDelegate().getMutableInstance(appObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query newQueryInstance() throws AppRuntimeException
    {
        return getDelegate().newQueryInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T findById(K id) throws AppRuntimeException
    {
        return getDelegate().findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K parseId(String idAsString) throws AppRuntimeException
    {
        return getDelegate().parseId(idAsString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> find(Query query) throws AppRuntimeException
    {
        return getDelegate().find(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(Query query) throws AppRuntimeException
    {
        return getDelegate().count(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(T appObject) throws AppRuntimeException
    {
        getDelegate().save(appObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteById(K... keys) throws AppRuntimeException
    {
        List<T> appObjects = new ArrayList<>();
        for (K id : keys)
        {
            T appObj = findById(id);
            if (appObj != null)
            {
                appObjects.add(appObj);
            }
        }

        delete(appObjects.toArray((T[]) Array.newInstance(getAppObjClassRef(), appObjects.size())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(AppObject[] appObjects) throws AppRuntimeException
    {
        if (appObjects == null || appObjects.length == 0)
        {
            return;
        }

        // Since there is no notion of child controllers in the AppObjectCollection, the necessary cascade delete
        // is performed here itself.
        for (AppObject resource : appObjects)
        {
            CompositeQuery cq = new CompositeQuery(resource);
            cq.setStartIndex(0);
            cq.setMaxResults(-1);
            setSelectedResource((T) resource);
            for (AppObjectController<?, ?> childController : getChildControllersList())
            {
                List<? extends AppObject> children = childController.find(cq);
                childController.delete(children.toArray(new AppObject[children.size()]));
            }
            setSelectedResource(null);
        }
        // Finally delete all the existing objects as well.
        getDelegate().delete(appObjects);
    }

    ///
    /// Process the annotations associated with the controller object.
    ///
    private void processAnnotations()
    {
        ControllerFor controller = getClass().getAnnotation(ControllerFor.class);
        if (controller != null)
        {
            Class<T> classRef = (Class<T>) controller.model();
            if (classRef != null)
            {
                this.appObjClassRef = classRef;
            }
            else
            {
                throw new AppRuntimeException("Controller class reference is null");
            }
            this.controllerName = controller.name();
        }
        else
        {
            throw new AppRuntimeException(
                    "Missing controller annotation. Please ensure that the annotation has been set for the controller.");
        }
    }
}
