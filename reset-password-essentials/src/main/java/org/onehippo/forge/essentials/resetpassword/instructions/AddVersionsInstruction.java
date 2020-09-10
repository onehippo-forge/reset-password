/*
 * Copyright 2020 Bloomreach Inc. (https://www.bloomreach.com)
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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.onehippo.cms7.essentials.plugin.sdk.utils.MavenModelUtils;
import org.onehippo.cms7.essentials.sdk.api.install.Instruction;
import org.onehippo.cms7.essentials.sdk.api.model.Module;
import org.onehippo.cms7.essentials.sdk.api.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class AddVersionsInstruction implements Instruction {

    private static final Logger log = LoggerFactory.getLogger(AddVersionsInstruction.class);

    /*
        NOTE: resetpassword.version is inserted  by maven resource plugin
    */

    @Inject
    private ProjectService projectService;

    @Override
    public Status execute(final Map<String, Object> parameters) {
        final File pom = projectService.getPomPathForModule(Module.PROJECT).toFile();
        final Model pomModel = MavenModelUtils.readPom(pom);
        if (pomModel == null) {
            return Status.FAILED;
        }
        final Properties properties = pomModel.getProperties();

            final String key = "bloomreach.forge.reset-password.version";
            final String value = readValueFromText();
            if (!Strings.isNullOrEmpty(key) && !Strings.isNullOrEmpty(value)) {
                properties.setProperty(key, value);
            }

        MavenModelUtils.writePom(pomModel, pom);
        return Status.SUCCESS;
    }

    private String readValueFromText() {
        try (final InputStream stream = getClass().getResourceAsStream("/plugin-version.txt")){
            final StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
            return writer.toString();
        } catch (IOException e) {
            log.error("Error: {}", e);
        }
        return "reset-password.version";
    }

    @Override
    public void populateChangeMessages(final BiConsumer<Type, String> changeMessageQueue) {
        changeMessageQueue.accept(Type.EXECUTE, "Add versions property");
    }
}