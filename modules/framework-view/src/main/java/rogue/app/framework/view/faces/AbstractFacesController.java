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

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.AppObjectController;
import rogue.app.framework.model.AppObjectControllerCollection;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;
import rogue.app.framework.view.*;
import rogue.app.framework.view.util.FacesUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>AbstractFacesController</code> is a generic abstract class that provides a, <b>view-friendly</b>, unified way
 * of accessing & manipulating data stored in a backend - such as a database. The <code>AbstractFacesController</code>
 * provides stub implementations for 6 types of {@link CommandInvocation} facilities - New / View / Edit / Save /
 * Delete / Search. These stubs can be easily extended by sub-classes to provide specific functionality.
 */
@PageMetadata(path = "/framework/templates/admin/resource-delete.xhtml", fragment = true, type = ActionType.DELETE)
public abstract class AbstractFacesController<T extends AppObject<T, K>, K> extends AppObjectController<T, K>
{
    private static final String CLASS_NAME = AbstractFacesController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final String OPERATION_FAILED_IN_ACTION_LISTENER = "OPERATION_FAILED_IN_ACTION_LISTENER";

    private CommandInvocation newCommand;
    private CommandInvocation<T> viewCommand;
    private CommandInvocation<T> editCommand;
    private CommandInvocation<T> deleteItemPreviewCommand;
    private CommandInvocation<T> deleteCommand;
    private CommandInvocation<String> searchCommand;
    private CommandInvocation cancelCommand;
    private CommandInvocation saveCommand;

    private String commandOutcomeBase;
    private String newCommandOutcome;
    private String searchCommandOutcome;
    private String cancelCommandOutcome;

    private boolean newCommandAvailable = true;
    private boolean viewCommandAvailable = true;
    private boolean editCommandAvailable = true;
    private boolean deleteItemPreviewCommandAvailable = true;
    private boolean deleteCommandAvailable = true;
    private boolean searchCommandAvailable = true;
    private boolean cancelCommandAvailable = true;
    private boolean saveCommandAvailable = true;

    private transient PageInfo listingPageMetadata;
    private transient PageInfo detailsPageMetadata;
    private transient Map<PageType, Map<ActionType, PageInfo>> pagesInfo;
    private transient boolean pageInfoRead;

    private String previousSearchString;
    private DataModel<T> dataModel;
    private int pageSize = 10;
    private Object parentSelectedItem;
    private List<AbstractFacesController<?, ?>> parentHierarchy;
    private boolean resourcePickerMode;

    protected AbstractFacesController()
    {
        super();
        initCommands();
    }

    protected AbstractFacesController(AppObjectController<?, ?> parentController)
    {
        super(parentController);

        // Initialize the command invocation objects.
        initCommands();
    }

    private void initCommands()
    {
        newCommand = new NewItemCommand();
        viewCommand = new ViewCommand();
        editCommand = new EditItemCommand();
        deleteItemPreviewCommand = new DeleteItemPreviewCommand();
        deleteCommand = new DeleteItemCommand();
        searchCommand = new SearchItemsCommand();
        cancelCommand = new CancelCommand();
        saveCommand = new SaveCommand();
    }

    /**
     * Get the hierarchy of parent controllers. This is a useful method when painting the bread crumbs on the UI.
     *
     * @return the hierarchy of parent controllers.
     */
    public List<AbstractFacesController<?, ?>> getParentHierarchy()
    {
        if (parentHierarchy == null)
        {
            List<AbstractFacesController<?, ?>> list = new ArrayList<>();
            AbstractFacesController<?, ?> parentController = (AbstractFacesController<?, ?>) getParentController();
            if (parentController != null)
            {
                list.addAll(parentController.getParentHierarchy());
                list.add(parentController);
            }
            list = Collections.unmodifiableList(list);

            this.parentHierarchy = list;
        }
        return parentHierarchy;
    }

    /**
     * Construct the data model appropriate for the controller. Implementations should check the value stored in
     * the {@link rogue.app.framework.view.faces.AbstractFacesController.SearchItemsCommand#getSelectedResource()
     * search command} while constructing the data model.
     *
     * @return a pagination supported data model.
     */
    protected abstract DataModel<T> constructDataModel();

    /**
     * Allows subclasses to setup various attributes before the item is displayed in VIEW mode.
     */
    protected void setupViewInstance()
    {
        // Do nothing.
    }

    public int getPageSize()
    {
        // TODO: Do I still need this?
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    /**
     * Identifies if the controller is being used to select a resource via a dialog picker.
     *
     * @return true if the controller is in dialog selection mode or not, false otherwise.
     */
    public boolean isResourcePickerMode()
    {
        return resourcePickerMode;
    }

    /**
     * Set the controller's resource picker mode state.
     *
     * @param resourcePickerMode true if the controller is in dialog selection mode or not, false otherwise.
     */
    public void setResourcePickerMode(boolean resourcePickerMode)
    {
        this.resourcePickerMode = resourcePickerMode;

        setNewCommandAvailable(!resourcePickerMode);
        setViewCommandAvailable(!resourcePickerMode);
        setEditCommandAvailable(!resourcePickerMode);
        setDeleteCommandAvailable(!resourcePickerMode);
        setDeleteItemPreviewCommandAvailable(!resourcePickerMode);
        setCancelCommandAvailable(!resourcePickerMode);
        setSaveCommandAvailable(!resourcePickerMode);
    }

    /**
     * Get the data model for the current view.
     *
     * @return the data model for the current view.
     */
    public DataModel<T> getDataModel()
    {
        final String METHOD_NAME = "getDataModel";
        if (dataModel == null)
        {
            try
            {
                dataModel = constructDataModel();
                if (dataModel instanceof LazyDataModel)
                {
                    ((LazyDataModel) dataModel).setPageSize(pageSize);
                }
                else if (dataModel instanceof Pagination)
                {
                    ((Pagination) dataModel).setItemsPerPage(pageSize);
                }
            }
            catch (Exception e)
            {
                FacesMessage message =
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Unable to fetch data.", e.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, message);
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to fetch data.", e);


                dataModel = new LazyDataModel<T>()
                {
                    @Override
                    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder,
                                        Map<String, String> filters)
                    {
                        return Collections.emptyList();
                    }
                };
            }
        }
        return dataModel;
    }

    /**
     * Invalidate the present data model.
     */
    public void invalidateDataModel()
    {
        dataModel = null;
    }

    @Override
    public void setParentResource(AppObject<?, ?> parentResource)
    {
        super.setParentResource(parentResource);
        // Invalidate the listing page metadata & the command output base as well.
        commandOutcomeBase = null;
        newCommandOutcome = null;
        searchCommandOutcome = null;
        cancelCommandOutcome = null;
        listingPageMetadata = null;
        detailsPageMetadata = null;
    }

    @Override
    public void setSelectedResource(T resource)
    {
        super.setSelectedResource(resource);
        // Invalidate page metadata elements.
        detailsPageMetadata = null;
    }

    /**
     * Get the page metadata associated with the entity listing page.
     *
     * @return the page metadata associated with the entity listing page.
     */
    public PageInfo getListingPageMetadata()
    {
        if (listingPageMetadata == null)
        {

            Pages pages = getClass().getAnnotation(Pages.class);
            if (pages != null)
            {
                for (PageMetadata pm : pages.value())
                {
                    if (pm.pageType() == PageType.ITEMS_LISTING)
                    {
                        if (getControllerCollection() != null)
                        {
                            initCommandOutcomeBase();
                            listingPageMetadata =
                                    new PageInfo(pm.displayName(), commandOutcomeBase, pm.pageType(), pm.path(),
                                                 pm.fragment());
                        }
                        else
                        {
                            listingPageMetadata = new PageInfo(pm);
                        }
                    }
                }
            }
        }
        return listingPageMetadata;
    }

    /**
     * Get the page metadata associated with the entity details page.
     *
     * @return the page metadata associated with the entity details page.
     */
    public PageInfo getDetailsPageMetadata()
    {
        if (detailsPageMetadata == null)
        {
            Pages pages = getClass().getAnnotation(Pages.class);
            if (pages != null)
            {
                for (PageMetadata pm : pages.value())
                {
                    if (pm.pageType() == PageType.ITEM_DETAILS)
                    {
                        if (getControllerCollection() != null)
                        {
                            T resource = getSelectedResource();
                            if (resource != null && resource.getId() != null)
                            {
                                initCommandOutcomeBase();
                                detailsPageMetadata =
                                        new PageInfo(pm.displayName(),
                                                     commandOutcomeBase + "/" + resource.getIdAsString(), pm.pageType(),
                                                     pm.path(), pm.fragment());
                            }
                        }
                        else
                        {
                            detailsPageMetadata = new PageInfo(pm);
                        }
                    }
                }
            }
        }
        return detailsPageMetadata;
    }

    /**
     * Get the page information associated with this controller for the request page type (listing vs details) and
     * the specified action type.
     *
     * @param pageType   the page type - listing vs. details
     * @param actionType the action type - VIEW / CREATE / DELETE / UPDATE / etc. A value of null defaults to VIEW.
     * @return the page information, if available.
     */
    public PageInfo getPageInfo(PageType pageType, ActionType actionType)
    {
        initPagesMetadata();
        if (actionType == null)
        {
            actionType = ActionType.VIEW;
        }
        Map<ActionType, PageInfo> pagesByType = pagesInfo.get(pageType);
        return pagesByType != null ? pagesByType.get(actionType) : null;
    }

    /**
     * Initialize the pages metadata for this controller. This method reads the current class's {@link Pages}
     * annotation and identifies what the listing and details page ought to be.
     */
    protected void initPagesMetadata()
    {
        final String METHOD_NAME = "initPagesMetadata";


        if (!pageInfoRead)
        {

            // Parse the data-top down.
            // Typical order of processing is:
            //       i. Process a PageMetadata annotation
            //      ii. Process the Pages annotation next
            //
            // Override existing data at each level.
            Stack<Class<?>> hierarchy = new Stack<>();
            Class<?> clazz = getClass();
            while (clazz != null)
            {
                hierarchy.push(clazz);
                clazz = clazz.getSuperclass();
            }

            while (!hierarchy.isEmpty())
            {
                clazz = hierarchy.pop();

                // Parse any direct PageMetadata instances.
                PageMetadata pageMetadata = clazz.getAnnotation(PageMetadata.class);
                if (pageMetadata != null)
                {
                    processPageMetadata(pageMetadata);
                }

                // Now handle the Pages annotation
                Pages pages = this.getClass().getAnnotation(Pages.class);
                if (pages != null)
                {
                    for (PageMetadata pm : pages.value())
                    {
                        processPageMetadata(pm);
                    }
                }
            }


            pageInfoRead = true;
        }


        // Check if the parentController controller's selectedItem has changed or not, in which case we need to refresh the
        // data model
        AppObjectController<?, ?> parentController = getParentController();
        if (parentController != null && !Objects.equals(parentController.getSelectedResource(), parentSelectedItem))
        {
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        "Clearing the cache for the faces controller as the parent's selected item has changed.");
            invalidateDataModel();

            parentSelectedItem = parentController.getSelectedResource();
        }
    }

    private void processPageMetadata(PageMetadata pm)
    {
        if (pagesInfo == null)
        {
            pagesInfo = new HashMap<>(2);
        }

        Map<ActionType, PageInfo> typePageInfoMap = pagesInfo.get(pm.pageType());
        if (typePageInfoMap == null)
        {
            typePageInfoMap = new HashMap<>(1);
            pagesInfo.put(pm.pageType(), typePageInfoMap);
        }

        for (ActionType at : pm.type())
        {
            typePageInfoMap.put(at, new PageInfo(pm));
        }

    }

    ///
    /// *********************** The CommandInvocation objects
    ///

    /**
     * Get the <code>CommandInvocation</code> for handling the creation of new application objects. This invocation
     * object does not hold any value.
     *
     * @return the <code>CommandInvocation</code> for handling the creation of new application objects.
     */
    public CommandInvocation getNewCommand()
    {
        return newCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for displaying a preview before proceeding with the delete operation.
     *
     * @return the <code>CommandInvocation</code> for displaying a preview before proceeding with the delete operation.
     */
    public CommandInvocation<T> getDeleteItemPreviewCommand()
    {
        return deleteItemPreviewCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for handling the deletion of selected application objects. The value
     * stored in the <code>CommandInvocation</code> represents the item to be deleted.
     * <p/>
     *
     * @return the <code>CommandInvocation</code> for handling the deletion of selected application objects.
     */
    public CommandInvocation<T> getDeleteCommand()
    {
        return deleteCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for handling viewing of a selected application object. The value stored
     * in the <code>CommandInvocation</code> represents the item to be viewed.
     *
     * @return the <code>CommandInvocation</code> for handling viewing of a selected application object.
     */
    public CommandInvocation<T> getViewCommand()
    {
        return viewCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for handling the editing of a selected application object. The value
     * stored in the <code>CommandInvocation</code> represents the item to be edited.
     *
     * @return the <code>CommandInvocation</code> for handling the editing of a selected application object.
     */
    public CommandInvocation<T> getEditCommand()
    {
        return editCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for searching application objects. The value stored in the object
     * represents the search string.
     *
     * @return the <code>CommandInvocation</code> for searching application objects.
     */
    public CommandInvocation<String> getSearchCommand()
    {
        return searchCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for canceling a Create or Edit operation.
     *
     * @return the <code>CommandInvocation</code> for canceling a Create or Edit operation.
     */
    public CommandInvocation getCancelCommand()
    {
        return cancelCommand;
    }

    /**
     * Get the <code>CommandInvocation</code> for saving the current in view application object (Create or Edit op).
     *
     * @return the <code>CommandInvocation</code> for saving the current in view application object (Create or Edit op).
     */
    public CommandInvocation getSaveCommand()
    {
        return saveCommand;
    }

    ///
    /// *********************** The Methods Invoked by the CommandInvocation objects
    ///


    /**
     * Constructs the outcome of a command given the command suffix.
     *
     * @param actionType the suffix that should be appended at the end of the URI.
     * @return the outcome of a command given the command suffix.
     */
    protected String getCommandOutcome(ActionType actionType, boolean includeId)
    {
        initCommandOutcomeBase();

        StringBuilder builder = new StringBuilder(100);
        builder.append(commandOutcomeBase);
        if (includeId)
        {
            T resource = getSelectedResource();
            if (resource != null && resource.getId() != null)
            {
                builder.append("/").append(resource.getIdAsString());
            }
        }

        if (actionType != null && ActionType.VIEW != actionType)
        {
            builder.append("/").append(actionType.toString());
        }

        return builder.toString();
    }

    /**
     * Initializes the base URI that leads up to this controller.
     */
    private void initCommandOutcomeBase()
    {
        if (commandOutcomeBase == null)
        {
            StringBuilder builder = new StringBuilder(100);
            AppObjectControllerCollection collection = getControllerCollection();
            if (collection != null)
            {
                builder.append("/").append(collection.getCollectionName());

                List<AbstractFacesController<?, ?>> hierarchy = getParentHierarchy();
                for (AbstractFacesController<?, ?> controller : hierarchy)
                {
                    builder.append("/").append(controller.getControllerName());
                    AppObject appObject = controller.getSelectedResource();
                    if (appObject != null)
                    {
                        builder.append("/").append(appObject.getIdAsString());
                    }
                }
                builder.append("/").append(getControllerName());
            }
            commandOutcomeBase = builder.toString();
        }
    }

    ///
    /// *********************** NewCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation invokes {@link #getNewInstance()} and
     * passes its value to {@link #setSelectedResource(rogue.app.framework.model.AppObject) setSelectedResource} and
     * navigates to the create page where further details can be entered.
     *
     * @return the page to navigate to.
     */
    protected String newCommandAction()
    {
        AppObject parent = getParentResource();
        T resource;
        if (parent != null)
        {
            resource = getNewInstance(parent);
        }
        else
        {
            resource = getNewInstance();
        }
        setSelectedResource(resource);

        if (newCommandOutcome == null)
        {
            newCommandOutcome = getCommandOutcome(ActionType.CREATE, false);
        }
        return newCommandOutcome;
    }

    /**
     * Convenience method for subclasses. The default implementation does nothing.
     *
     * @param event action event passed in by the framework.
     */
    protected void newCommandActionListener(ActionEvent event)
    {
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isNewCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isNewCommandAllowed()
    {
        return newCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the new command invocation is allowed.
     */
    public void setNewCommandAvailable(boolean available)
    {
        this.newCommandAvailable = available;
    }


    ///
    /// *********************** ViewCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default method does the following:
     * <p/>
     * <ul>
     * <li>Get the value stored on {@link #getViewCommand() viewCommand}</li>
     * <li>Set it as the {@link #getSelectedResource() selected item}</li>
     * <li>Navigate to the value in the <code>PageMetadata</code> annotation, with
     * <code>type = PageType.ITEM_DETAILS</code>, assigned to this class.</li>
     * </ul>
     */
    protected String viewCommand()
    {
        setSelectedResource(getViewCommand().getSelectedResource());
        setupViewInstance();

        // This should be request scoped.
        StateHolder holder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        String key = getControllerCollection() + "_" + CLASS_NAME + "_viewCommand";
        String command = holder.get(key);
        if (command == null)
        {
            command = getCommandOutcome(ActionType.VIEW, true);
            holder.put(key, command);
        }
        return command;
    }

    /**
     * Convenience method for subclasses. The default implementation does nothing.
     *
     * @param event action event passed in by the framework.
     */
    protected void viewCommandActionListener(ActionEvent event)
    {
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isViewCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the command invocation is allowed.
     */
    protected boolean isViewCommandAllowed()
    {
        return viewCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setViewCommandAvailable(boolean available)
    {
        this.viewCommandAvailable = available;
    }


    ///
    /// *********************** EditCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation of this method will do the following:
     * <p/>
     * <ul>
     * <li>Get the value stored on {@link #getEditCommand() editCommand}</li>
     * <li>Request for a {@link AppObjectController#getMutableInstance(AppObject)} editable instance}
     * of the item</li>
     * <li>Set editable item as the {@link #getSelectedResource() selected item}</li>
     * <li>Navigate to the value in the <code>PageMetadata</code> annotation, with
     * <code>type = PageType.ITEM_DETAILS</code>, assigned to this class.</li>
     * </ul>
     *
     * @return the page to navigate to.
     */
    protected String editCommandAction()
    {
        setSelectedResource(getMutableInstance(getEditCommand().getSelectedResource()));

        // This should be request scoped.
        StateHolder holder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        String key = getControllerCollection() + "_" + CLASS_NAME + "_editCommand";
        String command = holder.get(key);
        if (command == null)
        {
            command = getCommandOutcome(ActionType.UPDATE, true);
            holder.put(key, command);
        }
        return command;
    }

    /**
     * Convenience method for subclasses. The default implementation does nothing.
     *
     * @param event action event passed in by the framework.
     */
    protected void editCommandActionListener(ActionEvent event)
    {
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isEditCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isEditCommandAllowed()
    {
        return editCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setEditCommandAvailable(boolean available)
    {
        this.editCommandAvailable = available;
    }


    ///
    /// *********************** DeleteItemPreviewCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation navigates the user to the delete preview screen.
     *
     * @return the action for the delete preview screen.
     */
    protected String deleteItemPreviewCommandAction()
    {
        // Move the selected resources over to the delete command.
//        T resource = getSelectedResource();
//        if (resource != null)
//        {
//            deleteCommand.setSelectedResource(resource);
//        }
//        else
//        {
//            deleteCommand.setSelectedResources(getSelectedResources());
//        }

        // This should be request scoped.
        StateHolder holder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        String key = getControllerCollection() + "_" + CLASS_NAME + "_deleteItemPreviewCommand";
        String command = holder.get(key);
        if (command == null)
        {
            command = getCommandOutcome(ActionType.DELETE, true);
            holder.put(key, command);
        }

        return command;
    }

    /**
     * Convenience method for subclasses. The default implementation does nothing.
     *
     * @param event action event passed in by the framework.
     */
    protected void deleteItemPreviewCommandActionListener(ActionEvent event)
    {
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isDeleteItemPreviewCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isDeleteItemPreviewCommandAllowed()
    {
        return deleteItemPreviewCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setDeleteItemPreviewCommandAvailable(boolean available)
    {
        this.deleteItemPreviewCommandAvailable = available;
    }


    ///
    /// *********************** DeleteCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation does the actual deletion.
     *
     * @return null
     */
    protected String deleteCommandAction()
    {
        List<T> deleteList = getDeleteCommand().getValuesAsList();
        delete(deleteList.toArray((T[]) Array.newInstance(getAppObjClassRef(), deleteList.size())));
        invalidateDataModel();
        setSelectedResource(null);
        setSelectedResources(null);
        // Identify the page that the user should be navigated to next.
        return viewCommand();
    }

    /**
     * Convenience method for subclasses. The default implementation does nothing.
     *
     * @param event action event passed in by the framework.
     */
    protected void deleteCommandActionListener(ActionEvent event)
    {
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isDeleteCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isDeleteCommandAllowed()
    {
        return deleteCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setDeleteCommandAvailable(boolean available)
    {
        this.deleteCommandAvailable = available;
    }


    ///
    /// *********************** SearchCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation navigates the user to the search results page.
     *
     * @return the search results page.
     */
    protected String searchCommandAction()
    {
        if (searchCommandOutcome == null)
        {
            searchCommandOutcome = getCommandOutcome(ActionType.VIEW, false);
        }
        return searchCommandOutcome;
    }

    /**
     * Convenience method for subclasses. The default implementation does the following:
     * <p/>
     * <ul>
     * <li>Check if the search query string has changed.</li>
     * <li>If the query string has changed then invalidate the dataModel</li>
     * </ul>
     *
     * @param event action event passed in by the framework.
     */
    protected void searchCommandActionListener(ActionEvent event)
    {
        if (!Objects.equals(previousSearchString, getSearchCommand().getSelectedResource()))
        {
            // Reset the search string.
            previousSearchString = StringUtils.trim(getSearchCommand().getSelectedResource());
            invalidateDataModel();
        }
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isSearchCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isSearchCommandAllowed()
    {
        return searchCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setSearchCommandAvailable(boolean available)
    {
        this.searchCommandAvailable = available;
    }


    ///
    /// *********************** CancelCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation navigates the user to either the listing page,
     * or to the resource view page depending on the current operation state.
     */
    protected String cancelCommandAction()
    {
        if (cancelCommandOutcome == null)
        {
            cancelCommandOutcome = getCommandOutcome(ActionType.VIEW, false);
        }

        T item = getSelectedResource();
        if (item != null)
        {
            getViewCommand().setSelectedResource(item);
            return viewCommand();
        }

        return cancelCommandOutcome;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param event action event passed in by the framework.
     */
    protected void cancelCommandActionListener(ActionEvent event)
    {
        T item = getSelectedResource();
        if (item != null && item.isMutable() && item.getId() != null)
        {
            // We are in edit mode.
            setSelectedResource(item.getBaseResource());
        }
        else
        {
            setSelectedResource(null);
            setSelectedResources(null);
        }

    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isCancelCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isCancelCommandAllowed()
    {
        return cancelCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setCancelCommandAvailable(boolean available)
    {
        this.cancelCommandAvailable = available;
    }

    ///
    /// *********************** SaveCommand - methods
    ///

    /**
     * Convenience method for subclasses. The default implementation saves the object that is being currently edited.
     *
     * @return null
     */
    protected String saveCommandAction()
    {
        T resource = getSelectedResource();
        if (resource.isDirty())
        {
            getDelegate().save(resource);
            invalidateDataModel();
            setSelectedResource(resource);
        }
        else
        {
            setSelectedResource(resource.getBaseResource());
        }

        // This should be request scoped.
        StateHolder holder = ScopedStateHolder.getStateHolder(RequestScoped.class);
        String key = getControllerCollection() + "_" + CLASS_NAME + "_saveCommand";
        String command = holder.get(key);
        if (command == null)
        {
            command = getCommandOutcome(ActionType.VIEW, true);
            holder.put(key, command);
        }
        return command;
    }

    /**
     * Convenience method for subclasses. The default implementation does nothing.
     *
     * @param event action event passed in by the framework.
     */
    protected void saveCommandActionListener(ActionEvent event)
    {
    }

    /**
     * Convenience method for subclasses. The default implementation returns false.
     *
     * @return true if the invocation should be immediate.
     */
    protected boolean isSaveCommandImmediate()
    {
        return false;
    }

    /**
     * Convenience method for subclasses.
     *
     * @return true if the new command invocation is allowed.
     */
    protected boolean isSaveCommandAllowed()
    {
        return saveCommandAvailable;
    }

    /**
     * Convenience method for subclasses.
     *
     * @param available pass in true if the command invocation is allowed.
     */
    public void setSaveCommandAvailable(boolean available)
    {
        this.saveCommandAvailable = available;
    }


    /* =============================================================================================================== *
     *                                                                                                                 *
     * Class implementations of CommandInvocation to work around the limitation of Java reflection being unable to     *
     * access members within inner classes.                                                                            *
     *                                                                                                                 *
     * =============================================================================================================== *
     */

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class NewItemCommand extends CommandInvocation<T>
    {
        public NewItemCommand()
        {
            super("New Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                newCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "NewItemCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return newCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "NewItemCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);

                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isNewCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isNewCommandAllowed();
        }
    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class EditItemCommand extends CommandInvocation<T>
    {
        public EditItemCommand()
        {
            super("Edit Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                editCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "EditItemCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return editCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "EditItemCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isEditCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isEditCommandAllowed();
        }

    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class DeleteItemPreviewCommand extends CommandInvocation<T>
    {
        public DeleteItemPreviewCommand()
        {
            super("Preview Delete Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                deleteItemPreviewCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "DeleteItemPreviewCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return deleteItemPreviewCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "DeleteItemPreviewCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isDeleteItemPreviewCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isDeleteItemPreviewCommandAllowed();
        }

    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class DeleteItemCommand extends CommandInvocation<T>
    {
        public DeleteItemCommand()
        {
            // Share the resource selection holder between the delete command and the controller.
            super("Delete Command", getResourceSelectionHolder());
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                deleteCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "DeleteItemCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return deleteCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "DeleteItemCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isDeleteCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isDeleteCommandAllowed();
        }

    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class SearchItemsCommand extends CommandInvocation<String>
    {
        public SearchItemsCommand()
        {
            super("Search Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                searchCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "SearchItemsCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return searchCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "SearchItemsCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isSearchCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isSearchCommandAllowed();
        }

    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class CancelCommand extends CommandInvocation
    {

        public CancelCommand()
        {
            super("Cancel Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                cancelCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "CancelCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return cancelCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "CancelCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isCancelCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isCancelCommandAllowed();
        }

    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class SaveCommand extends CommandInvocation
    {

        public SaveCommand()
        {
            super("Save Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                saveCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "SaveCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return saveCommandAction();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "SaveCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isSaveCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isSaveCommandAllowed();
        }

    }

    /**
     * Class implementation of CommandInvocation to work around the limitations of accessing members in anonymous inner
     * classes.
     */
    public class ViewCommand extends CommandInvocation<T>
    {

        public ViewCommand()
        {
            super("View Command", null);
        }

        @Override
        public void invokeActionListener(ActionEvent event)
        {
            try
            {
                viewCommandActionListener(event);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, "ViewCommand", "invokeActionListener",
                            "Failed to execute action listener.", e);
                FacesUtils.addErrorMessage("Operation failed.", e);
                ScopedStateHolder.getStateHolder(RequestScoped.class)
                                 .put(OPERATION_FAILED_IN_ACTION_LISTENER, true);
            }
        }

        @Override
        public String invokeAction()
        {
            if (!ScopedStateHolder.getStateHolder(RequestScoped.class)
                                  .containsKey(OPERATION_FAILED_IN_ACTION_LISTENER))
            {
                try
                {
                    return viewCommand();
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, "ViewCommand", "invokeAction",
                                "Failed to execute action.", e);
                    FacesUtils.addErrorMessage("Operation failed.", e);
                }
            }
            return null;
        }

        @Override
        public boolean isImmediate()
        {
            return isViewCommandImmediate();
        }

        @Override
        public boolean isAllowed()
        {
            return isViewCommandAllowed();
        }

    }

}
