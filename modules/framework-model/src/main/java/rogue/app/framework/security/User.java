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

package rogue.app.framework.security;

import rogue.app.framework.model.attr.Attributes;

/**
 * Represents a user within the system. The <code>User</code> object captures a basic set of attributes that are
 * required by the current system to function.
 */
public interface User extends AppPrincipal<User>, Attributes
{
    /**
     * Get the user's username. For a web application, this typically would be tied to the user's email address.
     *
     * @return the user's username.
     */
    @Override
    public String getName();

    /**
     * Get the user's first name.
     *
     * @return the user's first name.
     */
    public String getFirstName();

    /**
     * Set the user's first name.
     *
     * @param firstName the user's first name.
     */
    public void setFirstName(String firstName);

    /**
     * Get the user's last name.
     *
     * @return the user's last name.
     */
    public String getLastName();

    /**
     * Set the user's last name.
     *
     * @param lastName the user's last name.
     */
    public void setLastName(String lastName);

    /**
     * Get the user's primary phone number.
     *
     * @return the user's primary phone number.
     */
    public String getPrimaryPhoneNumber();

    /**
     * Set the user's primary phone number.
     *
     * @param primaryPhoneNumber the user's primary phone number.
     */
    public void setPrimaryPhoneNumber(String primaryPhoneNumber);

    /**
     * Get the user's primary email address.
     *
     * @return the user's primary email address.
     */
    public String getPrimaryEmail();

    /**
     * Set the user's primary email address.
     *
     * @param primaryEmail the user's primary email address.
     */
    public void setPrimaryEmail(String primaryEmail);

    /**
     * Get the account status.
     *
     * @return the account status.
     */
    public AccountStatus getAccountStatus();

    /**
     * Set the account status.
     *
     * @param accountStatus the account status.
     */
    public void setAccountStatus(AccountStatus accountStatus);

}
