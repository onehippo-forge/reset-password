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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;
import org.hippoecm.frontend.plugins.login.LoginPlugin;

import java.util.List;

public class CustomLoginPlugin extends LoginPlugin {

    private static final long serialVersionUID = 1L;

    public CustomLoginPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
    }

    @Override
    protected LoginPanel createLoginPanel(final String id, final boolean autoComplete, final List<String> locales,
                                          final LoginHandler handler) {
        return new CustomLoginForm(id, autoComplete, locales, handler);
    }

    class CustomLoginForm extends LoginPanel {

        private static final long serialVersionUID = 1L;

        public CustomLoginForm(final String id, final boolean autoComplete, final List<String> locales, final LoginHandler handler) {
            super(id, autoComplete, locales, handler);
        }

        @Override
        protected void loginSuccess() {
            super.loginSuccess();
        }
    }
}
