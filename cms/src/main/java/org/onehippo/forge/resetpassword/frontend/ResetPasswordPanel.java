/*
 *  Copyright 2008-2019 BloomReach Inc. (https://www.bloomreach.com)
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClass;
import org.hippoecm.frontend.util.WebApplicationHelper;
import org.hippoecm.hst.util.HstRequestUtils;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.forge.resetpassword.services.mail.MailMessage;
import org.onehippo.forge.resetpassword.services.mail.MailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.HIPPO_USERS_PATH;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_KEY;
import static org.onehippo.forge.resetpassword.frontend.ResetPasswordConst.PASSWORD_RESET_TIMESTAMP;

/**
 * ResetPasswordPanel
 * Panel on ResetPasswordFrame, showing only a field for entering the username
 * <p>
 * Based on SimpleLoginPlugin and LoginPlugin.
 */
public class ResetPasswordPanel extends Panel {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordPanel.class);



    private static final String DATE_FORMAT = "dd-MM-yyyy HH:mm";
    private static final String SPACE = " ";

    private static final Pattern EMAIL_PARAM_NAME_PATTERN = Pattern.compile("\\$\\{name\\}");
    private static final Pattern EMAIL_PARAM_URL_PATTERN = Pattern.compile("\\$\\{url\\}");
    private static final Pattern EMAIL_PARAM_VALIDITY_PATTERN = Pattern.compile("\\$\\{validity\\}");

    /**
     * Constructor
     *
     * @param panelInfo information used for configuring this panel
     */
    public ResetPasswordPanel(final PanelInfo panelInfo) {
        super("resetPasswordForm");

        add(CssClass.append("login-panel-center"));
        add(new ResetPasswordForm(panelInfo.isAutoComplete(), panelInfo.getUserId(), panelInfo.getConfiguration()));
    }

    /**
     * Inner class for form on panel
     */
    protected class ResetPasswordForm extends Form {

        private final FeedbackPanel feedback;
        private final WebMarkupContainer resetPasswordFormTable;
        private final Map<String, String> labelMap;
        private final int urlValidityDuration;

        private String userId;

        /**
         * Constructor
         *
         * @param autoComplete  check if username field should allow auto complete
         * @param userId        user id
         * @param configuration configuration containing all labels and configurable lengths
         */
        public ResetPasswordForm(final boolean autoComplete, final String userId, final Configuration configuration) {
            super("resetPasswordForm");

            setUserId(userId);

            labelMap = configuration.getLabelMap();
            urlValidityDuration = configuration.getDurationsMap().get(Configuration.URL_VALIDITY_IN_MINUTES).intValue();

            resetPasswordFormTable = new WebMarkupContainer("resetPasswordFormTable");

            addLabelledComponent(resetPasswordFormTable, new Label("header-label", new ResourceModel("header")));
            addLabelledComponent(resetPasswordFormTable, new Label("username-label", new ResourceModel("username-label")));

            resetPasswordFormTable.add(new RequiredTextField<>("username",
                    new PropertyModel<>(this, "userId")));

            resetPasswordFormTable.add(new AttributeModifier("autocomplete", new Model<>(autoComplete ? "on" : "off")));
            resetPasswordFormTable.add(new Button("submit", new ResourceModel("submit-label")));
            add(resetPasswordFormTable);

            feedback = new FeedbackPanel("feedback");
            feedback.setOutputMarkupId(true);
            feedback.setEscapeModelStrings(false);
            add(feedback);
        }

        /**
         * Button action for password reset
         */
        @Override
        public final void onSubmit() {
            final CustomPluginUserSession userSession = CustomPluginUserSession.get();
            final Session session = userSession.getResetPasswordSession();

            boolean resetSuccess = false;
            try {
                if (resetPassword(session)) {
                    resetSuccess = true;
                }
            } catch (final PathNotFoundException ignore) {
                log.info("Unknown username: {}", userId);
                info(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
            } catch (final EmailException e) {
                log.error("Sending mail failed.", e);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            } catch (final RepositoryException re) {
                log.error("RepositoryException.", re);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            } finally {
                if (session != null) {
                    session.logout();
                    userSession.removeResetPasswordSession();
                }
            }

            if (resetSuccess) {
                // show success stuff
                resetPasswordFormTable.setVisible(false);
                info(labelMap.get(Configuration.EMAIL_SENT));
            }
        }

        /**
         * Set user id
         *
         * @param userId user id
         */
        public final void setUserId(final String userId) {
            this.userId = userId;
        }

        private boolean resetPassword(final Session session) throws RepositoryException, EmailException {

            final Node userNode;
            userNode = session.getNode(HIPPO_USERS_PATH + userId);

            // check if user is active
            boolean hippoUserActive = true;
            if (userNode.hasProperty("hipposys:active")) {
                hippoUserActive = userNode.getProperty("hipposys:active").getBoolean();
            }
            if (!hippoUserActive) {
                log.error("User is not active: {}", userId);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            }
            
            String hippoUserEmail = null;
            if (userNode.hasProperty("hipposys:email")) {
                hippoUserEmail = userNode.getProperty("hipposys:email").getString();
            }

            if (StringUtils.isEmpty(hippoUserEmail)) {
                log.error("Unknown e-mail: {}", userId);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            }

            final String code = UUID.randomUUID().toString();
            final String url = getResetPasswordUrl(code);
            final String userName = getUserName(userNode);
            final Calendar dateNow = Calendar.getInstance();
            dateNow.add(Calendar.MINUTE, urlValidityDuration);
            final Calendar expiryDate = (Calendar) dateNow.clone();

            final String mailText = getMailText(url, userName, expiryDate);

            sendEmail(hippoUserEmail, userName, mailText);
            log.debug("Sending mail link to user: {}", mailText);
            // persist code and exp.date
            // Node should be relaxed before adding extra properties
            if (userNode.canAddMixin("hippostd:relaxed")) {
                userNode.addMixin("hippostd:relaxed");
            }
            final Calendar currentDate = (Calendar) dateNow.clone();
            userNode.setProperty(PASSWORD_RESET_TIMESTAMP, currentDate);
            userNode.setProperty(PASSWORD_RESET_KEY, code);

            session.save();

            return true;
        }

        private void sendEmail(final String hippoUserEmail, final String username, final String mailText) throws EmailException {
            final MailService mailService = HippoServiceRegistry.getService(MailService.class);
            final MailMessage mailMessage = getMailMessage(hippoUserEmail, username, mailText);
            mailService.sendMail(mailMessage);
        }

        private String getMailText(final String url, final String username, final Calendar expireDate) {
            // generate text
            String mailText = labelMap.get(Configuration.EMAIL_TEXT_RESET);

            mailText = EMAIL_PARAM_NAME_PATTERN.matcher(mailText).replaceAll(username);
            mailText = EMAIL_PARAM_URL_PATTERN.matcher(mailText).replaceAll(url);
            mailText = EMAIL_PARAM_VALIDITY_PATTERN.matcher(mailText).replaceAll(new SimpleDateFormat(DATE_FORMAT).format(expireDate.getTime()));

            return mailText;
        }

        private MailMessage getMailMessage(final String hippoUserEmail, final String username, final String mailText) {
            final String fromName = labelMap.get(Configuration.EMAIL_FROM_NAME);
            final String fromEmail = labelMap.get(Configuration.EMAIL_FROM_EMAIL);
            final String subject = labelMap.get(Configuration.EMAIL_SUBJECT_RESET);

            final MailMessage mailMessage = new MailMessage();
            mailMessage.setToMail(hippoUserEmail, username);
            mailMessage.setFromMail(fromEmail, fromName);
            mailMessage.setSubject(subject);
            mailMessage.setPlainTextBody(".");
            mailMessage.setHtmlTextBody(mailText);
            return mailMessage;
        }

        private String getResetPasswordUrl(final String code) {
            final HttpServletRequest request = WebApplicationHelper.retrieveWebRequest().getContainerRequest();
            final String cmsBaseURL = HstRequestUtils.getCmsBaseURL(request);

            return cmsBaseURL + "/resetpassword?code=" + code + "&uid=" + userId;
        }

        private String getUserName(final Node userNode) throws RepositoryException {
            String hippoUserFirstName = null;
            if (userNode.hasProperty("hipposys:firstname")) {
                hippoUserFirstName = userNode.getProperty("hipposys:firstname").getString();
            }
            String hippoUserLastName = null;
            if (userNode.hasProperty("hipposys:lastname")) {
                hippoUserLastName = userNode.getProperty("hipposys:lastname").getString();
            }
            return hippoUserFirstName + SPACE + hippoUserLastName;
        }

        private void addLabelledComponent(final WebMarkupContainer container, final Component component) {
            component.setOutputMarkupId(true);
            container.add(component);
        }
    }
}