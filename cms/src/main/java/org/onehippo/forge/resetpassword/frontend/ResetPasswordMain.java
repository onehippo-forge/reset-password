/*
 *  Copyright 2008-2020 Bloomreach Inc. (https://www.bloomreach.com)
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

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

import org.hippoecm.frontend.Main;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.session.PluginUserSession;
import org.hippoecm.frontend.session.UserSession;

import org.onehippo.repository.security.JvmCredentials;

/**
 * Main class to start wicket code for ResetPassword page.
 */
public class ResetPasswordMain extends Main {

    public static final String USER_RESETPASSWORD = "resetpassword";

    @Override
    public UserSession newSession(final Request request, final Response response) {

        return new PluginUserSession(request) {
            @Override
            public UserCredentials getUserCredentials() {
                return new UserCredentials(JvmCredentials.getCredentials(USER_RESETPASSWORD));
            }

            @Override
            protected UserCredentials getUserCredentialsFromRequestAttribute() {
                return new UserCredentials(JvmCredentials.getCredentials(USER_RESETPASSWORD));
            }
        };
    }

    @Override
    public String getPluginApplicationName() {
        return getConfigurationParameter(PLUGIN_APPLICATION_NAME_PARAMETER, "resetpassword");
    }
}
