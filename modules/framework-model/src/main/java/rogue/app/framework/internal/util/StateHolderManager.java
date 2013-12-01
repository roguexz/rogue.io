package rogue.app.framework.internal.util;

import rogue.app.framework.util.StateHolder;

import java.lang.annotation.Annotation;

/**
 * Due to the restriction in CDI that a bean can be cast to an interface and should not be cast to the concrete type,
 * we need a separate interface definition. Keeping this logic internal only as it doesn't seem appropriate to be in
 * public package.
 */
public interface StateHolderManager
{
    /**
     * Get the container managed state holder for the given scope.
     *
     * @param clazz the scope of the state holder
     * @return an instance of the managed state holder, or null if nothing was found.
     */
    public StateHolder getManagedStateHolder(Class<? extends Annotation> clazz);
}
