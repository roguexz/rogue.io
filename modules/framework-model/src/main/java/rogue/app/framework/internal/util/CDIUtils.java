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

package rogue.app.framework.internal.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for performing CDI operations.
 */
public final class CDIUtils
{
    private static final String CLASS_NAME = CDIUtils.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private CDIUtils()
    {
    }

    public static <T> T lookupBean(Class<T> beanClazz, Class<?> scope, Annotation... qualifiers)
    {
        final String METHOD_NAME = "lookupBean";
        T instance = null;
        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(beanClazz, qualifiers);
        for (Bean<?> bean : beans)
        {
            if (beanClazz.isAssignableFrom(bean.getBeanClass()) && scope.equals(bean.getScope()))
            {
                CreationalContext ctx = beanManager.createCreationalContext(bean);
                instance = (T) beanManager.getReference(bean, beanClazz, ctx);
                break;
            }
            else
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Bean not set at requested scope("
                        + scope.getName() + "). Ignoring definition: " + bean.getBeanClass());
            }

        }
        return instance;
    }

    public static <T> List<T> lookupBeans(Class<T> beanClazz, Class<?> scope, Annotation... qualifiers)
    {
        final String METHOD_NAME = "lookupBeans";
        List<T> instances = new ArrayList<>(1);
        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(beanClazz, qualifiers);
        for (Bean<?> bean : beans)
        {
            if (beanClazz.isAssignableFrom(bean.getBeanClass()) && scope.equals(bean.getScope()))
            {
                CreationalContext ctx = beanManager.createCreationalContext(bean);
                instances.add((T) beanManager.getReference(bean, beanClazz, ctx));
            }
            else
            {
                LOGGER.logp(Level.FINE, CLASS_NAME, METHOD_NAME, "Bean not set at requested scope("
                        + scope.getName() + "). Ignoring definition: " + bean.getBeanClass());
            }
        }

        return instances;
    }
}
