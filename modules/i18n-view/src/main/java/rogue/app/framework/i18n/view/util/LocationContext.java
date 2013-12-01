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

package rogue.app.framework.i18n.view.util;

import com.google.appengine.api.datastore.GeoPt;
import org.apache.commons.lang.StringUtils;
import rogue.app.framework.i18n.model.City;
import rogue.app.framework.i18n.model.Country;
import rogue.app.framework.i18n.model.State;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.PersistenceUtils;
import rogue.app.framework.util.ScopedStateHolder;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Information of the current user's location.
 */
@Named("locationCtx")
@SessionScoped
public class LocationContext implements Serializable
{
    private static final String CLASS_NAME = LocationContext.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);
    private static final String REQ_PARAM_LATITUDE = "latitude";
    private static final String REQ_PARAM_LONGITUDE = "longitude";
    private static final String LAT_LNG_REQUEST_FAILED = "LAT_LNG_REQUEST_FAILED";

    private boolean initialized;
    private Country country;
    private State state;
    private City city;
    private GeoPt geoPoint;

    /**
     * Default public constructor
     */
    public LocationContext()
    {
    }

    /**
     * API mechanism to fetch the location context instance from the session scope.
     *
     * @return an instance of the LocationContext
     */
    public static LocationContext getInstance()
    {
        BeanManager manager = CDI.current().getBeanManager();
        if (manager != null)
        {
            Named n = LocationContext.class.getAnnotation(Named.class);
            Set<Bean<?>> beans = manager.getBeans(n.value());
            if (!beans.isEmpty())
            {
                beans.iterator().hasNext();
                return (LocationContext) beans.iterator().next();
            }
        }

        return null;
    }

    public Country getCountry()
    {
        initContext();
        return country;
    }

    public void setCountry(Country country)
    {
        initContext();
        this.country = country;
    }

    public State getState()
    {
        initContext();
        return state;
    }

    public void setState(State state)
    {
        initContext();
        this.state = state;
    }

    public City getCity()
    {
        initContext();
        return city;
    }

    public void setCity(City city)
    {
        initContext();
        this.city = city;
    }

    public GeoPt getGeoPoint()
    {
        initContext();
        return geoPoint;
    }

    public void setGeoPoint(GeoPt geoPoint)
    {
        initContext();
        this.geoPoint = geoPoint;
    }

    public void loadCityFromLatLong(ActionEvent event)
    {
        final String METHOD_NAME = "loadCityFromLatLong";
        Map<String, String> reqMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        try
        {
            float latitude = Float.parseFloat(reqMap.get(REQ_PARAM_LATITUDE));
            float longitude = Float.parseFloat(reqMap.get(REQ_PARAM_LONGITUDE));
            initGeoRegions(LocationUtils.getLocationInformationFromLatLng(latitude, longitude));

            if (city == null)
            {
                doNotAccessClientSideGPS(event);
            }
        }
        catch (Exception ex)
        {
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Failed to parse lat long coordinates: " + ex.getMessage(),
                        ex);
        }
    }

    public void doNotAccessClientSideGPS(ActionEvent event)
    {
        ScopedStateHolder.getStateHolder(SessionScoped.class).put(LAT_LNG_REQUEST_FAILED, true);
    }


    private void initContext()
    {
        if (!initialized)
        {
            Map<String, String> locationInformation = LocationUtils.getLocationInformationFromHeaders(null);
            initGeoRegions(locationInformation);

            // check if we have a valid city or not.
            if (city == null)
            {
                // repeat operation by fetching data from the IP INFO DB
                locationInformation = LocationUtils.getLocationInformationFromIP(null);
                initGeoRegions(locationInformation);
            }
            initialized = true;
        }
    }

    private void initGeoRegions(Map<String, String> locationInformation)
    {
        initCountry(locationInformation.get(LocationUtils.COUNTRY_CODE));
        boolean isCode = locationInformation.containsKey(LocationUtils.REGION_CODE);
        initState(locationInformation.get(isCode ? LocationUtils.REGION_CODE : LocationUtils.REGION_NAME), isCode);
        initCity(locationInformation.get(LocationUtils.CITY_NAME));
    }

    private void initCountry(String countryCode)
    {
        final String METHOD_NAME = "initCountry";
        if (!StringUtils.isEmpty(countryCode))
        {
            try
            {
                Map<String, Object> params = new HashMap<>(1);
                params.put("queryString", countryCode.toLowerCase());
                country = JpaController.getController(Country.class)
                                       .executeNamedQuerySingleResult("CountryEntity.findByCode", params);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to look up country.", e);
            }

        }

        if (country == null && LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to lookup country for code: " + countryCode);

        }
    }

    private void initState(String regionString, boolean isCode)
    {
        final String METHOD_NAME = "initState";

        if (!StringUtils.isEmpty(regionString))
        {
            try
            {
                Map<String, Object> params = new HashMap<>(2);
                params.put("parentKey", PersistenceUtils.getEntityKey(country));
                params.put("queryString", regionString.toLowerCase());

                state = JpaController.getController(State.class)
                                     .executeNamedQuerySingleResult(
                                             isCode ? "StateEntity.findByCode" : "StateEntity.findByName", params);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to look up state.", e);
            }
        }

        if (state == null && LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to lookup state: " + regionString);

        }
    }

    private void initCity(String cityName)
    {
        final String METHOD_NAME = "initCity";

        if (!StringUtils.isEmpty(cityName))
        {
            try
            {
                Map<String, Object> params = new HashMap<>(2);
                params.put("parentKey", PersistenceUtils.getEntityKey(state));
                params.put("queryString", cityName.toLowerCase());
                city = JpaController.getController(City.class)
                                    .executeNamedQuerySingleResult("CityEntity.findByName", params);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to lookup city.", e);
            }
        }

        if (city == null && LOGGER.isLoggable(Level.FINE))
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to lookup city with name: " + cityName);
        }
    }

    private void initCityLatLng(String value)
    {
        final String METHOD_NAME = "initCity";
        if (!StringUtils.isEmpty(value))
        {
            String[] tmp = value.split(",");
            if (tmp.length == 2)
            {
                try
                {
                    float latitude = Float.parseFloat(tmp[0]);
                    float longitude = Float.parseFloat(tmp[1]);
                    geoPoint = new GeoPt(latitude, longitude);
                }
                catch (NumberFormatException e)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to parse lat-long.", e);
                }
            }
        }
    }
}
