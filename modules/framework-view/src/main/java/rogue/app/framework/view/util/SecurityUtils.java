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

package rogue.app.framework.view.util;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.apache.commons.lang.StringUtils;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthConfig;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.util.BirthDate;
import org.brickred.socialauth.util.SocialAuthUtil;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.security.AppPrincipal;
import rogue.app.framework.security.User;
import rogue.app.framework.security.UserContext;
import rogue.app.framework.util.ScopedStateHolder;
import rogue.app.framework.util.StateHolder;
import rogue.app.framework.view.SiteConstants;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bunch of utilities for performing login
 */
public final class SecurityUtils
{
    private static final String CLASS_NAME = SecurityUtils.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    public static final String CUSTOMER_OBJECT = "__customer";
    private static final String ADMIN_OBJECT = "__admin";
    private static final String SOCIAL_AUTH_CLIENT = "__socialAuthClient";
    private static final String SOCIAL_PROFILE = "__socialProfile";
    private static final String EMAIL_ADDRESS = "__emailAddress";

    private static final String SOCIAL_AUTH_FACEBOOK_CONSUMER_KEY = "graph.facebook.com.consumer_key";
    private static final String SOCIAL_AUTH_FACEBOOK_CONSUMER_SECRET = "graph.facebook.com.consumer_secret";
    private static final String SOCIAL_AUTH_FACEBOOK_CUSTOM_PERMISSIONS = "graph.facebook.com.custom_permissions";

    private static SocialAuthConfig getSocialAuthConfig()
    {
        final String METHOD_NAME = "getSocialAuthConfig";
        SocialAuthConfig config = new SocialAuthConfig();
        try
        {
            Properties props = new Properties();

            String key = AppFunctions.getApplicationProperty(SOCIAL_AUTH_FACEBOOK_CONSUMER_KEY, null);
            String secret = AppFunctions.getApplicationProperty(SOCIAL_AUTH_FACEBOOK_CONSUMER_SECRET, null);
            String permissions = AppFunctions.getApplicationProperty(SOCIAL_AUTH_FACEBOOK_CUSTOM_PERMISSIONS, null);
            if (key != null && secret != null)
            {
                props.put(SOCIAL_AUTH_FACEBOOK_CONSUMER_KEY, key);
                props.put(SOCIAL_AUTH_FACEBOOK_CONSUMER_SECRET, secret);
                if (permissions != null)
                {
                    props.put(SOCIAL_AUTH_FACEBOOK_CUSTOM_PERMISSIONS, permissions);
                }
            }

            config.load(props);
        }
        catch (Exception e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to load social auth configuration", e);
        }
        return config;
    }

    private static SocialAuthManager getSocialAuthClient()
    {
        final String METHOD_NAME = "getSocialAuthClient";
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(SessionScoped.class);
        SocialAuthManager manager = stateHolder.get(SOCIAL_AUTH_CLIENT);
        if (manager == null)
        {
            try
            {
                manager = new SocialAuthManager();
                manager.setSocialAuthConfig(getSocialAuthConfig());

                stateHolder.put(SOCIAL_AUTH_CLIENT, manager);
            }
            catch (Exception e)
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to acquire social auth manager.", e);
            }
        }
        return manager;
    }

    public static String getOAuthCallbackURL()
    {
        return SiteUtils.getBaseSiteURL() + "/OAuthCallback";
    }

    /**
     * Get the login link for a given provider.
     *
     * @param providerId the provider - e.g., Google, Facebook, etc.
     * @return the login link for a given provider.
     */
    public static String getSocialLoginLink(String providerId)
    {
        final String METHOD_NAME = "getSocialLoginLink";
        String key = "__socialLoginLink_" + providerId;
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(SessionScoped.class);
        String link = stateHolder.get(key);
        if (link == null && !stateHolder.containsKey(key))
        {
            SocialAuthManager manager = getSocialAuthClient();
            if (manager != null)
            {
                try
                {
                    if ("google".equalsIgnoreCase(providerId))
                    {
                        // This is an GAE application. Make use of the native services.
                        link = UserServiceFactory.getUserService().createLoginURL(SiteUtils.getBaseSiteURL());
                    }
                    else
                    {
                        link = manager.getAuthenticationUrl(providerId, getOAuthCallbackURL());
                    }
                }
                catch (Exception e)
                {
                    LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                "Failed to get login link for provider: " + providerId, e);
                }
            }

            stateHolder.put(key, link);
        }

        return link;
    }

    public static boolean processSocialAuthCallback(HttpServletRequest request, HttpServletResponse response)
    {
        final String METHOD_NAME = "processSocialAuthCallback";
        try
        {
            SocialAuthManager manager = getSocialAuthClient();
            if (manager != null)
            {
                Map<String, String> paramsMap = SocialAuthUtil.getRequestParametersMap(request);
                AuthProvider provider = manager.connect(paramsMap);
                Profile p = provider.getUserProfile();

                // Store the profile on to the session.
                StateHolder stateHolder = ScopedStateHolder.getStateHolder(SessionScoped.class);
                stateHolder.put(SOCIAL_PROFILE, p);
                return true;
            }
        }
        catch (Exception e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to get hold of user profile!", e);
        }
        return false;
    }

    /**
     * Initialize the user context object.
     *
     * @param request the HttpServletRequest object (if available) or null.
     */
    public static void initUserContext(HttpServletRequest request) throws IOException
    {
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(SessionScoped.class);
        HttpSession session = getHttpSession(request);
        String uri = request.getRequestURI();
        boolean isAdminSession = uri.startsWith("/admin");

        if (!isAdminSession)
        {
            User user = stateHolder.get(CUSTOMER_OBJECT);
            String emailAddress = getEmailAddress();
            boolean redirectToProfilePage = false;

            if (user == null)
            {
                if (emailAddress != null)
                {
                    // Lookup the user's profile from the backend.
                    user = null; // TODO: findUserByEmailId(emailAddress);

                    if (user == null)
                    {
                        redirectToProfilePage = true;

                        // Create a new record
                        user = JpaController.getController(User.class).getNewInstance();
                        populateCustomerProfile(user);

                        // Add a message indicating that the profile is incomplete.
                        // TODO: Clean up. I want a clean separation
                        // Map<String, Object> resourceBundle = AppFunctions.getAppBundle("UIStrings");
                        String subject = "site.user.profile.incomplete.text";

                        String profilePageURL = SiteUtils.getBaseSiteURL() + SiteConstants.USER_PROFILE_PAGE;
                        String details = "site.user.profile.incomplete.details";
                        if (details != null)
                        {
                            details = MessageFormat.format(details, "<a href='" + profilePageURL + "'>",
                                                           "</a>");
                        }
                        FacesMessage message =
                                new FacesMessage(FacesMessage.SEVERITY_WARN, subject, details);
                        //message.setKey(UserSession.PROFILE_INCOMPLETE_MESSAGE_KEY);
                        // message.setEscapeDetail(false);
                        // UserSession.queueMessage(message);
                    }

                    stateHolder.put(CUSTOMER_OBJECT, user);
                }
            }
            else if (!user.isMutable())
            {
                // Verify if the customer object was updated during the current user session.
                if (user.getId() != null)
                {
                    user = JpaController.getController(User.class).findById(user.getId());
                }
                else
                {
                    User tmp = findUserByEmailId(emailAddress);
                    if (tmp != null)
                    {
                        user = tmp;
                    }
                }
                stateHolder.put(CUSTOMER_OBJECT, user);
            }

            UserContext.setContext(new UserContext(user));

//            if (redirectToProfilePage)
//            {
//                FacesContext ctx = FacesContext.getCurrentInstance();
//                if (ctx != null)
//                {
//                    ctx.getExternalContext().redirect(SiteConstants.USER_PROFILE_PAGE);
//                    ctx.responseComplete();
//                }
//            }
        }
        else
        {
            // Admin session.
            UserContext uc = (UserContext) session.getAttribute(ADMIN_OBJECT);
            if (uc == null)
            {
                if (request.getHeader("X-AppEngine-QueueName") == null)
                {
                    com.google.appengine.api.users.User user = UserServiceFactory.getUserService().getCurrentUser();
                    uc = new UserContext(user.getEmail());
                }
                else
                {
                    // System task. Bypass it
                    uc = new UserContext(AppPrincipal.SYSTEM_USER);
                }
                session.setAttribute(ADMIN_OBJECT, uc);
            }
            UserContext.setContext(uc);
        }
    }

    /**
     * Handle a log out request.
     */
    public static void handleLogout(HttpServletRequest request, HttpServletResponse response)
    {
        final String METHOD_NAME = "handleLogout";
        String logoutUrl = null;

        UserService userService = UserServiceFactory.getUserService();
        if (userService != null && !UserContext.getContext().isGuest())
        {
            logoutUrl = userService.createLogoutURL(SiteUtils.getBaseSiteURL() + SiteConstants.LOGOUT_PAGE);
        }

        request.getSession().invalidate();

        if (logoutUrl == null)
        {
            // This should never happen.
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Logout URL was null. Not sure why that would happen.");
            logoutUrl = SiteUtils.getBaseSiteURL() + SiteConstants.LOGOUT_PAGE;
        }

        try
        {
            response.sendRedirect(logoutUrl);
        }
        catch (IOException e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to send a logout redirect on URL: " + logoutUrl,
                        e);
        }

        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null)
        {
            ctx.responseComplete();
        }

    }

    private static HttpSession getHttpSession(HttpServletRequest request)
    {
        HttpSession session = null;
        if (request != null)
        {
            session = request.getSession();
        }
        else
        {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx != null)
            {
                session = (HttpSession) ctx.getExternalContext().getSession(true);
            }
        }
        return session;
    }

    /**
     * Find a user by his email id.
     *
     * @param emailAddress the user's email address.
     * @return a customer record if available, false otherwise.
     */
    private static User findUserByEmailId(String emailAddress)
    {
        User user = null;

        if (!StringUtils.isEmpty(emailAddress))
        {
            Map<String, Object> params = new HashMap<>(1);
            params.put("queryString", emailAddress);

            user = JpaController.getController(User.class)
                                .executeNamedQuerySingleResult("UserEntity.findByPrimaryEmail", params);
        }

        return user;
    }

    private static String getEmailAddress()
    {
        StateHolder stateHolder = ScopedStateHolder.getStateHolder(SessionScoped.class);
        String emailAddress = stateHolder.get(EMAIL_ADDRESS);
        if (emailAddress == null)
        {
            Profile p = stateHolder.get(SOCIAL_PROFILE);
            com.google.appengine.api.users.User user = UserServiceFactory.getUserService().getCurrentUser();
            if (p != null)
            {
                emailAddress = p.getEmail();
            }
            else if (user != null)
            {
                emailAddress = user.getEmail();
            }

            emailAddress = StringUtils.trim(emailAddress);
            emailAddress = StringUtils.lowerCase(emailAddress);

            stateHolder.put(EMAIL_ADDRESS, emailAddress);
        }

        return emailAddress;
    }

    private static void populateCustomerProfile(User customer)
    {

        StateHolder stateHolder = ScopedStateHolder.getStateHolder(SessionScoped.class);
        Profile p = stateHolder.get(SOCIAL_PROFILE);
        com.google.appengine.api.users.User user = UserServiceFactory.getUserService().getCurrentUser();

        customer.setPrimaryEmail(getEmailAddress());
        if (p != null)
        {
            customer.setFirstName(p.getFirstName());
            customer.setLastName(p.getLastName());
            // Set the date of birth too.
            BirthDate bd = p.getDob();
            if (bd != null)
            {
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(bd.getYear(), bd.getMonth() - 1, bd.getDay());
                //customer.setDateOfBirth(cal.getTime());
            }
        }
        else if (user != null)
        {
            customer.setFirstName(user.getNickname());
        }
    }

}
