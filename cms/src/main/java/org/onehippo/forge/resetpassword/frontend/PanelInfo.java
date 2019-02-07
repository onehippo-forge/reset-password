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

import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;

/**
 * Container class containing all panel information.
 */
public class PanelInfo {
    private final boolean autoComplete;
    private final String userId;
    private final Configuration configuration;

    private final IPluginContext context;
    private final IPluginConfig config;

    public PanelInfo(final boolean autoComplete, final String userId, final Configuration configuration, final IPluginContext context, final IPluginConfig config) {
        this.autoComplete = autoComplete;
        this.userId = userId;
        this.configuration = configuration;
        this.context = context;
        this.config = config;
    }

    public boolean isAutoComplete() {
        return autoComplete;
    }

    public String getUserId() {
        return userId;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public IPluginContext getContext() {
        return context;
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public IPluginConfig getConfig() {
        return config;
    }

}
