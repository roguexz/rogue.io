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

import com.google.appengine.api.datastore.GeoPt;
import org.primefaces.event.map.MarkerDragEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import rogue.app.framework.annotations.ControllerFor;
import rogue.app.framework.i18n.model.Address;
import rogue.app.framework.i18n.model.City;
import rogue.app.framework.i18n.view.faces.bean.CitySelector;
import rogue.app.framework.model.AppObject;
import rogue.app.framework.model.PersistentObject;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.view.faces.EntityConverter;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;
import rogue.app.framework.i18n.view.util.LocationUtils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ControllerFor(name="addresses", model = Address.class)
public class AddressController extends EntityFacesController<Address>
{
    private MapModel mapModel = new DefaultMapModel();
    private Marker marker;
    private boolean showDebugLog;

    private CitySelector citySelector;

    public AddressController()
    {
        super();
    }

    public AddressController(EntityFacesController<? extends PersistentObject> parentController)
    {
        super(parentController);
    }

    public boolean isShowDebugLog()
    {
        return showDebugLog;
    }

    public void setShowDebugLog(boolean showDebugLog)
    {
        this.showDebugLog = showDebugLog;
    }

    @Override
    protected DataModel<Address> constructDataModel()
    {
        Map<String, Object> queryParams = new HashMap<>(1);
        PersistentObject appObject = (PersistentObject) this.getParentController().getSelectedResource();
        queryParams.put("appObjRefKey", appObject != null ? appObject.getId() : null);
        JpaQuery listingQuery = new JpaQuery("AddressEntity.findAssociatedEntries", true, queryParams);
        JpaQuery countQuery = new JpaQuery("AddressEntity.findAssociatedEntries.count", true, queryParams);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);

    }

    @Override
    public Address getNewInstance()
    {
        Address address = super.getNewInstance();
        PersistentObject appObject = (PersistentObject) this.getParentController().getSelectedResource();
        if (appObject != null)
        {
            address.setAssociatedResource(appObject);
            address.setName(appObject.toString());
        }

        return address;
    }

    @Override
    public void setSelectedResource(Address item)
    {
        super.setSelectedResource(item);
        if (item != null)
        {
            getCitySelector().setCity(item.getCity());
        }
        updateMarker();
    }

    @Override
    protected void saveCommandActionListener(ActionEvent event)
    {
        // Lookup the GeoPt and set it not available.
        Address item = getSelectedResource();
        if (item.isDirty())
        {
            String clientId = null;
            if (event.getComponent() != null)
            {
                clientId = event.getComponent().getClientId();
            }
            GeoPt pt = LocationUtils.getCoordinates(item, showDebugLog, clientId);
            // check and see if the pt was manually updated in this request.
            if (pt != null
                    && (item.getBaseResource() == null || Objects.equals(item.getGeoPoint(),
                                                                         item.getBaseResource().getGeoPoint()))
                    && !Objects.equals(item.getGeoPoint(), pt))
            {
                item.setGeoPoint(pt);
            }
        }

        super.saveCommandActionListener(event);

        updateMarker();
    }

    public CitySelector getCitySelector()
    {
        if (citySelector == null)
        {
            citySelector = new CitySelector();
        }
        return citySelector;
    }

    public MapModel getMapModel()
    {
        return mapModel;
    }

    public void refreshMarker(ActionEvent event)
    {
        Address item = getSelectedResource();
        if (item != null && item.isMutable())
        {
            String clientId = null;
            if (event.getComponent() != null)
            {
                clientId = event.getComponent().getClientId();
            }

            GeoPt pt = LocationUtils.getCoordinates(item, showDebugLog, clientId);
            if (pt != null)
            {
                item.setGeoPoint(pt);
                updateMarker();
            }
            else
            {
                FacesMessage message = new FacesMessage("Unable to lookup address.");
                FacesContext ctx = FacesContext.getCurrentInstance();
                if (ctx != null)
                {
                    ctx.addMessage(event.getComponent().getClientId(), message);
                }
            }
        }
    }

    public void deleteMarker(ActionEvent event)
    {
        Address item = getSelectedResource();
        if (item != null && item.isMutable())
        {
            item.setGeoPoint(null);
            updateMarker();
        }
    }

    public void onMarkerDrag(MarkerDragEvent event)
    {
        Marker marker = event.getMarker();
        Address item = getSelectedResource();
        if (item != null && item.isMutable())
        {
            LatLng ll = marker.getLatlng();
            GeoPt pt = new GeoPt((float) ll.getLat(), (float) ll.getLng());
            item.setGeoPoint(pt);
        }
    }

    protected void updateMarker()
    {
        Address item = getSelectedResource();
        if (item != null)
        {
            GeoPt pt = item.getGeoPoint();
            if (pt != null)
            {
                LatLng ll = new LatLng(pt.getLatitude(), pt.getLongitude());
                if (marker == null)
                {
                    marker = new Marker(ll);
                    mapModel.addOverlay(marker);
                }
                else
                {
                    marker.setLatlng(ll);
                }
                marker.setDraggable(item.isMutable());
            }
        }
    }

    @Override
    public void setParentResource(AppObject<?, ?> parentResource)
    {
        super.setParentResource(parentResource);
        if (isSingleSelection())
        {
            // Set the currently selected resource as well.
            Map<String, Object> queryParams = new HashMap<>(1);
            PersistentObject appObject = (PersistentObject) parentResource;
            queryParams.put("appObjRefKey", appObject != null ? appObject.getId() : null);
            JpaQuery pq = new JpaQuery("AddressEntity.findAssociatedEntries", true, queryParams);
            List<Address> results = getDelegate().find(pq);
            if (!results.isEmpty())
            {
                setSelectedResource(results.get(0));
            }
            else
            {
                setSelectedResource(getNewInstance());
            }
        }
    }

    // TODO: Fix this! Rogue your stupid brainz have gone crazy! Smart Ass!
    public List<City> getCities()
    {
        return JpaController.getController(City.class).findEntities(0, -1);
    }

    public EntityConverter<City> getCityConverter()
    {
        return CITY_ENTITY_CONVERTER;
    }

    private static final EntityConverter<City> CITY_ENTITY_CONVERTER = new EntityConverter<>(City.class);
}
