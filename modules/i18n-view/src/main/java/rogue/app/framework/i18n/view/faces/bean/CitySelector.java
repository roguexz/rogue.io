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

package rogue.app.framework.i18n.view.faces.bean;

import rogue.app.framework.i18n.model.City;
import rogue.app.framework.i18n.view.faces.controller.CountriesController;
import rogue.app.framework.i18n.view.util.LocationContext;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Backing bean to support selecting of a city.
 */
//@Named
//@ViewScoped
public class CitySelector implements Serializable
{
    private static final String CLASS_NAME = CitySelector.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    //@Inject
    CountriesController countriesController;
    //@Inject
    private LocationContext locationContext;

    /**
     * Initialize the various geo-region controllers based on the given city.
     */
    public void setCity(City city)
    {
        initInjectedMembers();

        if (city != null)
        {
            getCountriesController().setSelectedResource(city.getCountry());
            getCountriesController().getStatesController().setSelectedResource(city.getState());
            getCountriesController().getStatesController().getCitiesController().setSelectedResource(
                    city);
        }
    }

    public City getCity()
    {
        initInjectedMembers();

        return getCountriesController().getStatesController().getCitiesController().getSelectedResource();
    }

    /**
     * Get the countries controller reference.
     *
     * @return the countries controller reference.
     */
    public CountriesController getCountriesController()
    {
        initInjectedMembers();

        if (countriesController.getSelectedResource() == null)
        {
            // Initialize it with data available from the location context
            countriesController.setSelectedResource(locationContext.getCountry());
        }
        return countriesController;
    }

    private void initInjectedMembers()
    {
        final String METHOD_NAME = "initInjectedMembers";

        if (countriesController == null)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Members not injected: ResourcePicker");
            if (LOGGER.isLoggable(Level.FINEST))
            {
                LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Members not injected", new Exception());
            }
            countriesController = new CountriesController();
        }

        if (locationContext == null)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Members not injected: LocationContext");
            if (LOGGER.isLoggable(Level.FINEST))
            {
                LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Members not injected", new Exception());
            }
            locationContext = LocationContext.getInstance();
        }

        countriesController.setResourcePickerMode(true);
    }
}
