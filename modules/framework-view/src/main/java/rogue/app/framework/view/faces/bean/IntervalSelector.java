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

package rogue.app.framework.view.faces.bean;

import org.joda.time.*;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

//@Named
//@ViewScoped
public class IntervalSelector implements Serializable
{
    /**
     * The default event title.
     */
    private static final String DEFAULT_EVENT_TITLE = "Selected time interval";
    /**
     * The default duration is set to 2 hours.
     */
    private static final Duration DEFAULT_DURATION = new Duration(2L * 60L * 60L * 1000L);
    /**
     * The default start time for each day - set to 1100hrs local time.
     */
    private static final LocalTime DEFAULT_START_TIME = new LocalTime(11, 0);
    /**
     * The default end time for each day - set to 2300hrs local time.
     */
    private static final LocalTime DEFAULT_END_TIME = new LocalTime(23, 0);
    /**
     * The timezone used for calculating the date & time objects.
     */
    private TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");

    private String title = DEFAULT_EVENT_TITLE; // Initialize with the default event title.
    private Duration duration = DEFAULT_DURATION;
    private LocalTime dayStartTime = DEFAULT_START_TIME;
    private LocalTime dayEndTime = DEFAULT_END_TIME;
    private ScheduleModel scheduleModel;
    private ScheduleEvent scheduleEvent;
    private Interval minimumTimeInterval;
    private Interval selectedInterval;

    /**
     * Get the title specified on the event.
     *
     * @return the title specified on the event.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Set the title specified on the event.
     *
     * @param title the title specified on the event.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Get the time zone used for computing the time slot selection.
     *
     * @return the time zone used for computing the time slot selection.
     */
    public TimeZone getTimeZone()
    {
        return timeZone;
    }

    /**
     * Set the time zone used for computing the time slot selection.
     *
     * @param timeZone the time zone used for computing the time slot selection.
     */
    public void setTimeZone(TimeZone timeZone)
    {
        this.timeZone = timeZone;
    }

    /**
     * Get the duration of each time slot that can be selected.
     *
     * @return the duration of each time slot that can be selected.
     */
    public Duration getDuration()
    {
        return duration;
    }

    /**
     * Set the duration of each time slot that can be selected.
     *
     * @param duration the duration of each time slot that can be selected.
     */
    public void setDuration(Duration duration)
    {
        this.duration = duration;
    }

    /**
     * Get the selected time interval.
     *
     * @return the selected time interval.
     */
    public Interval getSelectedInterval()
    {
        if (selectedInterval == null)
        {
            selectedInterval = getMinimumTimeInterval();
        }
        return selectedInterval;
    }

    /**
     * Set the selected time interval.
     *
     * @param interval the selected time interval.
     */
    public void setSelectedInterval(Interval interval)
    {
        this.selectedInterval = interval;
    }

    /**
     * Get the start time for each day.
     *
     * @return the start time for each day.
     */
    public LocalTime getDayStartTime()
    {
        return dayStartTime;
    }

    /**
     * Set the start time for each day.
     *
     * @param dayStartTime the start time for each day.
     */
    public void setDayStartTime(LocalTime dayStartTime)
    {
        this.dayStartTime = dayStartTime;
    }

    /**
     * Get the end time for each day.
     *
     * @return the end time for each day.
     */
    public LocalTime getDayEndTime()
    {
        return dayEndTime;
    }

    /**
     * Set the end time for each day.
     *
     * @param dayEndTime the end time for each day.
     */
    public void setDayEndTime(LocalTime dayEndTime)
    {
        this.dayEndTime = dayEndTime;
    }

    /**
     * Get the earliest possible time interval that can be selected. {@link #getSelectedInterval()} will always be equal
     * or greater than this interval.
     *
     * @return the earliest possible time interval that can be selected.
     */
    public Interval getMinimumTimeInterval()
    {
        return minimumTimeInterval;
    }

    /**
     * Set the earliest possible time interval that can be selected. {@link #getSelectedInterval()} will always be equal
     * or greater than this interval.
     *
     * @param minimumTimeInterval the earliest possible time interval that can be selected.
     */
    public void setMinimumTimeInterval(Interval minimumTimeInterval)
    {
        this.minimumTimeInterval = minimumTimeInterval;
    }

    /**
     * Get the schedule model to be used with the UI component.
     *
     * @return the schedule model to be used with the UI component.
     */
    public ScheduleModel getScheduleModel()
    {
        if (scheduleModel == null)
        {
            scheduleModel = new DefaultScheduleModel();
            scheduleEvent = constructEvent(getTitle(), getSelectedInterval());
            scheduleModel.addEvent(scheduleEvent);
        }
        return scheduleModel;
    }

    /**
     * Process the time selection event.
     *
     * @param event the select event.
     */
    public void selectDateTimeInterval(SelectEvent event)
    {
        Date startDate = (Date) event.getObject();

        updateSchedule(new Interval(new DateTime(startDate), getDuration()));
    }

    /**
     * Get the closest interval to the specified instant.
     *
     * @param instant the DateTime instant.
     * @return the closes interval.
     */
    public Interval getClosestInterval(DateTime instant)
    {
        instant = instant.withSecondOfMinute(0);
        instant = instant.withMillisOfSecond(0);

        // Move to the next hour
        if (instant.getMinuteOfHour() > 0)
        {
            instant = instant.plusMinutes(60 - instant.getMinuteOfHour());
        }


        // Verify if the current hour of the day lies between the min & max times
        int diff = getDayStartTime().getHourOfDay() - instant.getHourOfDay();
        if (diff > 0)
        {
            instant = instant.plusHours(diff);
        }

        diff = instant.getHourOfDay() - getDayEndTime().getHourOfDay();
        if (diff > 0)
        {
            instant = instant.toDateMidnight().toDateTime();
            instant = instant.plusDays(1);
            instant = instant.plusHours(getDayStartTime().getHourOfDay());
        }

        return new Interval(instant, getDuration());
    }

    private ScheduleEvent constructEvent(String title, Interval interval)
    {
        if (interval == null)
        {
            interval = getClosestInterval(DateTime.now(DateTimeZone.forTimeZone(getTimeZone())));
        }

        return new DefaultScheduleEvent(title, interval.getStart().toDate(), interval.getEnd().toDate());
    }

    private void updateSchedule(Interval eventInterval)
    {
        if (scheduleEvent != null)
        {
            getScheduleModel().deleteEvent(scheduleEvent);
        }
        scheduleEvent = constructEvent(getTitle(), eventInterval);
        scheduleModel.addEvent(scheduleEvent);
    }

}
