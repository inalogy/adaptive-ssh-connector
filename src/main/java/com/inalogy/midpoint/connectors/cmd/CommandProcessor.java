/*
 * Copyright (c) 2015-2020 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inalogy.midpoint.connectors.cmd;

import com.inalogy.midpoint.connectors.ssh.SshConfiguration;

import com.inalogy.midpoint.connectors.utils.Constants;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandProcessor {

    private final SshConfiguration configuration;

    public CommandProcessor(SshConfiguration configuration) {
        this.configuration = configuration;
    }

    public String process(Set<Attribute> attributes, @NotNull String scriptPath) {
        String command = buildCommand(scriptPath);

        if (attributes == null) {
            return command;
        }

        if (configuration.getArgumentStyle() == null) {
            return encodeArgumentsAndCommandToString(command, attributes, "--");
        }

        switch (configuration.getArgumentStyle()) {
            case SshConfiguration.ARGUMENT_STYLE_VARIABLES_BASH:
                return encodeArgumentsAndCommandToString(command, attributes, "--");
            case SshConfiguration.ARGUMENT_STYLE_VARIABLES_POWERSHELL:
            case SshConfiguration.ARGUMENT_STYLE_DASH:
                return encodeArgumentsAndCommandToString(command, attributes, "-");
            case SshConfiguration.ARGUMENT_STYLE_SLASH:
                return encodeArgumentsAndCommandToString(command, attributes, "/");
            default:
                throw new ConfigurationException("Unknown value of argument style: "+configuration.getArgumentStyle());
        }
    }

    private String buildCommand(String scriptPath) {
        switch (configuration.getShellType()) {
            case Constants.TYPE_SHELL:
                return "sh " + scriptPath;
            case Constants.TYPE_POWERSHELL:
                return scriptPath;
            default:
                throw new ConfigurationException("Unknown value of 'Shell Type': " + configuration.getShellType());
        }
    }

    private String encodeArgumentsAndCommandToString(String command, Set<Attribute> attributes, String paramPrefix) {
        StringBuilder commandLineBuilder = new StringBuilder();
        commandLineBuilder.append(command);

        for (Attribute attribute : attributes) {
            List<Object> values = attribute.getValue();
            boolean insertAttribute = true;

            if(values == null) {
                switch (configuration.getHandleNullValues()) {
                    case SshConfiguration.HANDLE_NULL_AS_EMPTY_STRING:
                        values = Stream.of("").collect(Collectors.toList());
                        break;
                    case SshConfiguration.HANDLE_NULL_AS_GONE:
                        insertAttribute = false;
                        break;
                    default:
                        throw new ConfigurationException("Unknown value of handleNullValues: " + configuration.getHandleNullValues());
                }
            }

            if(insertAttribute) {
                commandLineBuilder.append(" ");
                // check for special attributes
                String attributeName  = transformSpecialAttributes(attribute.getName());
                // impossible to map same attribute from midpoint to script
                commandLineBuilder.append(paramPrefix).append(attributeName);
                commandLineBuilder.append(" ");
                for (Object value : values) {
                    commandLineBuilder.append(quoteDouble(value)).append(",");
                }
                // delete the last "," at the end
                commandLineBuilder.deleteCharAt(commandLineBuilder.length() - 1);
            }
        }

        return commandLineBuilder.toString();
    }

    private @NotNull String quoteDouble(@NotNull Object value) {
        return '"' + value.toString().replaceAll("\"", "\"\"") + '"';
    }

    private String transformSpecialAttributes(String specialAttribute){
        /* Transform __NAME__ || __PASSWORD__ sent by midpoint to corresponding flags that are defined in constants
         */
        if (specialAttribute.equals(Constants.SPECIAL_CONNID_NAME)){
            return Constants.MICROSOFT_EXCHANGE_NAME_FLAG;
        } else if (specialAttribute.equals(Constants.SPECIAL_CONNID_PASSWORD)) {
            return Constants.MICROSOFT_EXCHANGE_PASSWORD_FLAG;

        }
        else {
            return specialAttribute;
        }
    }


}
