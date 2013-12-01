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

package rogue.app.framework.view.faces.application;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.sun.faces.util.Util;
import rogue.app.framework.AppRuntimeException;
import rogue.app.framework.model.BinaryResource;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.view.faces.controller.BinaryResourcesController;

import javax.faces.application.ProjectStage;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an Image as a JSF resource.
 */
public class AppImageResource extends Resource
{
    /**
     * The name of the library managed by this resource handler.
     */
    public static final String LIBRARY_NAME = "app";

    /**
     * An instance for using the convenience methods to load up the resource.
     */
    private static final BinaryResourcesController BINARY_RESOURCES_CONTROLLER = new BinaryResourcesController();

    private static final String CLASS_NAME = AppImageResource.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    /* HTTP Date format required by the HTTP/1.1 RFC */
    private static final String RFC1123_DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    /**
     * Lifespan of this resource for caching purposes.
     */
    private static final long MAX_AGE = 24 * 60 * 60 * 1000;

    /*
     * Response headers that need to be added by the ResourceManager
     * implementation.
     */
    private transient Map<String, String> responseHeaders;
    private String imageKey;
    private BinaryResource imageResource;

    public AppImageResource(String imageKey)
    {
        super();
        this.imageKey = imageKey;
        setLibraryName(LIBRARY_NAME);
        setResourceName(imageKey);
        initImageResource();
        if (imageResource != null)
        {
            setContentType(imageResource.getMimeType());
        }
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        initImageResource();
        if (imageResource != null)
        {
            if (imageResource.getBlob() != null)
            {
                return new ByteArrayInputStream(imageResource.getBlob().getBytes());
            }
        }
        throw new IOException("Unable to fetch image content.");
    }

    @Override
    public String getContentType()
    {
        initImageResource();
        return imageResource != null ? imageResource.getMimeType() : "image";
    }

    @Override
    public URL getURL()
    {
        return null;
    }


    @Override
    public Map<String, String> getResponseHeaders()
    {
        if (imageResource != null && isResourceRequest())
        {
            if (responseHeaders == null)
            {
                responseHeaders = new HashMap<>(6, 1.0f);
            }

            long expiresTime;
            FacesContext ctx = FacesContext.getCurrentInstance();

            if (ctx.isProjectStage(ProjectStage.Development))
            {
                expiresTime = new Date().getTime();
            }
            else
            {
                expiresTime = new Date().getTime() + MAX_AGE;
            }
            SimpleDateFormat format =
                    new SimpleDateFormat(RFC1123_DATE_PATTERN, Locale.US);
            format.setTimeZone(GMT);
            responseHeaders.put("Expires", format.format(new Date(expiresTime)));


            Date lastModified = getLastModified();
            responseHeaders.put("Last-Modified", format.format(lastModified));
            long contentLength = imageResource.getBlob().getBytes().length;

            responseHeaders.put("ETag", "W/\"" + contentLength + '-' + lastModified + '"');
            return responseHeaders;
        }
        else
        {
            return Collections.emptyMap();
        }

    }

    @Override
    public String getRequestPath()
    {

        String uri;
        FacesContext context = FacesContext.getCurrentInstance();
        String facesServletMapping = Util.getFacesMapping(context);
        // If it is extension mapped
        if (Util.isPrefixMapped(facesServletMapping))
        {
            uri = facesServletMapping + ResourceHandler.RESOURCE_IDENTIFIER + '/' +
                    getResourceName();
        }
        else
        {
            uri = ResourceHandler.RESOURCE_IDENTIFIER + '/' + getResourceName() +
                    facesServletMapping;
        }
        boolean queryStarted = false;
        if (null != getLibraryName())
        {
            queryStarted = true;
            uri += "?ln=" + getLibraryName();
        }

        if (imageResource != null && imageResource.isMutable())
        {
            if (queryStarted)
            {
                uri += "&";
            }
            else
            {
                uri += "?";
                queryStarted = true;
            }
            uri += "_t=" + new Date().getTime();
        }

        uri = context.getApplication().getViewHandler().getResourceURL(context, uri);

        return uri;

    }


    /**
     * @see javax.faces.application.Resource#userAgentNeedsUpdate(javax.faces.context.FacesContext)
     */
    public boolean userAgentNeedsUpdate(FacesContext context)
    {

        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
        // 14.25 If-Modified-Since

        // if the requested variant has not been modified since the time
        // specified in this field, an entity will not be returned from the
        // server; instead, a 304 (not modified) response will be returned
        // without any message-body.

        // A date which is later than the server's current time is
        // invalid.

        Map<String, String> requestHeaders =
                context.getExternalContext().getRequestHeaderMap();

        if (requestHeaders.containsKey(IF_MODIFIED_SINCE))
        {
            long lastModifiedOfResource = getLastModified().getTime();
            long lastModifiedHeader = getIfModifiedHeader(context.getExternalContext());
            return lastModifiedOfResource > lastModifiedHeader;
        }
        return true;

    }


    // --------------------------------------------------------- Private Methods


    /*
     * This method should only be called if the 'If-Modified-Since' header
     * is present in the request header map.
     */
    private long getIfModifiedHeader(ExternalContext externalContext)
    {
        final String METHOD_NAME = "getIfModifiedHeader";
        Object request = externalContext.getRequest();
        if (request instanceof HttpServletRequest)
        {
            // try to use the container where we can.  V3 for instance
            // has a FastHttpDateFormat format/parse implementation
            // which is more than likely more performant than SimpleDateFormat
            // (otherwise, why would it be there?).
            return ((HttpServletRequest) request).getDateHeader(IF_MODIFIED_SINCE);
        }
        else
        {
            SimpleDateFormat format =
                    new SimpleDateFormat(RFC1123_DATE_PATTERN, Locale.US);
            try
            {
                Date ifModifiedSinceDate = format.parse(externalContext.getRequestHeaderMap().get(IF_MODIFIED_SINCE));
                return ifModifiedSinceDate.getTime();
            }
            catch (ParseException ex)
            {
                if (LOGGER.isLoggable(Level.WARNING))
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                "jsf.application.resource.invalid_if_modified_since_header",
                                new Object[]{
                                        externalContext.getRequestHeaderMap().get(IF_MODIFIED_SINCE)
                                });
                    if (ex != null)
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "", ex);
                    }
                }
                return -1;
            }
        }

    }

    private boolean isResourceRequest()
    {

        FacesContext ctx = FacesContext.getCurrentInstance();
        return (ctx.getApplication().getResourceHandler().isResourceRequest(ctx));

    }

    private void initImageResource()
    {
        final String METHOD_NAME = "initImageResource";
        // Check if we have a valid key or not
        if (imageKey != null)
        {
            try
            {
                Key key = BINARY_RESOURCES_CONTROLLER.parseId(imageKey);
                if (key != null)
                {
                    imageResource = BINARY_RESOURCES_CONTROLLER.findById(key);
                }
            }
            catch (AppRuntimeException e)
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Unable to load resource.", e);
            }
        }

        if (imageResource == null)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to fetch data for image with id: " + imageKey);
        }
    }

    private Date getLastModified()
    {
        final String METHOD_NAME = "getLastModified";
        Date date = null;

        try
        {
            Key key = KeyFactory.stringToKey(getResourceName());
            if (key != null)
            {
                JpaController controller = JpaController.getController(BinaryResource.class);
                BinaryResource resource = (BinaryResource) controller.findById(key);
                if (resource != null)
                {
                    date = resource.getLastModificationDate();
                    if (date == null)
                    {
                        date = resource.getCreationDate();
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Failed to fetch modification date for resource.", e);
        }

        if (date == null)
        {
            date = Calendar.getInstance().getTime();
        }

        return date;
    }

}
