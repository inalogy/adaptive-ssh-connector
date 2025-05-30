package com.inalogy.midpoint.connectors.ssh.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.ConnectorSettings;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.schema.SchemaType;
import com.inalogy.midpoint.connectors.ssh.utils.Constants;

import org.identityconnectors.common.security.GuardedString;
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
    private final AdaptiveSshConfiguration configuration;
    private DynamicConfiguration dynamicConfiguration;

    public CommandProcessor(AdaptiveSshConfiguration configuration, SessionManager sshManager, DynamicConfiguration dynamicConfiguration) {
        this.configuration = configuration;
        this.sshManager = sshManager;
        this.dynamicConfiguration = dynamicConfiguration;
    }

    /**
     * process Attributes based on {@link AdaptiveSshConfiguration#getArgumentStyle()}
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
            case AdaptiveSshConfiguration.ARGUMENT_STYLE_VARIABLES_BASH:
                return encodeArgumentsAndCommandToString(command, attributes, "--");
            case AdaptiveSshConfiguration.ARGUMENT_STYLE_VARIABLES_POWERSHELL:
            case AdaptiveSshConfiguration.ARGUMENT_STYLE_DASH:
                return encodeArgumentsAndCommandToString(command, attributes, "-");
            case AdaptiveSshConfiguration.ARGUMENT_STYLE_SLASH:
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
        ConnectorSettings sudoSettings = dynamicConfiguration.getSettings().getConnectorSettings();
        String sudoCommand = "";
        if (sudoSettings.getAddSudoExecution().isEnabled()){
            sudoCommand = sudoSettings.getAddSudoExecution().getValue() + " ";
        }
        switch (configuration.getShellType()) {
            case Constants.TYPE_SHELL:
                return sudoCommand + "sh " + scriptPath;
            case Constants.TYPE_CSH_SHELL:
                return sudoCommand + "csh " + scriptPath;
            case Constants.TYPE_KORN_SHELL:
                return sudoCommand + "ksh " + scriptPath;
            case Constants.TYPE_BASH_SHELL:
                return sudoCommand + "bash " + scriptPath;

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
     * Special attribute names are handled in this method, if midpoint send __NAME__ it gets matched to appropriate flag defined in dynamicConfiguration.
     */
    private String encodeArgumentsAndCommandToString(String command, Set<Attribute> attributes, String paramPrefix) {
        StringBuilder commandLineBuilder = new StringBuilder();
        commandLineBuilder.append(command);
        ConnectorSettings passwordConfiguration = this.dynamicConfiguration.getSettings().getConnectorSettings();

        for (Attribute attribute : attributes) {
                List<Object> values = attribute.getValue();

            if(values.isEmpty()) {
                //if midpoint sends replace attr with null we need to set corresponding constant that need to be handled by the remote script
                values.add(this.dynamicConfiguration.getScriptEmptyAttribute());
            }

            commandLineBuilder.append(" ");
            // check for special attributes
            String attributeName  = transformSpecialAttributes(attribute.getName());
            // impossible to map same attribute from midpoint to script
            commandLineBuilder.append(paramPrefix).append(attributeName);
            commandLineBuilder.append(" ");
            for (Object value : values) {
                if (passwordConfiguration.getIcfsPasswordFlagEquivalent().isEnabled() && attributeName.equals(passwordConfiguration.getIcfsPasswordFlagEquivalent().getValue())){
                    value = passwordAccessor((GuardedString) value);
                }
                commandLineBuilder.append(quoteDouble(value)).append(",");
            }
            // delete the last "," at the end
            commandLineBuilder.deleteCharAt(commandLineBuilder.length() - 1);
        }

        return commandLineBuilder.toString();
    }



    private String passwordAccessor(GuardedString guardedPassword) {

        List<String> passwordList = new ArrayList<>(1);
        if (guardedPassword != null) {
            guardedPassword.access(new GuardedString.Accessor() {
                @Override
                public void access(char[] chars) {
                    passwordList.add(new String(chars));
                }
            });
//            return passwordList.get(0);
        }
        if (!passwordList.isEmpty()) {
            return passwordList.get(0);
        }
        return null;
    }

    private @NotNull String quoteDouble(@NotNull Object value) {
        String val = value.toString();
        if (this.dynamicConfiguration.isReplaceWhiteSpaceEnabled() && val.contains(" ")){
            val = val.replace(" ", this.dynamicConfiguration.getReplaceWhiteSpaceValue());
        }
        return '"' + val.replaceAll("\"", "\"\"") + '"';
    }

    /**
     * Transform __NAME__ || __PASSWORD__ || __UID__ sent by midpoint to corresponding flags that are defined in resource configuration
     * @param specialAttribute __NAME__ || __PASSWORD__
     * @return corresponding flag from resource configuration e.g. __NAME__ -> name
     */
    private String transformSpecialAttributes(String specialAttribute) {
        ConnectorSettings connectorSettings = this.dynamicConfiguration.getSettings().getConnectorSettings();

        if (connectorSettings.getIcfsNameFlagEquivalent().isEnabled() && specialAttribute.equals(Constants.SPECIAL_CONNID_NAME)) {
            return connectorSettings.getIcfsNameFlagEquivalent().getValue();
        } else if (connectorSettings.getIcfsUidFlagEquivalent().isEnabled() && specialAttribute.equals(Constants.SPECIAL_CONNID_UID)) {
            return connectorSettings.getIcfsUidFlagEquivalent().getValue();
        } else if (connectorSettings.getIcfsPasswordFlagEquivalent().isEnabled() && specialAttribute.equals(Constants.SPECIAL_CONNID_PASSWORD)) {
            return connectorSettings.getIcfsPasswordFlagEquivalent().getValue();
        }

        return specialAttribute;
    }


    /**
     *
     * @param attributes that need to be transformed to appropriate flags
     * @param operationName type of operation defined in Constants
     * @param schemaType corresponding schemaType objet of currently processed entity
     * @return String response
     */
    public String processAndExecuteCommand(Set<Attribute> attributes, String operationName, SchemaType schemaType){
        String operationScript = switch (operationName) {
            case Constants.SEARCH_OPERATION -> schemaType.getSearchScript();
            case Constants.UPDATE_OPERATION -> schemaType.getUpdateScript();
            case Constants.CREATE_OPERATION -> schemaType.getCreateScript();
            case Constants.DELETE_OPERATION -> schemaType.getDeleteScript();
            default -> {
                LOG.error("Unsupported operation");
                throw new RuntimeException("Unsupported operation");
            }
        };
        String sshProcessedCommand = process(attributes, operationScript);

        long start = System.nanoTime();
        String result = this.sshManager.exec(sshProcessedCommand);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        LOG.ok("commandExecutionTime: [{0}] took {1} ms", operationName, durationMs);
        return result;
    }
}
