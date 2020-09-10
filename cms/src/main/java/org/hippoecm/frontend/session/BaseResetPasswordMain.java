/*
 *  Copyright 2020 Bloomreach Inc. (https://www.bloomreach.com)
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
package org.hippoecm.frontend.session;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;

import org.hippoecm.frontend.Main;
import org.hippoecm.frontend.model.JcrSessionModel;
import org.hippoecm.frontend.model.UserCredentials;

import org.onehippo.repository.security.JvmCredentials;

/**
 * Base main class in a product package to access the working PluginUserSession constructor.
 */
public class BaseResetPasswordMain extends Main {

    public static final String USER_RESETPASSWORD = "resetpassword";

    @Override
    public UserSession newSession(final Request request, final Response response) {
        final JcrSessionModel jcrSessionModel = new JcrSessionModel(new UserCredentials(JvmCredentials.getCredentials(USER_RESETPASSWORD)));
        return new PluginUserSession(request, jcrSessionModel);
    }
}
