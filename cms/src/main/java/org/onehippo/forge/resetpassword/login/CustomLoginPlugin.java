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
package org.onehippo.forge.resetpassword.login;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.login.DefaultLoginPlugin;
import org.hippoecm.frontend.plugins.login.LoginConfig;
import org.hippoecm.frontend.plugins.login.LoginHandler;
import org.hippoecm.frontend.plugins.login.LoginPanel;
import org.onehippo.forge.resetpassword.frontend.ResetPasswordConst;

public class CustomLoginPlugin extends DefaultLoginPlugin {

    private static final ResourceReference loginCss = new CssResourceReference(CustomLoginPlugin.class, "customloginplugin.css");

    public CustomLoginPlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
    }

    @Override
    protected LoginPanel createLoginPanel(final String id, final LoginConfig config, final LoginHandler handler) {
        return new CustomLoginForm(id, config, handler);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(loginCss));
    }

    class CustomLoginForm extends TimeZonePanel {

        public CustomLoginForm(final String id, final LoginConfig config, final LoginHandler handler) {
            super(id, config, handler);
            form.addLabelledComponent(new Label("forgot-password-label", new ResourceModel("forgot.password.label")));

            setCookieValue(ResetPasswordConst.LOCALE_COOKIE, selectedLocale, ResetPasswordConst.LOCALE_COOKIE_MAXAGE);
        }

        @Override
        protected void loginSuccess() {
            super.loginSuccess();
        }
    }
}
