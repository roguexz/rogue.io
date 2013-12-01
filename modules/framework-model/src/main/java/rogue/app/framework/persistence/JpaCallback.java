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

import rogue.app.framework.AppRuntimeException;

import javax.persistence.EntityManager;

/**
 * Callback definition for performing JPA queries. The callback implementations can concentrate purely on the business
 * logic (actual query) instead of having to worry about the scaffolding required around the operation.
 */
public interface JpaCallback<T>
{
    /**
     * The method invoked to execute the JPA query.
     *
     * @param entityManager the entity manager to leverage
     * @param params        parameters passed to the executor
     * @return the result of the execution, or null.
     * @throws AppRuntimeException if the operation fails.
     */
    public T execute(EntityManager entityManager, Object... params) throws AppRuntimeException;
}
