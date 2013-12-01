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


import com.google.apphosting.api.ApiProxy;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Utility class for doing a variety of things.
 */
public class SiteUtils
{
    // Private constructor
    private SiteUtils()
    {
    }

    /**
     * The base site URL at which the application is running. This information is fetched from the AppEngine environment.
     *
     * @return base site URL at which the application is running.
     */
    public static String getBaseSiteURL()
    {
        ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        String baseSiteURL = (String) env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        baseSiteURL = "http://" + baseSiteURL;
        return baseSiteURL;
    }

    /**
     * Send a mail to the intended recipients.
     *
     * @param toEmailId            the recipient's email id
     * @param toEmailDisplayName   the recipient's display name
     * @param fromEmailId          the sender's email id
     * @param fromEmailDisplayName the sender's display name
     * @param subject              the subject of the mail
     * @param body                 the body of the mail
     * @throws Exception if sending of the mail fails
     */
    public static void sendMail(String toEmailId, String toEmailDisplayName, String fromEmailId,
                                String fromEmailDisplayName, String subject, String body)
            throws Exception
    {
        Preconditions.checkArgument(StringUtils.isEmpty(toEmailId), "Recipient's email ID cannot be empty.");
        Preconditions.checkArgument(StringUtils.isEmpty(fromEmailId), "Sender's email ID cannot be empty.");
        Preconditions.checkArgument(StringUtils.isEmpty(subject), "Mail subject cannot be empty.");
        Preconditions.checkArgument(StringUtils.isEmpty(body), "Mail body cannot be empty.");

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);

        if (StringUtils.isEmpty(fromEmailDisplayName))
        {
            fromEmailDisplayName = fromEmailId;
        }

        msg.setFrom(new InternetAddress(fromEmailId, fromEmailDisplayName));
        String[] tmp = toEmailId.split(",");
        for (String mailId : tmp)
        {
            if (StringUtils.isEmpty(toEmailDisplayName))
            {
                toEmailDisplayName = mailId;
            }

            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(mailId, toEmailDisplayName));
        }
        msg.setSubject(subject);
        msg.setText(body);
        Transport.send(msg);
    }

}
