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

import java.io.Serializable;

/**
 * The <code>PageInfo</code> class represents information about a page associated with a controller.
 */
public class PageInfo implements Serializable
{

    private final String name;
    private final PageType pageType;
    private final String pagePath;
    private final boolean fragment;
    private final String outcome;

    public PageInfo(PageMetadata metadata)
    {
        this.name = metadata.displayName();
        this.outcome = metadata.outcome();
        this.pageType = metadata.pageType();
        this.pagePath = metadata.path();
        this.fragment = metadata.fragment();
    }

    public PageInfo(String name, String outcome, PageType pageType, String pagePath, boolean fragment)
    {
        this.name = name;
        this.outcome = outcome;
        this.pageType = pageType;
        this.pagePath = pagePath;
        this.fragment = fragment;
    }

    public String getName()
    {
        return name;
    }

    public String getOutcome()
    {
        return outcome;
    }


    public PageType getPageType()
    {
        return pageType;
    }

    public String getPagePath()
    {
        return pagePath;
    }

    public boolean isFragment()
    {
        return fragment;
    }
}