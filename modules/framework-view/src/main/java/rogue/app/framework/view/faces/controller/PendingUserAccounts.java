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

package rogue.app.framework.view.faces.controller;

import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.security.User;
import rogue.app.framework.view.faces.EntityFacesController;
import rogue.app.framework.view.faces.model.QueryDataModel;

import javax.faces.model.DataModel;

/**
 * Listing of the pending user accounts.
 */
//@Named("PendingAccountsController")
//@ViewScoped
//@ControllerFor(model = User.class)
public class PendingUserAccounts extends EntityFacesController<User>
{
    public PendingUserAccounts()
    {
        super();
    }

    @Override
    public User getNewInstance()
    {
        return null;
    }

    @Override
    public User getMutableInstance(User item)
    {
        return null;
    }

    @Override
    protected DataModel<User> constructDataModel()
    {
        JpaQuery listingQuery = new JpaQuery("UserEntity.findPendingAccounts", true, null);
        JpaQuery countQuery = new JpaQuery("UserEntity.findPendingAccounts.count", true, null);
        return new QueryDataModel<>(getDelegate(), getConverter(), listingQuery, countQuery);
    }
}
