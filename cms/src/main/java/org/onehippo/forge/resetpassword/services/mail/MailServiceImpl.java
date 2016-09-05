/*
 *  Copyright 2008-2016 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.resetpassword.services.mail;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Class to send mails.
 */
public class MailServiceImpl implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

    private static final String DEFAULT_MAIL_SESSION = "mail/Session";

    private String mailSession;

    @Override
    public void sendMail(final MailMessage mailMessage) throws EmailException {

        final HtmlEmail email = new HtmlEmail();

        final Session session = getSession();
        if (session == null) {
            throw new EmailException("Unable to send mail; no mail session available");
        }

        email.setMailSession(session);
        email.addTo(mailMessage.getToMail(), mailMessage.getToName());
        email.setFrom(mailMessage.getFromMail(), mailMessage.getFromName());
        email.setSubject(mailMessage.getSubject());

        // set the html message
        email.setHtmlMsg(mailMessage.getHtmlTextBody());

        // set the alternative message
        email.setTextMsg(mailMessage.getPlainTextBody());

        // send the email
        email.send();
    }

    protected Session getSession() {
        String sessionName = getMailSession();
        if(sessionName == null || sessionName.length() <= 0) {
            sessionName = DEFAULT_MAIL_SESSION;
        }
        Context initialContext = null;
        try {
            initialContext = new InitialContext();
            final Context context = (Context) initialContext.lookup("java:comp/env");
            return (Session) context.lookup(sessionName);
        } catch (final NamingException e) {
            LOGGER.error("Error creating email session: " + sessionName, e);
            try {
                if (initialContext != null) {
                    initialContext.close();
                }
            } catch (final NamingException exception) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error("Error finding context", exception);
                }
            }
        }
        return null;
    }


    @Override
    public void setMailSessionName(final String mailSession) {
        this.mailSession = mailSession;
    }

    public String getMailSession() {
        return mailSession;
    }
}
