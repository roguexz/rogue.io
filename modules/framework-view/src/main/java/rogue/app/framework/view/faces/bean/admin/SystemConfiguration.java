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

package rogue.app.framework.view.faces.bean.admin;

import com.google.appengine.api.datastore.Blob;
import org.apache.commons.lang.StringUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import rogue.app.framework.model.BinaryResource;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.view.util.AppFunctions;
import rogue.app.framework.view.util.FacesUtils;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * The <code>SystemConfiguration</code> bean helps in setting up the application's configurations.
 */
//@Named
//@ViewScoped
public class SystemConfiguration implements Serializable
{

    private static final String[] configKeys = {
            "site.name", "site.copyright", "ipinfodb.key", "graph.facebook.com.consumer_key",
            "graph.facebook.com.consumer_secret", "graph.facebook.com.custom_permissions", "site.email.id",
            "site.email.display.name", "admin.email.id", "calendar.service.account"
    };

    private static final String[] binaryResourceKeys = {"calendar.service.private.key"};

    private Map<String, String> appProperties;
    private Map<String, BinaryResource> appBinaryResources;

    public SystemConfiguration()
    {
    }

    public Map<String, String> getAppProperties()
    {
        initConfiguration();
        return appProperties;
    }

    public Map<String, BinaryResource> getAppBinaryResources()
    {
        initConfiguration();
        return appBinaryResources;
    }

    public void handleFileUpload(FileUploadEvent event)
    {
        initConfiguration();
        UIInput component = (UIInput) event.getComponent();
        String key = (String) component.getAttributes().get("property.key");
        key = StringUtils.trim(key);

        if (StringUtils.isEmpty(key))
        {
            String message = String.format(
                    "The 'property.key' attribute was not set on the component with id: %s. Ensure that it has been set in the code.",
                    component.getClientId());
            component.setValidatorMessage(message);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
        }

        UploadedFile file = event.getFile();
        BinaryResource resource = appBinaryResources.get(key);
        if (resource == null)
        {
            JpaController controller = JpaController.getController(BinaryResource.class);
            Map<String, Object> queryParams = new HashMap<>(3);
            queryParams.put("queryString", key.toLowerCase());
            queryParams.put("appObjRefKey", null);
            queryParams.put("nameSpace", Attributes.SYSTEM_NAMESPACE);

            resource = (BinaryResource) controller
                    .executeNamedQuerySingleResult("BinaryResourceEntity.findByName", queryParams);

            if (resource == null)
            {
                resource = (BinaryResource) controller.getNewInstance();
                resource.setNameSpace(Attributes.SYSTEM_NAMESPACE);
                resource.setName(key);
            }
            else
            {
                resource = resource.getMutableInstance();
            }

            appBinaryResources.put(key, resource);
        }

        // set the uploaded file's name as the description of the resource
        resource.setDescription(file.getFileName());

        // set the content.
        resource.setBlob(new Blob(file.getContents()));
    }

    public void invokeSaveCommand(AjaxBehaviorEvent event)
    {
        for (Map.Entry<String, String> entry : appProperties.entrySet())
        {
            saveAttribute(entry.getKey(), entry.getValue());
        }

        JpaController controller = JpaController.getController(BinaryResource.class);
        for (BinaryResource resource : appBinaryResources.values())
        {
            if (resource != null && resource.isDirty())
            {
                controller.save(resource);
            }
        }

        FacesUtils.addSuccessMessage("Done.", "Configuration changes saved successfully.");
        appProperties = null;
        appBinaryResources = null;
    }

    private void saveAttribute(String key, String value)
    {
        Map<String, Object> queryParams = new HashMap<>(4);
        queryParams.put("nameSpace", Attributes.SYSTEM_NAMESPACE);
        queryParams.put("appObjRefKey", null);
        queryParams.put("layerKey", null);

        queryParams.put("queryString", key.toLowerCase());
        JpaController<Attribute> controller = JpaController.getController(Attribute.class);
        Attribute attr = controller.executeNamedQuerySingleResult("AttributeEntity.findByName",
                                                                  queryParams);


        if (attr != null)
        {
            attr = attr.getMutableInstance();
        }
        else
        {
            attr = controller.getNewInstance();
            attr.setName(key);
            attr.setNameSpace(Attributes.SYSTEM_NAMESPACE);
        }

        attr.setValue(value);

        if (attr.isDirty())
        {
            controller.save(attr);
        }
    }

    private void initConfiguration()
    {
        if (appProperties == null)
        {
            appProperties = new HashMap<>();
            for (String s : configKeys)
            {
                appProperties.put(s, AppFunctions.getApplicationProperty(s, null));
            }
        }

        if (appBinaryResources == null)
        {
            appBinaryResources = new HashMap<>();

            JpaController controller = JpaController.getController(BinaryResource.class);
            Map<String, Object> queryParams = new HashMap<>(3);
            queryParams.put("appObjRefKey", null);
            queryParams.put("nameSpace", Attributes.SYSTEM_NAMESPACE);

            for (String s : binaryResourceKeys)
            {
                queryParams.put("queryString", s.toLowerCase());
                BinaryResource resource = (BinaryResource) controller
                        .executeNamedQuerySingleResult("BinaryResourceEntity.findByName", queryParams);
                appBinaryResources.put(s, resource);
            }
        }
    }
}
