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
package org.onehippo.forge.resetpassword.services.mail;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.onehippo.repository.modules.ProvidesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@ProvidesService(types = MailService.class)
public class EmailModule extends AbstractReconfigurableDaemonModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailModule.class);
    private String mailSessionName;
    private MailService mailService;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        if(moduleConfig.hasProperty("mail.jndi.resource.name")) {
            mailSessionName = moduleConfig.getProperty("mail.jndi.resource.name").getString();
        }
        // in case the mailservice already is initialized
        if(mailService!=null) {
            mailService.setMailSessionName(mailSessionName);
        }
    }

    @Override
    protected void doInitialize(final Session session) throws RepositoryException {
        LOGGER.info("Initialized mail module");
        mailService = new MailServiceImpl();
        mailService.setMailSessionName(mailSessionName);
        HippoServiceRegistry.registerService(mailService, MailService.class);
    }

    @Override
    protected void doShutdown() {
        HippoServiceRegistry.unregisterService(mailService, MailService.class);
    }
}
