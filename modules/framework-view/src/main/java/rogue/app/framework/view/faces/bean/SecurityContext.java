package rogue.app.framework.view.faces.bean;

import rogue.app.framework.security.User;
import rogue.app.framework.security.UserContext;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Managed bean for accessing the various security operations on the current request.
 */
@Named
@RequestScoped
public class SecurityContext implements Serializable
{
    @Inject
    private LoginAction loginAction;
    @Inject
    private LogoutAction logoutAction;

    /**
     * Get the name of the currently logged in user.
     *
     * @return the name of the currently logged in user.
     */
    public String getUsername()
    {
        return UserContext.getContext().getUsername();
    }

    /**
     * Get the currently logged in user object.
     *
     * @return the currently logged in user object.
     */
    public User getUser()
    {
        return UserContext.getContext().getUser();
    }

    /**
     * Returns true if the current request has a logged in user.
     *
     * @return true if the current request has a logged in user, false otherwise.
     */
    public boolean isLoggedIn()
    {
        return !UserContext.getContext().isGuest();
    }

    /**
     * Get the action associated with performing a login.
     *
     * @return the action associated with performing a login.
     */
    public LoginAction getLoginAction()
    {
        return loginAction;
    }

    /**
     * Get the action associated with performing a logout.
     *
     * @return the action associated with performing a logout.
     */
    public LogoutAction getLogoutAction()
    {
        return logoutAction;
    }
}
