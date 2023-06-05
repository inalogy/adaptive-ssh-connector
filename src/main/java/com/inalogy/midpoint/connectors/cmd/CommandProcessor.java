package com.inalogy.midpoint.connectors.cmd;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.inalogy.midpoint.connectors.schema.SchemaType;
import com.inalogy.midpoint.connectors.ssh.SshConfiguration;
import com.inalogy.midpoint.connectors.utils.Constants;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.objects.Attribute;

import org.jetbrains.annotations.NotNull;

/**
 * The CommandProcessor class is responsible for processing sets of attributes and generating
 * corresponding commands and flags that can be understood by an SSH interface.
 * @author Frantisek Mikus
 * @since 1.0
 */
public class CommandProcessor {

    private static final Log LOG = Log.getLog(CommandProcessor.class);
    private final SessionManager sshManager;
    private final SshConfiguration configuration;

    public CommandProcessor(SshConfiguration configuration, SessionManager sshManager) {
        this.configuration = configuration;
        this.sshManager = sshManager;
    }

    /**
     * process Attributes based on {@link SshConfiguration#getArgumentStyle()}
     * @param attributes set of attributes that need to be formatted with appropriate flags
     * @param scriptPath of currently processed Object
     * @return processed string with scriptPath and attributes
     */
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

    /**
     * build command based on configuration ShellType
     * @param scriptPath which is defined in SchemaType for particular object
     */
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

    /**
     * encodeArguments and Commands to String with paramPrefix.
     * Each attribute name represent flag that should be accepted by remote script
     * Each attribute value define value of attribute associated with name
     * Multivalued attributes are separated by "," .
     * Special attribute names are handled in this method, if midpoint send __NAME__ it gets matched to appropriate flag defined in constants.
     */
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
     * @return constant e.g. __NAME__ -> {@link Constants#MICROSOFT_EXCHANGE_NAME_FLAG}
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

    /**
     *
     * @param attributes that need to be transformed to appropriate flags
     * @param operationName type of operation defined in Constants
     * @param schemaType corresponding schemaType objet of currently processed entity
     * @return String response
     */
    public String processAndExecuteCommand(Set<Attribute> attributes, String operationName, SchemaType schemaType){
        String operationScript;
        switch (operationName){
            case Constants.SEARCH_OPERATION:
                operationScript = schemaType.getSearchScript();
                break;
            case Constants.UPDATE_OPERATION:
                operationScript = schemaType.getUpdateScript();
                break;
            case Constants.CREATE_OPERATION:
                operationScript = schemaType.getCreateScript();
                break;
            case Constants.DELETE_OPERATION:
                operationScript = schemaType.getDeleteScript();
                break;
            default:
                LOG.error("Unsupported operation");
                throw new RuntimeException("Unsupported operation");
        }
        String sshProcessedCommand = process(attributes, operationScript);
        return this.sshManager.exec(sshProcessedCommand);
    }
}
