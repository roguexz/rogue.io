package rogue.app.framework.view.urisupport.faces.application;

import rogue.app.framework.view.urisupport.URIAction;
import rogue.app.framework.view.urisupport.URIResolver;
import rogue.app.framework.view.urisupport.internal.URIUtil;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ViewResource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A resource handler implementation that provides support for loading resources from META-INF/resources folder in
 * servlet containers that do not support resource loading as yet (pre-3.0)
 */
public class URIResourceHandler extends ResourceHandlerWrapper
{
    private ResourceHandler wrapped;
    private String resourcesRoot;
    private Map<String, URL> urlCache = new HashMap<>();

    public URIResourceHandler(ResourceHandler wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ResourceHandler getWrapped()
    {
        return wrapped;
    }

    @Override
    public ViewResource createViewResource(FacesContext context, String resourceName)
    {
        if (!"/".equals(resourceName) && !resourceName.endsWith(URIUtil.getFaceletSuffix()))
        {
            // Verify if the resource name represents a URIAction or not.
            URIAction action = URIResolver.getInstance().resolveURI(resourceName);
            if (action != null)
            {
                // The URIViewHandler should have setup a page to render.
                String pageToRender = (String) context.getExternalContext().getRequestMap()
                                                      .get(URIViewHandler.FRAMEWORK_PAGE_TO_RENDER);
                if (pageToRender != null)
                {
                    resourceName = pageToRender;
                }
            }
        }

        ViewResource resource = super.createViewResource(context, resourceName);
        if (resource == null)
        {
            // Check to see if the resource is available under the META-INF/resources folder or not.
            resource = searchViewResource(context, resourceName);
        }
        return resource;
    }

    @Override
    public Resource createResource(String resourceName)
    {
        return getWrappedResource(super.createResource(resourceName));
    }

    @Override
    public Resource createResource(String resourceName, String libraryName)
    {
        return getWrappedResource(super.createResource(resourceName, libraryName));
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType)
    {
        return getWrappedResource(super.createResource(resourceName, libraryName, contentType));
    }

    @Override
    public Resource createResourceFromId(String resourceId)
    {
        return getWrappedResource(super.createResourceFromId(resourceId));
    }

    /**
     * If the given resource object can be rendered locally, then do so by returning a wrapped object, otherwise
     * return the input as is.
     *
     * @param resource - the input resource object.
     * @return a wrapped resource object if given input can be rendered locally instead.
     */
    private Resource getWrappedResource(Resource resource)
    {
        WebAppResource webAppResource = null;
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        initResourcesRoot(context);

        if (resource != null)
        {
            URL baseURL = resource.getURL();
            if (baseURL != null)
            {
                String extForm = baseURL.toExternalForm();
                int idx = extForm.indexOf(resourcesRoot);
                if (idx != -1)
                {
                    try
                    {
                        extForm = extForm.substring(idx);
                        URL resourceURL = context.getResource(extForm);
                        if (resourceURL != null)
                        {
                            webAppResource = new WebAppResource(extForm, resource);
                        }
                    }
                    catch (MalformedURLException e)
                    {
                        // Do nothing.
                    }
                }
            }
        }
        return webAppResource != null ? webAppResource : resource;
    }

    /**
     * Search for a view resource that can be present inside the META-INF/resources folder as well.
     *
     * @param context      the current FacesContext instance.
     * @param resourceName the viewId to look for
     * @return the ViewResource if available.
     */
    private ViewResource searchViewResource(FacesContext context, String resourceName)
    {
        ViewResource resource = null;

        // Check if the resource is available within the META-INF locations
        if (!resourceName.startsWith("/"))
        {
            resourceName = "/" + resourceName;
        }
        resourceName = "META-INF/resources" + resourceName;

        URL url = urlCache.get(resourceName);
        if (url == null && !urlCache.containsKey(resourceName))
        {
            url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
            urlCache.put(resourceName, url);
        }

        if (url != null)
        {
            resource = new URIViewResource(url);
        }

        return resource;
    }

    /**
     * Initialize the location of the webapp resources folder.
     *
     * @param context the current instance of ExternalContext.
     */
    private void initResourcesRoot(ExternalContext context)
    {
        if (resourcesRoot == null)
        {
            resourcesRoot = context.getInitParameter(ResourceHandler.WEBAPP_RESOURCES_DIRECTORY_PARAM_NAME);
            if (resourcesRoot == null)
            {
                resourcesRoot = "/resources";
            }
        }
    }
}
