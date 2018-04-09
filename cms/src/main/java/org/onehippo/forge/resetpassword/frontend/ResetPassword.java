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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JavaPluginConfig;
import org.hippoecm.frontend.plugins.login.LoginHeaderItem;
import org.hippoecm.frontend.plugins.login.LoginPlugin;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClass;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.usagestatistics.UsageStatisticsSettings;
import org.hippoecm.frontend.util.WebApplicationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResetPassword class for all Wicket based front-end code.
 * Calls @Link{ResetPasswordPanel} and @Link{SetPasswordPanel} as sub panels.
 *
 * Based on @Link{SimpleLoginPlugin} and @Link{LoginPlugin}.
 */
public class ResetPassword extends RenderPlugin {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetPassword.class);

    private static final ResourceReference DEFAULT_FAVICON = new UrlResourceReference(
            Url.parse("../skin/images/hippo-cms.ico"));

    private static final String TERMS_AND_CONDITIONS_LINK = "https://www.onehippo.com/tnc";
    private static final String PARAM_CODE = "code";
    private static final String PARAM_UID = "uid";

    private static final int DEFAULT_URL_VALIDITY_IN_MINUTES = 60;

    private static final String EDITION = "edition";

    private ResourceReference editionCss;
    private final String configurationPath;

    /**
     * ResetPassword initializer.
     * @param context plugin context
     * @param config plugin config
     */
    public ResetPassword(final IPluginContext context, final IPluginConfig config) {
        super(context, new JavaPluginConfig(config));
        configurationPath = config.getString("labels.location");

        add(CssClass.append("hippo-login-plugin"));

        add(new Label("pageTitle", getString("page.title")));
        add(new ResourceLink("faviconLink", DEFAULT_FAVICON));

        final Configuration configuration = getCustomPluginUserSession();
        final Map<String, String> labelsMap = configuration != null ? configuration.getLabelMap() : new HashMap<>();
        final int urlDuration = configuration != null ? configuration.getDurationsMap().get(Configuration.URL_VALIDITY_IN_MINUTES).intValue() : DEFAULT_URL_VALIDITY_IN_MINUTES;

        final IRequestParameters requestParameters = getRequest().getQueryParameters();
        final String code = requestParameters.getParameterValue(PARAM_CODE).toString();
        final String uid = requestParameters.getParameterValue(PARAM_UID).toString();
        final boolean hasParameters = StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(uid);

        final boolean autocomplete = getPluginConfig().getAsBoolean("signin.form.autocomplete", true);

        final PanelInfo panelInfo = new PanelInfo(autocomplete, uid, configuration, context, config);
        final Panel resetPasswordForm = new ResetPasswordPanel(panelInfo);
        resetPasswordForm.setVisible(!hasParameters);
        add(resetPasswordForm);

        final Panel setPasswordForm = new SetPasswordPanel(panelInfo, code, resetPasswordForm);
        setPasswordForm.setVisible(hasParameters);
        add(setPasswordForm);

        // In case of using a different edition, add extra CSS rules to show the required styling
        if (config.containsKey(EDITION)) {
            final String edition = config.getString(EDITION);
            editionCss = new CssResourceReference(LoginPlugin.class, "login_" + edition + ".css");
        }

        final ExternalLink termsAndConditions = new ExternalLink("termsAndConditions", TERMS_AND_CONDITIONS_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return UsageStatisticsSettings.get().isEnabled();
            }
        };
        termsAndConditions.setOutputMarkupId(true);
        add(termsAndConditions);
    }

    /**
     * Checks if enterprise css should be added.
     * @param response response
     */
    @Override
    public final void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(LoginHeaderItem.get());
        if (editionCss != null) {
            response.render(CssHeaderItem.forReference(editionCss));
        }
    }

    private Configuration getCustomPluginUserSession() {
        final CustomPluginUserSession userSession = CustomPluginUserSession.get();
        final String cookieValue = getCookieValue(CustomPluginUserSession.LOCALE_COOKIE);
        if(cookieValue != null) {
            userSession.setLocale(new Locale(cookieValue));
        }
        Session session = null;

        try {
            session = userSession.getResetPasswordSession();

            final Node configNode = session.getNode(configurationPath);
            return new Configuration(configNode);
        } catch (final RepositoryException re) {
            LOGGER.error("Error trying to fetch configuration node", re);
        } finally {
            if (session != null) {
                session.logout();
                userSession.removeResetPasswordSession();
            }
        }
        return null;
    }

    protected String getCookieValue(final String cookieName) {
        Cookie[] cookies = WebApplicationHelper.retrieveWebRequest().getContainerRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}