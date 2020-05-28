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

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.hippoecm.frontend.attributes.ClassAttribute;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.UserCredentials;
import org.hippoecm.frontend.plugin.config.impl.JcrPluginConfig;
import org.hippoecm.frontend.plugins.cms.admin.password.validation.IPasswordValidationService;
import org.hippoecm.frontend.plugins.cms.admin.password.validation.PasswordValidationServiceImpl;
import org.hippoecm.frontend.plugins.cms.admin.password.validation.PasswordValidationStatus;
import org.hippoecm.frontend.plugins.cms.admin.users.User;
import org.hippoecm.frontend.plugins.login.LoginResourceModel;
import org.hippoecm.frontend.session.LoginException;
import org.hippoecm.frontend.session.PluginUserSession;
import org.onehippo.repository.security.JvmCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.HIPPO_USERS_PATH;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORDVALIDATION_LOCATION;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_KEY;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_TIMESTAMP;

/**
 * SetPasswordPanel
 * Panel on ResetPasswordFrame, showing two fields for entering the new password twice
 * <p>
 * Based on @Link{SimpleLoginPlugin} en @Link{LoginPlugin}.
 */
public class SetPasswordPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(SetPasswordPanel.class);

    private final IPasswordValidationService passwordValidationService;

    private final Map<String, String> labelMap;

    private final int urlValidity;

    /**
     * Constructor
     *
     * @param panelInfo          information used for configuring this panel
     * @param code               check for valid password reset
     * @param resetPasswordPanel original panel, reference used to set the proper panel visible
     */
    public SetPasswordPanel(final PanelInfo panelInfo, final String code, final Panel resetPasswordPanel) {
        super("setPasswordForm");
        labelMap = panelInfo.getConfiguration().getLabelMap();
        urlValidity = panelInfo.getConfiguration().getDurationsMap().get(Configuration.URL_VALIDITY_IN_MINUTES).intValue();

        add(ClassAttribute.append("hippo-login-panel-center"));
        add(new SetPasswordForm(panelInfo, code, resetPasswordPanel));
        String passwordValidationLocation = panelInfo.getConfig().get(PASSWORDVALIDATION_LOCATION).toString();
        JcrPluginConfig pluginConfig = new JcrPluginConfig(new JcrNodeModel(passwordValidationLocation));
        passwordValidationService = new PasswordValidationServiceImpl(panelInfo.getContext(), pluginConfig);

    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(OnDomReadyHeaderItem.forScript("$('#password-verification').bind(\"cut copy paste\",function(e) {" +
                "  e.preventDefault();" +
                "});"));
    }

    protected class SetPasswordForm extends Form {

        private final String code;
        private final String uid;

        private final FeedbackPanel feedback;
        private TextField<String> passwordField;
        private IModel<String> passwordPlaceholder;
        private TextField<String> passwordVerificationField;
        private IModel<String> passwordVerificationPlaceholder;

        private String password;
        private String passwordVerification;

        private final WebMarkupContainer setPasswordFormTable = new WebMarkupContainer("setPasswordFormTable");
        private final WebMarkupContainer loginLink;

        public SetPasswordForm(final PanelInfo panelInfo, final String code, final Panel resetPanel) {
            super("setPasswordForm");
            this.code = code;
            uid = panelInfo.getUserId();

            createPasswordFormTable(panelInfo.isAutoComplete());
            add(setPasswordFormTable);

            feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            feedback.setEscapeModelStrings(false);
            add(feedback);

            loginLink = new WebMarkupContainer("loginLink");
            loginLink.add(new Label("login.link.text", labelMap.get(Configuration.LOGIN_LINK_TEXT)));
            loginLink.setVisible(false);
            add(loginLink);

            final Link<Void> resetLink = new Link<Void>("resetLink") {

                @Override
                public void onClick() {
                    resetPanel.setVisible(true);
                    setVisible(false);
                }
            };
            resetLink.add(new Label("reset.link.text", labelMap.get(Configuration.REQUEST_NEW_LINK_TEXT)));
            resetLink.setVisible(false);
            setPasswordFormTable.add(resetLink);

            final boolean hasParameters = StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(uid);
            if (hasParameters) {
                final boolean validParameters = validateForm(code, uid);
                if (!validParameters) {
                    setPasswordFormTable.setVisible(false);
                    resetLink.setVisible(true);
                }
            }
        }

        private void createPasswordFormTable(final boolean autocomplete) {
            setPasswordFormTable.add(new Label("intro.text", labelMap.get(Configuration.SET_PW_INTRO_TEXT)));

            setPasswordFormTable.add(passwordField = new PasswordTextField("new-password",
                    new PropertyModel<>(this, "password")));
            setPasswordFormTable.add(passwordVerificationField = new PasswordTextField("repeat-password",
                    new PropertyModel<>(this, "passwordVerification")));
            passwordVerificationField.setMarkupId("password-verification");
            passwordPlaceholder = new LoginResourceModel("password-label", ResetPassword.class);
            passwordVerificationPlaceholder = new LoginResourceModel("password-verification-label", ResetPassword.class);
            addAjaxAttributeModifier(passwordField, "placeholder", passwordPlaceholder);
            addAjaxAttributeModifier(passwordVerificationField, "placeholder", passwordVerificationPlaceholder);
            setPasswordFormTable.add(new AttributeModifier("autocomplete", new Model<>(autocomplete ? "on" : "off")));
            setPasswordFormTable.add(new Button("submit", new ResourceModel("submit-label")));
        }

        @Override
        protected final void onValidate() {
            super.onValidate();

            if (!passwordField.getValue().equals(passwordVerificationField.getValue())) {
                error(labelMap.get(Configuration.PASSWORDS_DO_NOT_MATCH));
            }

            validateForm(code, uid);

            try {
                PluginUserSession.get().login(new UserCredentials(JvmCredentials.getCredentials("resetpassword")));
                // validate password
                final User user = new User(uid);
                final List<PasswordValidationStatus> statuses =
                        passwordValidationService.checkPassword(passwordField.getValue(), user);
                for (final PasswordValidationStatus status : statuses) {
                    if (!status.accepted()) {
                        error(status.getMessage());
                    }
                }
            } catch (final RepositoryException re) {
                log.error("Error validating SetPasswordForm", re);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            } catch (LoginException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected final void onSubmit() {
            super.onSubmit();

            final CustomPluginUserSession userSession = CustomPluginUserSession.get();
            Session session = null;
            try {
                session = userSession.getResetPasswordSession();
                final User user = new User(uid);
                user.savePassword(password);
                info(labelMap.get(Configuration.PASSWORD_RESET_DONE));

                setPasswordFormTable.setVisible(false);
                loginLink.setVisible(true);

                final Node userNode = session.getNode(HIPPO_USERS_PATH + uid);
                if (userNode == null) {
                    log.info("Unknown username: {}", uid);
                    error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                    return;
                }

                if (userNode.hasProperty(PASSWORD_RESET_TIMESTAMP)) {
                    userNode.getProperty(PASSWORD_RESET_TIMESTAMP).remove();
                }

                if (userNode.hasProperty(PASSWORD_RESET_KEY)) {
                    userNode.getProperty(PASSWORD_RESET_KEY).remove();
                }

                // persist code and exp.date
                session.save();

            } catch (final RepositoryException re) {
                log.error("Error saving password SetPasswordForm", re);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            } finally {
                PluginUserSession.get().logout();
                if (session != null) {
                    session.logout();
                    userSession.removeResetPasswordSession();
                }
            }
        }

        private boolean validateForm(final String code, final String uid) {
            final CustomPluginUserSession userSession = CustomPluginUserSession.get();
            Session session = null;
            try {
                session = userSession.getResetPasswordSession();

                if (validateUid(code, uid, session)) {
                    return false;
                }
            } catch (final RepositoryException re) {
                log.error("Error validating SetPasswordForm", re);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
                return false;
            } finally {
                if (session != null) {
                    session.logout();
                    userSession.removeResetPasswordSession();
                }
            }
            return true;
        }

        private boolean validateUid(final String code, final String uid, final Session session) throws RepositoryException {
            try {
                final Node userNode = session.getNode(HIPPO_USERS_PATH + uid);

                validateCode(code, uid, userNode);
                validateTimestamp(uid, userNode);

            } catch (final PathNotFoundException pnfe) {
                log.error("Unknown username: {}", uid, pnfe);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return true;
            } catch (final ResetPasswordException rpe) {
                log.info("Error handling verification url: {}", rpe);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return true;
            } catch (final ResetPasswordLinkExpiredException rpplee) {
                log.info("Error handling verification url: {}", rpplee);
                error(labelMap.get(Configuration.RESET_LINK_EXPIRED));
                return true;
            }
            return false;
        }

        private void validateTimestamp(final String uid, final Node userNode) throws RepositoryException, ResetPasswordException, ResetPasswordLinkExpiredException {
            final Calendar currentTime = Calendar.getInstance();

            final Calendar persistedTimestamp;
            if (userNode.hasProperty(PASSWORD_RESET_TIMESTAMP)) {
                persistedTimestamp = userNode.getProperty(PASSWORD_RESET_TIMESTAMP).getDate();
            } else {
                throw new ResetPasswordException(
                        "No " + PASSWORD_RESET_TIMESTAMP + " property found on node: " + uid);
            }
            persistedTimestamp.add(Calendar.MINUTE, urlValidity);
            if (currentTime.after(persistedTimestamp)) {
                throw new ResetPasswordLinkExpiredException("The link has expired");
            }
        }

        private void validateCode(final String code, final String uid, final Node userNode) throws RepositoryException, ResetPasswordException {
            final String persistedCode;
            if (userNode.hasProperty(PASSWORD_RESET_KEY)) {
                persistedCode = userNode.getProperty(PASSWORD_RESET_KEY).getString();
            } else {
                throw new ResetPasswordException("No email present on node: " + uid);
            }
            if (!code.equals(persistedCode)) {
                throw new ResetPasswordException(
                        "Verification code passed in the url does not equal the persisted verification code");
            }
        }

        private void addAjaxAttributeModifier(final Component component, final String name, final IModel<String> value) {
            final AjaxAttributeModifier modifier = new AjaxAttributeModifier(name, value);
            component.add(modifier);
        }
    }
}