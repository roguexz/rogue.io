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

package rogue.app.framework.model;

import rogue.app.framework.AppRuntimeException;

import java.io.Serializable;
import java.util.List;

/**
 * An <code>AppObjectCollection</code> represents a collection of <code>AppObject</code>s and provides methods to
 * manipulate them. Think of this interface as a generic definition for classes like SomeObjectManager /
 * SomeObjectController / etc.
 *
 * @param <T> the application object type
 * @param <K> the type of the key used by the application -- because generics isn't that powerful yet.
 */
public interface AppObjectCollection<T extends AppObject<T, K>, K> extends Serializable
{
    /**
     * Returns a new application object instance.
     *
     * @return a new application object instance.
     * @throws AppRuntimeException if the operation fails.
     */
    public T getNewInstance() throws AppRuntimeException;

    /**
     * Returns a new application object instance that is further initialized with the given parent object.
     *
     * @param parent the parent object
     * @return a new application object instance.
     * @throws AppRuntimeException if the operation fails.
     */
    public T getNewInstance(AppObject<?, ?> parent) throws AppRuntimeException;

    /**
     * Returns a mutable instance of the given application object.
     *
     * @param appObject the application object whose mutable instance is requested.
     * @return a mutable instance of the given argument
     * @throws AppRuntimeException if the operation fails.
     */
    public T getMutableInstance(T appObject) throws AppRuntimeException;

    /**
     * Returns a new query instance.
     *
     * @return a new query instance.
     * @throws AppRuntimeException if the operation fails.
     */
    public Query newQueryInstance() throws AppRuntimeException;

    /**
     * Find an application object, given its "id".
     *
     * @param id the object's identifier
     * @return the application object or null if no such object was found.
     * @throws AppRuntimeException if the operation fails.
     */
    public T findById(K id) throws AppRuntimeException;

    /**
     * Attempts to parse the given string and return a possible ID object.
     *
     * @param idAsString the string representation of the ID object.
     * @return a possible ID object associated with this controller.
     * @throws AppRuntimeException if the operation fails.
     */
    public K parseId(String idAsString) throws AppRuntimeException;

    /**
     * Find a list of application objects that match the given query criteria.
     *
     * @param query the query criteria.
     * @return a list of application objects that match the given query criteria.
     * @throws AppRuntimeException if the operation fails.
     */
    public List<T> find(Query query) throws AppRuntimeException;

    /**
     * Get a count of all application objects that match the specified criteria.
     *
     * @param query the query criteria
     * @return the count of all objects matching the specified criteria
     * @throws AppRuntimeException if the operation fails.
     */
    public long count(Query query) throws AppRuntimeException;

    /**
     * Save the given application object to a persistent store.
     *
     * @param appObject the application object to save.
     * @throws AppRuntimeException if the operation fails.
     */
    public void save(T appObject) throws AppRuntimeException;

    /**
     * Delete the given application objects.
     *
     * @param appObjects the collection of application objects that should be deleted.
     * @throws AppRuntimeException if the operation fails.
     */
    public void delete(AppObject... appObjects) throws AppRuntimeException;

    /**
     * Delete application objects, identified by the given keys.
     *
     * @param ids the object IDs.
     * @throws AppRuntimeException if the operation fails.
     */
    public void deleteById(K... ids) throws AppRuntimeException;

}
