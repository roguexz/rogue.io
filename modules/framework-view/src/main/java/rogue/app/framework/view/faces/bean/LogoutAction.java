package rogue.app.framework.view.faces.bean;

import rogue.app.framework.view.faces.CommandInvocation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Command invocation for handling user log out.
 */
@ApplicationScoped
public class LogoutAction extends CommandInvocation
{
    @Inject
    private Instance<AuthenticationManager> authenticationManager;

    @Override
    public String invokeAction()
    {
        authenticationManager.get().processLogout();
        return null;
    }
}
