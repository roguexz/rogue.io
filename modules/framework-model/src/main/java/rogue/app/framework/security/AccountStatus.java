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

/**
 * Represents the various states in which an account can be.
 */
public enum AccountStatus
{
    /**
     * Incomplete profile.
     */
    PROFILE_INCOMPLETE(0, "Profile Incomplete"),
    /**
     * Account pending approval.
     */
    PENDING_APPROVAL(1, "Pending Approval"),
    /**
     * Account approved. Awaiting confirmation from the user's side
     */
    ACCOUNT_APPROVED(2, "Account Approved"),
    /**
     * A regular active account.
     */
    ACTIVE(3, "Active"),
    /**
     * Account is locked.
     */
    LOCKED(4, "Locked"),
    /**
     * Account is deleted.
     */
    DELETED(5, "Deleted");

    private int accValue;
    private String displayString;

    private AccountStatus(int value, String displayString)
    {
        this.accValue = value;
        this.displayString = displayString;
    }

    /**
     * Get the integer value associated with the account status.
     *
     * @return integer representing the value associated with the status.
     */
    public int getValue()
    {
        return accValue;
    }


    /**
     * Look up the <code>AccountStatus</code> enum associated with the given integer value. If the value does not match
     * any registered values, the <code>defaultStatus</code> object is returned.
     *
     * @param value         the integer value of the status.
     * @param defaultStatus the default status to return if the given value does not match the registered statuses.
     * @return the <code>AccountStatus</code> enum (or default) that matches the given value.
     */
    public static AccountStatus fromValue(int value, AccountStatus defaultStatus)
    {
        for (AccountStatus ac : AccountStatus.values())
        {
            if (value == ac.getValue())
            {
                return ac;
            }
        }
        return defaultStatus;
    }

    @Override
    public String toString()
    {
        return displayString;
    }
}
