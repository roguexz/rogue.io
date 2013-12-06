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

package rogue.app.framework.internal.persistence.security;

import org.apache.commons.lang.StringUtils;
import rogue.app.framework.persistence.EntityImplementationFor;
import rogue.app.framework.persistence.PersistentEntity;
import rogue.app.framework.security.AccountStatus;
import rogue.app.framework.security.AppPrincipal;
import rogue.app.framework.security.User;

import javax.enterprise.context.SessionScoped;
import javax.persistence.*;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Base entity for representing a user account within the system.
 */
@Entity
@NamedQueries(
        {
                @NamedQuery(name = "UserEntity.findAll",
                            query = "SELECT e FROM UserEntity e"),
                @NamedQuery(name = "UserEntity.findAll.count",
                            query = " SELECT COUNT(e) FROM UserEntity e"),
                @NamedQuery(name = "UserEntity.searchByName",
                            query = "SELECT e FROM UserEntity e WHERE e.lowerCaseName LIKE :queryString"),
                @NamedQuery(name = "UserEntity.searchByName.count",
                            query = "SELECT COUNT(e) FROM UserEntity e WHERE e.lowerCaseName LIKE :queryString"),
                @NamedQuery(name = "UserEntity.findByName",
                            query = "SELECT e FROM UserEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "UserEntity.findByName.count",
                            query = "SELECT COUNT(e) FROM UserEntity e WHERE e.lowerCaseName = :queryString"),
                @NamedQuery(name = "UserEntity.findByPrimaryEmail",
                            query = "SELECT e FROM UserEntity e WHERE e.primaryEmail = :queryString"),
                @NamedQuery(name = "UserEntity.findPendingAccounts",
                            query = "SELECT e FROM UserEntity e WHERE e.accValue = 1 ORDER BY e.creationDate"),
                @NamedQuery(name = "UserEntity.findPendingAccounts.count",
                            query = "SELECT COUNT(e) FROM UserEntity e WHERE e.accValue = 1 ORDER BY e.creationDate"),
                @NamedQuery(name = "UserEntity.findApprovedAccounts",
                            query = "SELECT e FROM UserEntity e WHERE e.accValue = 2 ORDER BY e.creationDate"),
                @NamedQuery(name = "UserEntity.findApprovedAccounts.count",
                            query = "SELECT COUNT(e) FROM UserEntity e WHERE e.accValue = 2 ORDER BY e.creationDate")
        })
@rogue.app.framework.persistence.Cacheable(preferredScope = SessionScoped.class)
@EntityImplementationFor(User.class)
public class UserEntity extends PersistentEntity<User> implements User
{
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String primaryEmail;
    private String primaryPhoneNumber;
    private String lowerCaseName;
    private int accValue; // Default value of 0 implies that the profile is incomplete.

    public UserEntity()
    {
        super();
    }

    public UserEntity(UserEntity baseResource)
    {
        super(baseResource);
    }

    /**
     * Get the user's first name.
     *
     * @return the user's first name.
     */
    @Override
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Set the user's first name.
     *
     * @param firstName the user's first name.
     */
    @Override
    public void setFirstName(String firstName)
    {
        if (isMutable() && !Objects.equals(this.firstName, firstName))
        {
            this.firstName = firstName;
            displayName = null;
            markDirty();
        }
    }

    /**
     * Get the user's last name.
     *
     * @return the user's last name.
     */
    @Override
    public String getLastName()
    {
        return lastName;
    }

    /**
     * Set the user's last name.
     *
     * @param lastName the user's last name.
     */
    @Override
    public void setLastName(String lastName)
    {
        if (isMutable() && !Objects.equals(this.lastName, lastName))
        {
            this.lastName = lastName;
            displayName = null;
            markDirty();
        }
    }

    /**
     * Get the user's primary phone number.
     *
     * @return the user's primary phone number.
     */
    @Override
    public String getPrimaryPhoneNumber()
    {
        return primaryPhoneNumber;
    }

    /**
     * Set the user's primary phone number.
     *
     * @param primaryPhoneNumber the user's primary phone number.
     */
    @Override
    public void setPrimaryPhoneNumber(String primaryPhoneNumber)
    {
        if (isMutable() && !Objects.equals(this.primaryPhoneNumber, primaryPhoneNumber))
        {
            this.primaryPhoneNumber = primaryPhoneNumber;
            markDirty();
        }
    }

    /**
     * Get the user's primary email address.
     *
     * @return the user's primary email address.
     */
    @Override
    public String getPrimaryEmail()
    {
        return primaryEmail;
    }

    /**
     * Set the user's primary email address.
     *
     * @param primaryEmail the user's primary email address.
     */
    @Override
    public void setPrimaryEmail(String primaryEmail)
    {
        if (isMutable() && !Objects.equals(this.primaryEmail, primaryEmail))
        {
            this.primaryEmail = primaryEmail;
            markDirty();
        }
    }

    protected void prePersist()
    {
        super.prePersist();
        primaryEmail = StringUtils.lowerCase(primaryEmail);
        lowerCaseName = StringUtils.lowerCase(firstName + " " + lastName);
    }

    ///
    /// Non entity implementation
    ///
    @Transient
    private String displayName;

    @Override
    public String toString()
    {
        if (displayName == null)
        {
            if (!StringUtils.isEmpty(firstName))
            {
                displayName = firstName + " " + lastName;
            }
            else
            {
                displayName = lastName;
            }
        }
        return displayName;
    }

    public AccountStatus getAccountStatus()
    {
        return AccountStatus.fromValue(accValue, AccountStatus.LOCKED);
    }

    public void setAccountStatus(AccountStatus accountStatus)
    {
        if (isMutable() && this.accValue != accountStatus.getValue())
        {
            this.accValue = accountStatus.getValue();
            markDirty();
        }
    }


    ///
    /// Non-entity methods
    ///

    @Override
    public String getName()
    {
        return getPrimaryEmail();
    }

    @Override
    public Enumeration<? extends AppPrincipal> getRoles()
    {
        return null;  //TODO: Fix this.
    }


    ///
    /// Override of Equals & hashCode
    ///


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof UserEntity))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        UserEntity that = (UserEntity) o;

        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null)
        {
            return false;
        }
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null)
        {
            return false;
        }
        if (primaryEmail != null ? !primaryEmail.equals(that.primaryEmail) : that.primaryEmail != null)
        {
            return false;
        }
        if (primaryPhoneNumber != null ? !primaryPhoneNumber.equals(that.primaryPhoneNumber) :
            that.primaryPhoneNumber != null)
        {
            return false;
        }
        if (accValue != that.accValue)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (primaryPhoneNumber != null ? primaryPhoneNumber.hashCode() : 0);
        result = 31 * result + (primaryEmail != null ? primaryEmail.hashCode() : 0);
        result = 31 * result + accValue;
        return result;
    }
}
