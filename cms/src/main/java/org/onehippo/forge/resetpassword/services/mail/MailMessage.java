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
package org.onehippo.forge.resetpassword.services.mail;

public class MailMessage {
    
    private String toMail;
    private String toName;
    private String fromMail;
    private String fromName;
    private String subject;
    private String plainTextBody;
    private String htmlTextBody;

    public String getToMail() {
        return toMail;
    }

    public void setToMail(final String toMail) {
        this.toMail = toMail;
    }

    public void setToMail(final String toMail, final String toName) {
        this.toMail = toMail;
        this.toName = toName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(final String toName) {
        this.toName = toName;
    }

    public String getFromMail() {
        return fromMail;
    }

    public void setFromMail(final String fromMail) {
        this.fromMail = fromMail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(final String fromName) {
        this.fromName = fromName;
    }

    public void setFromMail(final String fromMail, final String fromName) {
        this.fromMail = fromMail;
        this.fromName = fromName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }
    
    public String getPlainTextBody() {
        return plainTextBody;
    }

    public void setPlainTextBody(final String plainTextBody) {
        this.plainTextBody = plainTextBody;
    }

    public String getHtmlTextBody() {
        return htmlTextBody;
    }

    public void setHtmlTextBody(final String htmlTextBody) {
        this.htmlTextBody = htmlTextBody;
    }
}
