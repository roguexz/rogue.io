package rogue.app.framework.model;

import javax.inject.Named;

/**
 * Identifies objects that expose a collection of {@link AppObjectController} instances. This interface is typically
 * used by beans that wish to provide access to pre-configured controllers. An example would be a bean that
 * exposes controllers for performing admin operations or a bean that is to be used on the site. This interface is also
 * used by the URI support module to help resolve the beans that are associated with a predefined prefix.
 */
public interface AppObjectControllerCollection
{
    /**
     * Get the name of the collection. Implementations can choose how they wish to surface this information. It can be
     * either via the {@link Named} annotation or other means.
     *
     * @return the name of the collection.
     */
    public String getCollectionName();

    /**
     * Get a controller identified by the given name.
     *
     * @param name the name of the controller.
     * @return a controller identified by the given name, or null if no such controller is associated with this bean.
     */
    public AppObjectController<?, ?> getController(String name);
}
