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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.forge.resetpassword.services.mail.MailMessage;
import org.onehippo.forge.resetpassword.services.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hippoecm.frontend.util.RequestUtils.getFarthestRequestScheme;

/**
 * ResetPasswordPanel
 * Panel on ResetPasswordFrame, showing only a field for entering the username
 * <p>
 * Based on SimpleLoginPlugin and LoginPlugin.
 */
public class ResetPasswordPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResetPasswordPanel.class);

    private static final String PASSWORD_RESET_KEY = "passwordResetKey";
    private static final String PASSWORD_RESET_TIMESTAMP = "passwordResetTimestamp";
    private static final String HIPPO_USERS_PATH = "/hippo:configuration/hippo:users/";

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

        add(CssClass.append("hippo-login-panel-center"));
        add(new ResetPasswordForm(panelInfo.isAutoComplete(), panelInfo.getUserId(), panelInfo.getConfiguration()));
    }

    /**
     * Inner class for form on panel
     */
    protected class ResetPasswordForm extends Form {

        private static final long serialVersionUID = 1L;

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
                LOGGER.info("Unknown username: " + userId);
                info(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
            } catch (final EmailException e) {
                LOGGER.error("Sending mail failed.", e);
                error(labelMap.get(Configuration.SYSTEM_ERROR));
            } catch (final RepositoryException re) {
                LOGGER.error("RepositoryException.", re);
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
                LOGGER.error("User is not active: " + userId);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            }

            String hippoUserEmail = null;
            if (userNode.hasProperty("hipposys:email")) {
                hippoUserEmail = userNode.getProperty("hipposys:email").getString();
            }

            if (StringUtils.isEmpty(hippoUserEmail)) {
                LOGGER.error("Unknown e-mail: " + userId);
                error(labelMap.get(Configuration.INFORMATION_INCOMPLETE));
                return false;
            }

            final String code = UUID.randomUUID().toString();
            final String username = getUserName(userNode);

            final Calendar dateNow = Calendar.getInstance();
            final Calendar currentDate = (Calendar) dateNow.clone();
            final String mailText = getMailText(session, code, username, dateNow);

            sendEmail(hippoUserEmail, username, mailText);

            // persist code and exp.date
            // Node should be relaxed before adding extra properties
            if (userNode.canAddMixin("hippostd:relaxed")) {
                userNode.addMixin("hippostd:relaxed");
            }
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

        private String getMailText(final Session session, final String code, final String username, final Calendar dateNow) throws RepositoryException {
            // set expire date (used in email)
            dateNow.add(Calendar.MINUTE, urlValidityDuration);
            final Calendar expireDate = (Calendar) dateNow.clone();

            final String url = getUrl(session, code);

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

        private String getUrl(final Session session, final String code) {
            String frontendHostName = getLocationHeaderOrigin();

            return frontendHostName + getRequest().getContextPath() +
                    "/resetpassword?code=" +
                    code +
                    "&uid=" +
                    userId;
        }

        /**
         * Creates a RFC-6454 comparable origin from the {@code request} requested resource.
         *
         * // stole logic from org.hippoecm.frontend.http.CsrfPreventionRequestCycleListener#getLocationHeaderOrigin(javax.servlet.http.HttpServletRequest)
         *
         * @return only the scheme://host[:port] part, or {@code null} when the origin string is not
         *         compliant
         */
        private String getLocationHeaderOrigin() {
            HttpServletRequest request = WebApplicationHelper.retrieveWebRequest().getContainerRequest();

            String host = request.getHeader("X-Forwarded-Host");
            if (host != null) {
                String[] hosts = host.split(",");
                final String location = getFarthestRequestScheme(request) + "://" + hosts[0];
                LOGGER.debug("X-Forwarded-Host header found. Return location '{}'", location);
                return location;
            }

            host = request.getHeader("Host");
            if (host != null && !"".equals(host)) {
                final String location = getFarthestRequestScheme(request) + "://" + host;
                LOGGER.debug("Host header found. Return location '{}'", location);
                return location;
            }

            // Build scheme://host:port from request
            String scheme = request.getScheme();
            if (scheme == null) {
                return null;
            } else {
                scheme = scheme.toLowerCase(Locale.ENGLISH);
            }

            host = request.getServerName();
            if (host == null) {
                return null;
            }

            StringBuilder target = new StringBuilder();
            target.append(scheme)
                    .append("://")
                    .append(host);

            int port = request.getServerPort();
            if ("http".equals(scheme) && port != 80 || "https".equals(scheme) && port != 443) {
                target.append(':')
                .append(port);
            }
            LOGGER.debug("Host '{}' from request.serverName is used because no 'Host' or 'X-Forwarded-Host' header found. " +
                    "Return location '{}'", target.toString());
            return target.toString();
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