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

import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class obtaining all configurable labels, url validity and password duration.
 *
 * For labels use LabelMap, for url validity and password duration use DurationsMap.
 * To obtain a specific value, use the publicly available keys in this class.
 *
 * final Configuration configuration = new Configuration(node);
 * final Map<String, String> labelMap = configuration.getLabelMap();
 * final String emailTextReset = labelMap.get(Configuration.EMAIL_TEXT_RESET);
 */
public class Configuration {

    private final Map<String, String> labels = new HashMap<>();
    private final Map<String, Integer> durations = new HashMap<>();

    public static final String URL_VALIDITY_IN_MINUTES = "url.validity.in.minutes";
    public static final String EMAIL_TEXT_RESET = "email.text.reset";
    public static final String EMAIL_TEXT_EXPIRE = "email.text.expire";
    public static final String EMAIL_SUBJECT_RESET = "email.subject.reset";
    public static final String EMAIL_SUBJECT_EXPIRE = "email.subject.expire";
    public static final String EMAIL_FROM_EMAIL = "email.from.email";
    public static final String EMAIL_FROM_NAME = "email.from.name";
    public static final String PASSWORD_RESET_DONE = "password.reset.done";
    public static final String SYSTEM_ERROR = "system.error";
    public static final String PASSWORDS_DO_NOT_MATCH = "passwords.do.not.match";
    public static final String EMAIL_SENT = "email.sent";
    public static final String INFORMATION_INCOMPLETE = "information.incomplete";
    public static final String RESET_LINK_EXPIRED = "reset.link.expired";
    public static final String RESET_INTRO_TEXT = "reset.intro.text";
    public static final String SET_PW_INTRO_TEXT = "setpw.intro.text";
    public static final String LOGIN_LINK_TEXT = "login.link.text";
    public static final String REQUEST_NEW_LINK_TEXT = "request.new.link.text";
    public static final String DAYS_BEFORE_PASSWORD_EXPIRES = "number.of.days.before.password.expires";

    /**
     * Constructor initializing all configurations based on provided config node.
     * @param configNode config node
     * @throws RepositoryException when an expected label is not available.
     */
    public Configuration(final Node configNode) throws RepositoryException {
        mapConfigurations(configNode);
    }

    /**
     * Get Map containing all labels.
     * @return map
     */
    public Map<String,String> getLabelMap() {
        return Collections.unmodifiableMap(labels);
    }

    /**
     * Get Map containing all configurable durations.
     * @return map
     */
    public Map<String, Integer> getDurationsMap() {
        return Collections.unmodifiableMap(durations);
    }

    /**
     * For a valuelist node return the label with the specified key
     *
     * @param node of type selection:valuelist
     * @param key selection:key
     * @return selection:label
     * @throws RepositoryException when label is missing
     */
    public static String getLabel(final Node node, final String key) throws RepositoryException {
        final String value =  getKeyLabels(node).get(key);
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Key '" + key + "' is not set.");
        }
        return value;
    }

    private void mapConfigurations(final Node configNode) throws RepositoryException {
        labels.put(EMAIL_SENT, getLabel(configNode, EMAIL_SENT));
        labels.put(INFORMATION_INCOMPLETE, getLabel(configNode, INFORMATION_INCOMPLETE));
        labels.put(RESET_LINK_EXPIRED, getLabel(configNode, RESET_LINK_EXPIRED));
        labels.put(REQUEST_NEW_LINK_TEXT, getLabel(configNode, REQUEST_NEW_LINK_TEXT));
        labels.put(PASSWORDS_DO_NOT_MATCH, getLabel(configNode, PASSWORDS_DO_NOT_MATCH));
        labels.put(SYSTEM_ERROR, getLabel(configNode, SYSTEM_ERROR));
        labels.put(PASSWORD_RESET_DONE, getLabel(configNode, PASSWORD_RESET_DONE));
        labels.put(EMAIL_FROM_NAME, getLabel(configNode, EMAIL_FROM_NAME));
        labels.put(EMAIL_FROM_EMAIL, getLabel(configNode, EMAIL_FROM_EMAIL));
        labels.put(EMAIL_SUBJECT_RESET, getLabel(configNode, EMAIL_SUBJECT_RESET));
        labels.put(EMAIL_SUBJECT_EXPIRE, getLabel(configNode, EMAIL_SUBJECT_EXPIRE));
        labels.put(EMAIL_TEXT_RESET, getLabel(configNode, EMAIL_TEXT_RESET));
        labels.put(EMAIL_TEXT_EXPIRE, getLabel(configNode, EMAIL_TEXT_EXPIRE));
        labels.put(RESET_INTRO_TEXT, getLabel(configNode, RESET_INTRO_TEXT));
        labels.put(SET_PW_INTRO_TEXT, getLabel(configNode, SET_PW_INTRO_TEXT));
        labels.put(LOGIN_LINK_TEXT, getLabel(configNode, LOGIN_LINK_TEXT));

        durations.put(URL_VALIDITY_IN_MINUTES, convertLabelToInt(configNode, URL_VALIDITY_IN_MINUTES));
        durations.put(DAYS_BEFORE_PASSWORD_EXPIRES, convertLabelToInt(configNode, DAYS_BEFORE_PASSWORD_EXPIRES));
    }

    private static int convertLabelToInt(final Node configNode, final String key) throws RepositoryException {
        try {
            return Integer.parseInt(getLabel(configNode, key));
        } catch (final NumberFormatException ignored) {
            throw new IllegalArgumentException("Key '" + key + "' is not a number.");
        }
    }

    private static Map<String,String> getKeyLabels(final Node node) throws RepositoryException {
        final Map<String, String> properties = new HashMap<>();

        final NodeIterator nodeIterator = node.getNodes("selection:listitem");
        while(nodeIterator.hasNext()){
            final Node subNode = nodeIterator.nextNode();
            final String key = subNode.getProperty("selection:key").getString();
            final String label = subNode.getProperty("selection:label").getString();
            properties.put(key,label);
        }
        return properties;
    }

}