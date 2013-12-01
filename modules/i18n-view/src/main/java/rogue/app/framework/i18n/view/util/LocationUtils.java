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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import rogue.app.framework.i18n.model.*;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.view.util.AppFunctions;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LocationUtils
{
    public static final String COUNTRY_CODE = "countryCode";
    public static final String COUNTRY_NAME = "countryName";
    public static final String REGION_CODE = "regionCode";
    public static final String REGION_NAME = "regionName";
    public static final String CITY_NAME = "cityName";
    public static final String LAT_LONG = "latitudeLongitude";

    private static final String CLASS_NAME = LocationUtils.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    // Headers defined by the Google App Engine request.
    private static final String X_APPENGINE_COUNTRY = "X-AppEngine-Country";
    private static final String X_APPENGINE_REGION = "X-AppEngine-Region";
    private static final String X_APPENGINE_CITY = "X-AppEngine-City";
    private static final String X_APPENGINE_CITYLATLONG = "X-AppEngine-CityLatLong";

    private static final String IPINFODB_KEY = "ipinfodb.key";
    private static final String IPINFODB_URL =
            "http://api.ipinfodb.com/v3/ip-city/?key=%s&ip=%s&format=json";

    private static final String GMAPS_REVERSE_LOOKUP_URL =
            "https://maps.googleapis.com/maps/api/geocode/json?sensor=false&latlng=%f,%f";
    private static final String GMAPS_LOOKUP_URL =
            "https://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=%s";

    private LocationUtils()
    {
    }

    /**
     * Get the location information based on the request headers set by Google App Engine.
     *
     * @param request (optional) the request object to use if the Faces environment has not been set.
     * @return the JSON response parsed as a map.
     */
    public static Map<String, String> getLocationInformationFromHeaders(HttpServletRequest request)
    {
        Map<String, String> locationInfo = new HashMap<>(4);
        if (request != null)
        {
            locationInfo.put(COUNTRY_CODE, request.getHeader(X_APPENGINE_COUNTRY));
            locationInfo.put(REGION_CODE, request.getHeader(X_APPENGINE_REGION));
            locationInfo.put(CITY_NAME, request.getHeader(X_APPENGINE_CITY));
            locationInfo.put(LAT_LONG, request.getHeader(X_APPENGINE_CITYLATLONG));
        }
        else
        {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null)
            {
                Map<String, String> headers = context.getExternalContext().getRequestHeaderMap();
                locationInfo.put(COUNTRY_CODE, headers.get(X_APPENGINE_COUNTRY));
                locationInfo.put(REGION_CODE, headers.get(X_APPENGINE_REGION));
                locationInfo.put(CITY_NAME, headers.get(X_APPENGINE_CITY));
                locationInfo.put(LAT_LONG, headers.get(X_APPENGINE_CITYLATLONG));
            }
        }
        return locationInfo;
    }


    /**
     * Get the location information based on the ipAddress using IPInfoDB service.
     *
     * @param request (optional) the request object to use if the Faces environment has not been set.
     * @return the location information
     */
    public static Map<String, String> getLocationInformationFromIP(HttpServletRequest request)
    {
        final String METHOD_NAME = "getLocationInformationFromIP";
        String ipAddress = null;

        if (request != null)
        {
            ipAddress = request.getRemoteAddr();
        }
        else
        {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null)
            {
                ipAddress = ((HttpServletRequest) context.getExternalContext().getRequest()).getRemoteAddr();
            }
        }

        Map<String, String> locationInfo = new HashMap<>();
        if (ipAddress != null)
        {
            try
            {
                LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Attempting to resolve location for IP " + ipAddress);
                // Verify that the ip address is proper
                InetAddress address = InetAddress.getByName(ipAddress);
                if (!address.isLoopbackAddress())
                {
                    String key = AppFunctions.getApplicationProperty(IPINFODB_KEY, null);
                    if (key == null)
                    {
                        LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "IPINFODB key missing.");
                    }
                    else
                    {
                        String content = fetchContent(String.format(IPINFODB_URL, key, ipAddress));
                        JSONObject obj = new JSONObject(content);
                        locationInfo.put(COUNTRY_CODE, obj.getString("countryCode"));
                        locationInfo.put(COUNTRY_NAME, obj.getString("countryName"));
                        locationInfo.put(REGION_NAME, obj.getString("regionName"));
                        locationInfo.put(CITY_NAME, obj.getString("cityName"));
                        locationInfo.put(LAT_LONG, obj.getString("latitude") + "," + obj.getString("longitude"));
                    }
                }
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to load city based on IP", e);
            }
        }
        return locationInfo;
    }

    public static Map<String, String> getLocationInformationFromLatLng(float latitude, float longitude)
    {
        final String METHOD_NAME = "getLocationInformationFromLatLng";
        Map<String, String> locationInfo = new HashMap<>();
        try
        {
            String content = fetchContent(String.format(GMAPS_REVERSE_LOOKUP_URL, latitude, longitude));
            JSONObject jsonObject = new JSONObject(content);
            JSONArray array = jsonObject.getJSONArray("results");
            if (array != null)
            {
                for (int i = 0; i < array.length(); i++)
                {
                    jsonObject = array.getJSONObject(i);
                    JSONArray addressComponents = jsonObject.getJSONArray("address_components");
                    for (int j = 0; j < addressComponents.length(); j++)
                    {
                        jsonObject = addressComponents.getJSONObject(j);
                        if (jsonObject != null)
                        {
                            JSONArray typesArray = jsonObject.getJSONArray("types");
                            if (typesArray != null)
                            {
                                String t = typesArray.getString(0);

                                if ("country".equals(t))
                                {
                                    locationInfo.put(COUNTRY_CODE, jsonObject.getString("short_name"));
                                    locationInfo.put(COUNTRY_NAME, jsonObject.getString("long_name"));
                                }
                                else if ("administrative_area_level_1".equals(t))
                                {
                                    locationInfo.put(REGION_CODE, jsonObject.getString("short_name"));
                                    locationInfo.put(REGION_NAME, jsonObject.getString("long_name"));
                                }
                                if ("locality".equals(t))
                                {
                                    locationInfo.put(CITY_NAME, jsonObject.getString("long_name"));
                                }
                            }
                        }
                    }

                    // break if the city has been found.
                    if (locationInfo.containsKey(CITY_NAME))
                    {
                        break;
                    }
                }
            }

        }
        catch (Exception e)
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to load city based on LatLong", e);
        }

        return locationInfo;
    }

    public static GeoPt getCoordinates(Address address, boolean logMessage, String clientId)
    {
        final String METHOD_NAME = "getCoordinates";
        GeoPt pt = null;
        if (address != null)
        {
            StringBuilder builder = new StringBuilder();
            if (!StringUtils.isEmpty(address.getAddress1()))
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }
                builder.append(address.getAddress1());
            }

            if (!StringUtils.isEmpty(address.getAddress2()))
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }
                builder.append(address.getAddress2());
            }

            if (!StringUtils.isEmpty(address.getLocality()))
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }
                builder.append(address.getLocality());
            }

            City city = address.getCity();

            if (city != null && !StringUtils.isEmpty(city.getName()))
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }
                builder.append(address.getCity().getName());

                State state = city.getState();
                if (state != null && !StringUtils.isEmpty(state.getName()))
                {
                    if (builder.length() != 0)
                    {
                        builder.append(", ");
                    }
                    builder.append(state.getName());


                    Country country = state.getCountry();
                    if (country != null && !StringUtils.isEmpty(country.getName()))
                    {
                        if (builder.length() != 0)
                        {
                            builder.append(", ");
                        }
                        builder.append(country.getName());
                    }
                }
            }

            if (!StringUtils.isEmpty(address.getPostalCode()))
            {
                if (builder.length() != 0)
                {
                    builder.append(", ");
                }
                builder.append(address.getPostalCode());
            }

            String addr = builder.toString().replaceAll(" ", "+");

            addr = StringEscapeUtils.escapeHtml(addr);

            try
            {
                String content = fetchContent(String.format(GMAPS_LOOKUP_URL, addr));
                JSONObject jsonObject = new JSONObject(content);

                String debugLog = String.format(GMAPS_LOOKUP_URL, addr) + "\n" + jsonObject.toString();
                if (logMessage)
                {
                    LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME, debugLog);
                    FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(debugLog));
                }

                JSONArray array = jsonObject.getJSONArray("results");
                if (array != null)
                {
                    for (int i = 0; i < array.length(); i++)
                    {
                        jsonObject = array.getJSONObject(i);

                        boolean zipCodeMatched = false;

                        JSONArray addressComponents = jsonObject.getJSONArray("address_components");
                        if (addressComponents != null)
                        {
                            for (int j = 0; j < addressComponents.length(); j++)
                            {
                                JSONObject addressObject = addressComponents.getJSONObject(j);
                                if (addressObject != null)
                                {
                                    JSONArray typesArray = addressObject.getJSONArray("types");
                                    if (typesArray != null)
                                    {
                                        String t = typesArray.getString(0);
                                        if ("postal_code".equals(t))
                                        {
                                            // Check if the returned address has the same zip code as that
                                            if (Objects.equals(address.getPostalCode(),
                                                               addressObject.getString("long_name")))
                                            {
                                                zipCodeMatched = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (zipCodeMatched)
                        {
                            JSONObject geometry = jsonObject.getJSONObject("geometry");
                            if (geometry != null)
                            {
                                JSONObject location = geometry.getJSONObject("location");
                                if (location != null)
                                {
                                    Double lat = location.getDouble("lat");
                                    Double lng = location.getDouble("lng");
                                    if (lat != null && lng != null)
                                    {
                                        pt = new GeoPt(lat.floatValue(), lng.floatValue());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to lookup address", e);
            }

            if (pt == null)
            {
                // Lookup based on the zip code.
                pt = getCoordinates(Long.parseLong(address.getPostalCode()));
            }

        }
        return pt;
    }

    /**
     * Get the LatLong information for a given zip code.
     *
     * @param zipCode the zip code
     * @return the LatLng information if the zip code is available, null other wise.
     */
    public static GeoPt getCoordinates(long zipCode)
    {
        Map<String, Object> params = new HashMap<>(1);
        params.put("queryString", zipCode);
        GeoCode gc = JpaController.getController(GeoCode.class)
                                  .executeNamedQuerySingleResult("GeoCodeEntity.findByZipCode", params);
        GeoPt pt = null;
        if (gc != null)
        {
            pt = new GeoPt(gc.getLatitude(), gc.getLongitude());
        }
        return pt;
    }

    /**
     * Get the great-circle distance between two geo-points.
     */
    public static double getHaversineDistance(GeoPt pt1, GeoPt pt2)
    {
        double lat1 = pt1.getLatitude();
        double lat2 = pt2.getLatitude();
        double lon1 = pt1.getLongitude();
        double lon2 = pt2.getLongitude();
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return 6371 * c;
    }

    /**
     * Fetch the content of a target URL as a string.
     *
     * @param url the target URL to fetch.
     * @return content as a string
     * @throws IOException if accessing the URL fails
     */
    private static String fetchContent(String url) throws IOException
    {
        final String METHOD_NAME = "fetchContent";
        URL u = new URL(url);
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder(100);
        String line;

        try
        {
            reader = new BufferedReader(new InputStreamReader(u.openStream()));
            while ((line = reader.readLine()) != null)
            {
                builder.append(line).append("\n");
            }
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to close stream", e);
                }
            }
        }
        return builder.toString();
    }
}
