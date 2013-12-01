package rogue.app.framework.internal.util;

import rogue.app.framework.persistence.MemcachedScoped;
import rogue.app.framework.util.StateHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete implementation of {@link StateHolderManager} that provides access to the following scoped state holders:
 * <ul>
 * <li>Memcached Scope</li>
 * <li>Application Scope</li>
 * <li>Session Scope</li>
 * <li>Request Scope</li>
 * </ul>
 */
@ApplicationScoped
public class DefaultStateHolderManager implements StateHolderManager
{
    private static final String CLASS_NAME = DefaultStateHolderManager.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    @Inject
    @ScopedStateHolderQualifier(ApplicationScoped.class)
    private Instance<StateHolder> applicationScopedMap;

    @Inject
    @ScopedStateHolderQualifier(SessionScoped.class)
    private Instance<StateHolder> sessionScopedMap;

    @Inject
    @ScopedStateHolderQualifier(RequestScoped.class)
    private Instance<StateHolder> requestScopedMap;

    @Inject
    @ScopedStateHolderQualifier(MemcachedScoped.class)
    private Instance<StateHolder> memcachedScopeMap;

    @Override
    public StateHolder getManagedStateHolder(Class<? extends Annotation> clazz)
    {
        if (RequestScoped.class.equals(clazz))
        {
            return requestScopedMap.get();
        }
        else if (SessionScoped.class.equals(clazz))
        {
            return sessionScopedMap.get();
        }
        else if (ApplicationScoped.class.equals(clazz))
        {
            return applicationScopedMap.get();
        }
        else if (MemcachedScoped.class.equals(clazz))
        {
            return memcachedScopeMap.get();
        }
        else
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, "getManagedStateHolder",
                        "No state holder found for scope: " + clazz);
        }
        return null;
    }
}
