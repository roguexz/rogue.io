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

package rogue.app.framework.persistence;

import javax.enterprise.context.NormalScope;
import javax.enterprise.context.RequestScoped;
import java.lang.annotation.*;

/**
 * Associated with a <code>PersistentEntity</code> implementation, this annotation tells the <code>JpaController</code>
 * that the data fetched using named queries / ids can be cached at the scope mentioned.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable
{
    /**
     * The scope at which the object should ideally be cached. The value should be an annotation class that represents a
     * {@link NormalScope}. If the value does not represent a NormalScope, the framework is free to choose an
     * appropriate default value.
     *
     * @return the scope at which the object should be cached.
     */
    Class<? extends Annotation> preferredScope() default RequestScoped.class;
}
