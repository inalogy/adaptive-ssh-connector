package com.inalogy.midpoint.connectors.cmd;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.inalogy.midpoint.connectors.ssh.SshConfiguration;
import com.inalogy.midpoint.connectors.utils.Constants;

import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.objects.Attribute;

import org.jetbrains.annotations.NotNull;

/**
 * The CommandProcessor class is responsible for processing sets of attributes and generating
 * corresponding commands and flags that can be understood by an SSH interface.
 */
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

    /**
     * Transform __NAME__ || __PASSWORD__ sent by midpoint to corresponding flags that are defined in constants
     * @param specialAttribute __NAME__ || __PASSWORD__
     * @return constant e.g. __NAME__ -> name
     */
    private String transformSpecialAttributes(String specialAttribute){

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
