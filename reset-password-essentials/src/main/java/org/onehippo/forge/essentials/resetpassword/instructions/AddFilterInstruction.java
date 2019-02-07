/*
 * Copyright 2019 BloomReach Inc. (https://www.bloomreach.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.forge.essentials.resetpassword.instructions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.onehippo.cms7.essentials.sdk.api.install.Instruction;
import org.onehippo.cms7.essentials.sdk.api.model.Module;
import org.onehippo.cms7.essentials.sdk.api.service.WebXmlService;

public class AddFilterInstruction implements Instruction {

    private static final String FILTER_CLASS = "org.apache.wicket.protocol.http.WicketFilter";
    private static final String FILTER_NAME = "ResetPassword";
    private static final List<String> URL_PATTERNS = Collections.singletonList("/resetpassword/*");
    private static final List<WebXmlService.Dispatcher> DISPATCHERS = Arrays.asList(WebXmlService.Dispatcher.REQUEST, WebXmlService.Dispatcher.FORWARD);
    private static final Module MODULE = Module.CMS;
    private static final Map<String, String> initParams = new HashMap<>();

    static {
        initParams.put("applicationClassName", "org.onehippo.forge.resetpassword.frontend.ResetPasswordMain");
        initParams.put("config", "resetpassword");
        initParams.put("wicket.configuration", "deployment");
        initParams.put("repository-address", "vm://");
    }

    @Inject
    private WebXmlService webXmlService;

    @Override
    public Status execute(final Map<String, Object> parameters) {
        if (webXmlService.addFilter(MODULE, FILTER_NAME, FILTER_CLASS, initParams)
                && webXmlService.insertFilterMapping(MODULE, FILTER_NAME, URL_PATTERNS, "CMS")
                && webXmlService.addDispatchersToFilterMapping(MODULE, FILTER_NAME, DISPATCHERS)
                && webXmlService.addDispatchersToFilterMapping(MODULE, "CMS", DISPATCHERS)) {
            return Status.SUCCESS;
        }
        return Status.FAILED;
    }

    @Override
    public void populateChangeMessages(final BiConsumer<Type, String> changeMessageQueue) {
        changeMessageQueue.accept(Type.EXECUTE, "Install ResetPassword filter into CMS web.xml.");
    }
}
