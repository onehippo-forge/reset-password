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
package org.onehippo.forge.resetpassword.login;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.DefaultLoginPlugin;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;
import org.onehippo.forge.resetpassword.frontend.CustomPluginUserSession;

public class CustomLoginPlugin extends DefaultLoginPlugin {

    public CustomLoginPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
    }

    @Override
    protected LoginPanel createLoginPanel(final String id, final boolean autoComplete, final List<String> locales,
                                          final LoginHandler handler) {
        return new CustomLoginForm(id, autoComplete, locales, handler);
    }

    class CustomLoginForm extends LoginPanel {

        public CustomLoginForm(final String id, final boolean autoComplete, final List<String> locales, final LoginHandler handler) {
            super(id, autoComplete, locales, handler);
            form.addLabelledComponent(new Label("forgot-password-label", new ResourceModel("forgot.password.label")));

            setCookieValue(CustomPluginUserSession.LOCALE_COOKIE, selectedLocale, CustomPluginUserSession.LOCALE_COOKIE_MAXAGE);
        }

        @Override
        protected void loginSuccess() {
            super.loginSuccess();
        }
    }
}
