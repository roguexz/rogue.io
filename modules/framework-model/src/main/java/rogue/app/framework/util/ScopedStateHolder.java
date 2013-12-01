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

package rogue.app.framework.util;

import org.jboss.weld.literal.AnyLiteral;
import rogue.app.framework.internal.util.CDIUtils;
import rogue.app.framework.internal.util.DummyStateHolder;
import rogue.app.framework.internal.util.StateHolderManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper utility to get hold of various scoped {@link StateHolder state holder} instances
 */
public class ScopedStateHolder
{
    private static final String CLASS_NAME = ScopedStateHolder.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static StateHolderManager[] stateHolderManagers;

    private static StateHolder dummyStateHolder = new DummyStateHolder();

    /**
     * Get the {@link StateHolder} instance associated with the given scope.
     *
     * @param scope the class that identifies the scope of the cache map
     * @return the {@link StateHolder} instance if available.
     */
    public static StateHolder getStateHolder(Class<? extends Annotation> scope)
    {
        final String METHOD_NAME = "getStateHolder";
        if (stateHolderManagers == null)
        {
            BeanManager beanManager = CDI.current().getBeanManager();

            List<StateHolderManager> managers =
                    CDIUtils.lookupBeans(StateHolderManager.class, ApplicationScoped.class, AnyLiteral.INSTANCE);
            stateHolderManagers = managers.toArray(new StateHolderManager[managers.size()]);
        }

        StateHolder holder = null;
        for (StateHolderManager manager : stateHolderManagers)
        {
            holder = manager.getManagedStateHolder(scope);
            if (holder != null)
            {
                break;
            }
        }

        if (holder == null)
        {
            LOGGER.logp(Level.INFO, CLASS_NAME, METHOD_NAME,
                        "Unable to get requested state holder, returning dummy holder instead");
            holder = dummyStateHolder;
        }

        return holder;
    }

}
