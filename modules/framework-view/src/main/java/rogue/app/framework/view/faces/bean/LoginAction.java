package rogue.app.framework.view.faces.bean;

import rogue.app.framework.view.faces.CommandInvocation;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.*;

/**
 * Command invocation that can be associated with a login action.
 */
@SessionScoped
public class LoginAction extends CommandInvocation<String>
{
    @Inject
    private Instance<AuthenticationManager> authenticationManager;

    private List<Map.Entry<String, String>> loginLinks;

    public LoginAction()
    {
    }

    /**
     * Get a list of configured authentication providers.
     *
     * @return a list of configured authentication providers.
     */
    public Set<String> getProviders()
    {
        return authenticationManager.get().getProviders();
    }

    /**
     * Get the entry set of login links.
     *
     * @return the map of login links.
     */
    public List<Map.Entry<String, String>> getLoginLinks()
    {
        // Because ui:repeat does not support the collection interface.
        if (loginLinks == null)
        {
            loginLinks = new ArrayList<>();
            for (Map.Entry<String, String> entry : authenticationManager.get().getLoginLinks().entrySet())
            {
                loginLinks.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }
        return loginLinks;
    }
}
