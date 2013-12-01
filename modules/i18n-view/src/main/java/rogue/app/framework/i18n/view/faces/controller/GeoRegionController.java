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

package rogue.app.framework.i18n.view.faces.controller;

import rogue.app.framework.i18n.model.GeoRegion;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.view.faces.EntityFacesController;

import java.util.logging.Logger;

public abstract class GeoRegionController<T extends GeoRegion<T, P, C>, P, C> extends EntityFacesController<T>
{
    private static final String CLASS_NAME = GeoRegionController.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private static final String ERROR_LOADING_CALENDAR = "error.loading.calendar";

    private static final String CALENDAR_ATTRIBUTE_NAME = "associated.calendar";
//    private CalendarWrapper calendarWrapper;
//    private CalendarsController calendarController;

    protected GeoRegionController()
    {
        super();
    }

    protected GeoRegionController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    @Override
    public void setParentResource(AppObject<?, ?> parent)
    {
        super.setParentResource(parent);
        invalidateDataModel();
    }

    public boolean isCalendarDefined()
    {
        T item = getSelectedResource();
        return item != null && Attributes.class.isAssignableFrom(item.getClass()) &&
                ((Attributes) item).isAttributeDefined(CALENDAR_ATTRIBUTE_NAME);
    }

//    public void createCalendar(ActionEvent event)
//    {
//        try
//        {
//            CalendarManager manager = CalendarManager.getInstance();
//            if (manager.isConfigured())
//            {
//                T item = getSelectedResource();
//                calendarWrapper = new CalendarWrapper();
//                calendarWrapper.setSummary("Holiday Calendar for " + item.getName());
//                calendarWrapper.setDescription(
//                        String.format("Calendar associated with [%s: %s, Id: %d]", getAppObjClassRef().getSimpleName(),
//                                      item.getName(), item.getId().getId()));
//
//                calendarWrapper = manager.saveCalendar(calendarWrapper);
//
//                item = item.getMutableInstance();
//                ((Attributes) item).setAttribute(CALENDAR_ATTRIBUTE_NAME, calendarWrapper.getId());
//                getDelegate().save(item);
//            }
//            else
//            {
//                FacesMessage message = new FacesMessage("Unable to create a calendar. Please try again later.");
//                message.setSeverity(FacesMessage.SEVERITY_WARN);
//                FacesContext.getCurrentInstance().addMessage(null, message);
//            }
//        }
//        catch (Exception e)
//        {
//            LOGGER.logp(Level.WARNING, CLASS_NAME, "createCalendar", "Failed to create calendar", e);
//            FacesUtils.addErrorMessage(e, "Failed to create calendar");
//        }
//    }
//
//    public CalendarsController getCalendarsController()
//    {
//        if (calendarController == null)
//        {
//            calendarController = new CalendarsController(this);
//            calendarController.setListingPageMetadata(new PageMetadataImpl("Calendars", "", PageType.ITEMS_LISTING));
//            calendarController.setDetailsPageMetadata(
//                    new PageMetadataImpl("Calendar", "CalendarDetails?faces-redirect=true", PageType.ITEM_DETAILS));
//            calendarController.getEventsController().setListingPageMetadata(
//                    new PageMetadataImpl("Events", "EventsListing?faces-redirect=true", PageType.ITEMS_LISTING));
//            calendarController.getEventsController().setDetailsPageMetadata(
//                    new PageMetadataImpl("Event", "EventDetails?faces-redirect=true", PageType.ITEM_DETAILS));
//        }
//
//        if (isCalendarDefined())
//        {
//            if (calendarWrapper == null &&
//                    !ScopedStateHolder.getScopedStateHolder(Scope.VIEW).containsKey(ERROR_LOADING_CALENDAR))
//            {
//                try
//                {
//                    String calendarId =
//                            ((Attributes) getSelectedResource()).getAttribute(CALENDAR_ATTRIBUTE_NAME);
//                    CalendarManager manager = CalendarManager.getInstance();
//                    if (manager.isConfigured())
//                    {
//                        calendarWrapper = manager.getCalendar(calendarId);
//                    }
//                }
//                catch (Exception e)
//                {
//                    FacesMessage message = new FacesMessage("Unable to acquire the associated calendar.");
//                    message.setSeverity(FacesMessage.SEVERITY_WARN);
//                    FacesContext.getCurrentInstance().addMessage(null, message);
//                    LOGGER.logp(Level.WARNING, CLASS_NAME, "getCalendarsController",
//                                "Unable to acquire the associated calendar.", e);
//
//                    ScopedStateHolder.getScopedStateHolder(Scope.VIEW).put(ERROR_LOADING_CALENDAR, true);
//                }
//            }
//        }
//
//        calendarController.setSelectedResource(calendarWrapper);
//        return calendarController;
//    }
}
