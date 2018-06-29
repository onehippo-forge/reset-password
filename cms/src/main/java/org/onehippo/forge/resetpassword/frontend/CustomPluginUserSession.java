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
package org.onehippo.forge.resetpassword.frontend;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;

import org.apache.wicket.request.Request;
import org.hippoecm.frontend.PluginApplication;
import org.hippoecm.frontend.model.JcrSessionModel;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.repository.api.HippoNode;
import org.onehippo.repository.security.JvmCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CustomPluginUserSession
 *
 * Creates a new session with user "resetpassword" with jvm enabled password.
 * This user needs to be added using bootstrap and needs hipposys:passkey set at "jvm://"
 *
 * This session is used to check for settings and reset password.
 */
public class CustomPluginUserSession extends PluginUserSession {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPluginUserSession.class);

    private static final String USER_RESETPASSWORD = "resetpassword";
    private Session currentSession;
    public static final String LOCALE_COOKIE = "loc";
    public static final int LOCALE_COOKIE_MAXAGE = 365 * 24 * 3600; // expire one year from now

    /**
     * Constructor to create new session.
     *
     * @param request request
     */
    public CustomPluginUserSession(final Request request) {
        super(request);
    }

    /**
     * Get generic session, not user specific.
     * @return session
     */
    public static CustomPluginUserSession get() {
        return (CustomPluginUserSession) org.apache.wicket.Session.get();
    }

    /**
     * Application Name
     * @return name
     */
    @Override
    public String getApplicationName() {
        return PluginApplication.get().getPluginApplicationName();
    }

    /**
     * Get session specific for user resetpassword, with admin rights.
     * Credentials obtained using jvmCredentials.
     *
     * @return session
     */
    public Session getResetPasswordSession() {
        if (currentSession != null) {
            return currentSession;
        }
        final JcrSessionModel sessionModel = new JcrSessionModel(new UserCredentials(
                JvmCredentials.getCredentials(USER_RESETPASSWORD)));
        currentSession = sessionModel.getSession();
        return currentSession;
    }

    /**
     * Reset session.
     */
    public void removeResetPasswordSession() {
        currentSession = null;
    }

    /**
     * Retrieve the JCR query manager specific for the resetpassword session, when one is available.
     * When none is, null is returned.
     */
    @Override
    public QueryManager getQueryManager() {
        try {
            return getResetPasswordSession().getWorkspace().getQueryManager();
        } catch (final RepositoryException re) {
            LOGGER.error("Error getting queryManager", re);
            return null;
        }
    }

    /**
     * Retrieve the JCR root node specific for the resetpassword session.
     * Null is returned when no session is available or the root node cannot be obtained
     * from it.
     */
    @Override
    public HippoNode getRootNode() {
        HippoNode result = null;
        try {
            final Session jcrSession = getResetPasswordSession();
            if (jcrSession != null) {
                result = (HippoNode) jcrSession.getRootNode();
            }
        } catch (final RepositoryException e) {
            LOGGER.error("RepositoryException", e);
        }
        return result;
    }

}
