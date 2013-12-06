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

package rogue.app.framework.view.faces.bean;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.apache.commons.lang.StringUtils;
import org.brickred.socialauth.*;
import org.brickred.socialauth.util.Constants;
import org.brickred.socialauth.util.SocialAuthUtil;
import org.jboss.weld.literal.DefaultLiteral;
import rogue.app.framework.internal.util.CDIUtils;
import rogue.app.framework.internal.view.servlet.WebContext;
import rogue.app.framework.model.attr.Attribute;
import rogue.app.framework.model.attr.Attributes;
import rogue.app.framework.persistence.JpaController;
import rogue.app.framework.persistence.JpaQuery;
import rogue.app.framework.security.AppPrincipal;
import rogue.app.framework.security.User;
import rogue.app.framework.security.UserContext;
import rogue.app.framework.view.util.SiteUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>AuthenticationManager</code> is responsible for managing all the social authentication providers.
 */
@SessionScoped
public class AuthenticationManager implements Serializable
{
    private static final String CLASS_NAME = AuthenticationManager.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final Map<String, String> domainMap = new HashMap<>(14);
    private Set<String> providers = new TreeSet<>();
    private Map<String, String> loginLinks = new HashMap<>(3);
    private Profile socialProfile;
    private User loggedInUser;
    private SocialAuthConfig socialAuthConfig = new SocialAuthConfig();
    private SocialAuthManager socialAuthManager = new SocialAuthManager();

    static
    {
        // Because SocialAuthConfig does not expose this data!
        domainMap.put(Constants.GOOGLE, "www.google.com");
        domainMap.put(Constants.YAHOO, "api.login.yahoo.com");
        domainMap.put(Constants.TWITTER, "twitter.com");
        domainMap.put(Constants.FACEBOOK, "graph.facebook.com");
        domainMap.put(Constants.HOTMAIL, "consent.live.com");
        domainMap.put(Constants.LINKEDIN, "api.linkedin.com");
        domainMap.put(Constants.FOURSQUARE, "foursquare.com");
        domainMap.put(Constants.MYSPACE, "api.myspace.com");
        domainMap.put(Constants.SALESFORCE, "login.salesforce.com");
        domainMap.put(Constants.YAMMER, "www.yammer.com");
        domainMap.put(Constants.MENDELEY, "api.mendeley.com");
        domainMap.put(Constants.RUNKEEPER, "runkeeper.com");
        domainMap.put(Constants.GOOGLE_PLUS, "googleapis.com");
        domainMap.put(Constants.INSTAGRAM, "api.instagram.com");
    }

    public AuthenticationManager()
    {
    }

    @PostConstruct
    private void init()
    {
        final String METHOD_NAME = "init";
        final String SOCIAL_AUTH = "socialauth.";
        try
        {
            JpaController<Attribute> controller = JpaController.getController(Attribute.class);
            final Map<String, Object> queryParams = new HashMap<>(3);
            queryParams.put("nameSpace", Attributes.SYSTEM_NAMESPACE);
            queryParams.put("appObjRefKey", null);
            queryParams.put("layerKey", null); // base definitions only.
            queryParams.put("queryString", SOCIAL_AUTH + "%");
            JpaQuery listingQuery = new JpaQuery("AttributeEntity.searchByName", true, queryParams);
            List<Attribute> list = controller.find(listingQuery);

            if (!list.isEmpty())
            {
                Properties properties = new Properties();
                for (Attribute attr : list)
                {
                    String key = attr.getName();
                    key = key.substring(SOCIAL_AUTH.length());
                    for (Map.Entry<String, String> domain : domainMap.entrySet())
                    {
                        if (key.startsWith(domain.getValue()))
                        {
                            providers.add(domain.getKey());
                        }
                    }
                    properties.put(key, attr.getValue());
                }

                socialAuthConfig.load(properties);
                // Set the configuration on to the social auth manager
                socialAuthManager.setSocialAuthConfig(socialAuthConfig);

                // Setup all the required login links
                String oauthCallbackURL = SiteUtils.getBaseSiteURL() + "/OAuthCallback";
                Iterator<String> pNameIterator = providers.iterator();
                while (pNameIterator.hasNext())
                {
                    String pName = pNameIterator.next();
                    try
                    {
                        boolean isCustomPermission = !StringUtils.isEmpty(
                                properties.getProperty(SOCIAL_AUTH + "." + pName + ".custom_permissions"));

                        loginLinks.put(pName,
                                       socialAuthManager.getAuthenticationUrl(pName, oauthCallbackURL,
                                                                              isCustomPermission ? Permission.CUSTOM :
                                                                              Permission.AUTHENTICATE_ONLY));
                    }
                    catch (Exception e)
                    {
                        LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
                                    "Failed to acquire login link for provider: " + pName, e);
                        pNameIterator.remove();
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to load social authentication providers", e);
            // Flush the providers??
        }

        // Finally check whether Google was added or not. If not, then add it.
        if (!providers.contains(Constants.GOOGLE))
        {
            providers.add(Constants.GOOGLE);
            // This is an GAE application. Make use of the native services.
            String link = UserServiceFactory.getUserService().createLoginURL(SiteUtils.getBaseSiteURL());
            loginLinks.put(Constants.GOOGLE, link);
        }
    }

    /**
     * Get the collection of configured providers.
     *
     * @return the collection of configured providers.
     */
    public Set<String> getProviders()
    {
        return providers;
    }

    /**
     * Get the collection of login links.
     *
     * @return the collection of login links.
     */
    public Map<String, String> getLoginLinks()
    {
        return loginLinks;
    }

    /**
     * Initialize the user context.
     *
     * @param remoteUser the value returned by executing {@link javax.servlet.http.HttpServletRequest#getRemoteUser()}.
     */
    public void initUserContext(String remoteUser)
    {
        UserContext userContext;
        if (AppPrincipal.SYSTEM_USER.equals(remoteUser))
        {
            userContext = new UserContext(AppPrincipal.SYSTEM_USER);
        }
        else
        {
            if (loggedInUser == null)
            {
                com.google.appengine.api.users.User gaeUser = UserServiceFactory.getUserService().getCurrentUser();
                if (StringUtils.isEmpty(remoteUser))
                {
                    if (socialProfile != null)
                    {
                        remoteUser = socialProfile.getEmail();
                    }
                    else if (gaeUser != null)
                    {
                        remoteUser = gaeUser.getEmail();
                    }
                }

                if (remoteUser != null)
                {
                    // Try to load the user from the backend.
                    loggedInUser = findUserByEmailId(remoteUser);
                    if (loggedInUser == null)
                    {
                        loggedInUser = createUser(remoteUser);
                    }
                }
            }

            userContext = new UserContext(loggedInUser);
        }
        UserContext.setContext(userContext);
    }

    /**
     * Process the callback received from a social authentication provider.
     *
     * @param requestParameters the request parameters obtained by invoking
     *                          {@link SocialAuthUtil#getRequestParametersMap(javax.servlet.http.HttpServletRequest)}
     * @return true if the processing was successful, false otherwise.
     */
    public boolean processSocialAuthCallback(Map<String, String> requestParameters)
    {
        final String METHOD_NAME = "processSocialAuthCallback";
        try
        {
            AuthenticationManager manager =
                    CDIUtils.lookupBean(AuthenticationManager.class, SessionScoped.class, DefaultLiteral.INSTANCE);

            if (manager != null)
            {
                AuthProvider provider = socialAuthManager.connect(requestParameters);
                socialProfile = provider.getUserProfile();
                return true;
            }
            else
            {
                LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Unable to acquire authentication manager!");
            }
        }
        catch (Exception e)
        {
            LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "Failed to get hold of user profile!", e);
        }
        return false;
    }

    /**
     * Process a logout request.
     */
    public void processLogout()
    {
        final String METHOD_NAME = "handleLogout";
        String logoutUrl = SiteUtils.getBaseSiteURL();

        UserService userService = UserServiceFactory.getUserService();
        if (userService != null && !UserContext.getContext().isGuest())
        {
            logoutUrl = userService.createLogoutURL(logoutUrl);
        }

        try
        {
            WebContext.getResponse().sendRedirect(logoutUrl);
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

        // This attribute is picked up by the AppConfigFilter.
        WebContext.getRequest().setAttribute("destroy.session", true);
    }

    /**
     * Create a new user object from the given details.
     *
     * @return a new user object from the given details.
     */
    protected User createUser(String email)
    {
        User appUser = JpaController.getController(User.class).getNewInstance();

        String firstName, lastName;
        if (socialProfile != null)
        {
            firstName = socialProfile.getFirstName();
            lastName = socialProfile.getLastName();
        }
        else
        {
            com.google.appengine.api.users.User gaeUser = UserServiceFactory.getUserService().getCurrentUser();
            firstName = null;
            lastName = gaeUser.getNickname();
        }

        appUser.setPrimaryEmail(email);
        appUser.setFirstName(firstName);
        appUser.setLastName(lastName != null ? lastName : email);

        return appUser;
    }

    /**
     * Find a user by his email id.
     *
     * @param emailAddress the user's email address.
     * @return a customer record if available, false otherwise.
     */
    private User findUserByEmailId(String emailAddress)
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
}
