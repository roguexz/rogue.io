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

package rogue.app.framework.view;

/**
 * Collection of most of the commonly referred constants across various classes.
 *
 * TODO: Get rid of this if you don't need it.
 */
public final class SiteConstants
{
    public static final String RETURN_URL = "_site_return_url";

    public static final String TERMS_AND_CONDITIONS_ACCEPTED = "T_AND_C_ACCEPTED";

    // Pages
    public static final String ACKNOWLEDGE_TERMS_PAGE = "/site/legal/acknowledge-terms.xhtml";
    public static final String USER_PROFILE_PAGE = "/user/profile.xhtml";
    public static final String LOGOUT_PAGE = "/system/logout.xhtml";
    public static final String EXPIRED_PAGE = "/system/expired.xhtml";

    private SiteConstants()
    {
        // No extensions possible
    }
}
