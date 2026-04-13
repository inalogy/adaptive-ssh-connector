package com.inalogy.midpoint.connectors.ssh.unit;

import com.inalogy.midpoint.connectors.ssh.AdaptiveSshConfiguration;
import com.inalogy.midpoint.connectors.ssh.cmd.CommandProcessor;
import com.inalogy.midpoint.connectors.ssh.cmd.SessionManager;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfiguration;
import com.inalogy.midpoint.connectors.ssh.utils.dynamicconfig.DynamicConfigurationTestBuilder;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Test(groups = "unit")
public class CommandProcessorTest {

    @AfterMethod
    public void tearDown() {
        DynamicConfigurationTestBuilder.reset();
    }

    private AdaptiveSshConfiguration config(String shellType, String argumentStyle) {
        AdaptiveSshConfiguration cfg = new AdaptiveSshConfiguration();
        cfg.setHost("localhost");
        cfg.setUsername("testuser");
        cfg.setPassword(new GuardedString("testpass".toCharArray()));
        cfg.setShellType(shellType);
        cfg.setArgumentStyle(argumentStyle);
        cfg.schemaFilePath = "dummy"; // not used by process()
        cfg.dynamicConfigurationFilePath = "dummy";
        return cfg;
    }

    private CommandProcessor processor(String shellType, String argumentStyle, DynamicConfiguration dc) {
        AdaptiveSshConfiguration cfg = config(shellType, argumentStyle);
        // SessionManager is only used for exec(), not for process() — safe to pass null-ish
        SessionManager sm = new SessionManager(cfg, dc);
        return new CommandProcessor(cfg, sm, dc);
    }

    private Set<Attribute> attrs(Attribute... attributes) {
        return new HashSet<>(Arrays.asList(attributes));
    }

    @Test
    public void testProcess_dashStyle_singleAttribute() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("email", "user@test.com"));

        String cmd = cp.process(attributes, "/scripts/search.ps1");

        Assert.assertTrue(cmd.startsWith("sh /scripts/search.ps1"));
        Assert.assertTrue(cmd.contains("-email \"user@test.com\""));
    }

    @Test
    public void testProcess_dashStyle_multipleAttributes() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(
                AttributeBuilder.build("email", "user@test.com"),
                AttributeBuilder.build("alias", "u1")
        );

        String cmd = cp.process(attributes, "/scripts/search.ps1");

        Assert.assertTrue(cmd.contains("-email \"user@test.com\""));
        Assert.assertTrue(cmd.contains("-alias \"u1\""));
    }

    @Test
    public void testProcess_slashStyle() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "slash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("email", "user@test.com"));

        String cmd = cp.process(attributes, "/scripts/search.ps1");

        Assert.assertTrue(cmd.contains("/email \"user@test.com\""));
    }

    @Test
    public void testProcess_variablesPowershell() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("powershell", "variables-powershell", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("email", "user@test.com"));

        String cmd = cp.process(attributes, "C:\\scripts\\search.ps1");

        // powershell mode: no shell prefix, just the script path
        Assert.assertTrue(cmd.startsWith("C:\\scripts\\search.ps1"));
        Assert.assertTrue(cmd.contains("-email \"user@test.com\""));
    }

    @Test
    public void testProcess_variablesBash() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "variables-bash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("email", "user@test.com"));

        String cmd = cp.process(attributes, "/scripts/search.sh");

        Assert.assertTrue(cmd.contains("--email \"user@test.com\""));
    }

    @Test
    public void testProcess_nullArgumentStyle_defaultsToDash() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", null, dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("email", "user@test.com"));

        String cmd = cp.process(attributes, "/scripts/search.sh");

        Assert.assertTrue(cmd.contains("--email \"user@test.com\""));
    }

    @Test
    public void testBuildCommand_shell() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);
        String cmd = cp.process(null, "/scripts/test.sh");
        Assert.assertEquals(cmd, "sh /scripts/test.sh");
    }

    @Test
    public void testBuildCommand_bashShell() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("bashshell", "dash", dc);
        String cmd = cp.process(null, "/scripts/test.sh");
        Assert.assertEquals(cmd, "bash /scripts/test.sh");
    }

    @Test
    public void testBuildCommand_kornShell() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("kornshell", "dash", dc);
        String cmd = cp.process(null, "/scripts/test.sh");
        Assert.assertEquals(cmd, "ksh /scripts/test.sh");
    }

    @Test
    public void testBuildCommand_cshell() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("cshell", "dash", dc);
        String cmd = cp.process(null, "/scripts/test.sh");
        Assert.assertEquals(cmd, "csh /scripts/test.sh");
    }

    @Test
    public void testBuildCommand_powershell() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("powershell", "dash", dc);
        String cmd = cp.process(null, "C:\\scripts\\test.ps1");
        Assert.assertEquals(cmd, "C:\\scripts\\test.ps1");
    }

    @Test(expectedExceptions = ConfigurationException.class)
    public void testBuildCommand_unknownShell_throws() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("zsh", "dash", dc);
        cp.process(null, "/scripts/test.sh");
    }

    @Test
    public void testBuildCommand_withSudo() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildWithSudo("sudo");
        CommandProcessor cp = processor("shell", "dash", dc);

        String cmd = cp.process(null, "/scripts/test.sh");
        Assert.assertEquals(cmd, "sudo sh /scripts/test.sh");
    }

    @Test
    public void testBuildCommand_withDoas() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildWithSudo("doas");
        CommandProcessor cp = processor("shell", "dash", dc);

        String cmd = cp.process(null, "/scripts/test.sh");
        Assert.assertEquals(cmd, "doas sh /scripts/test.sh");
    }

    @Test
    public void testProcess_transformsName() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);
        // __NAME__ should be transformed to "name" (from icfsNameFlagEquivalent)
        Set<Attribute> attributes = attrs(AttributeBuilder.build("__NAME__", "testuser"));

        String cmd = cp.process(attributes, "/scripts/search.sh");

        Assert.assertTrue(cmd.contains("-name \"testuser\""));
        Assert.assertFalse(cmd.contains("__NAME__"));
    }

    @Test
    public void testProcess_transformsUid_whenEnabled() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildWithUidFlag("guid");
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("__UID__", "abc-123"));

        String cmd = cp.process(attributes, "/scripts/search.sh");

        Assert.assertTrue(cmd.contains("-guid \"abc-123\""));
        Assert.assertFalse(cmd.contains("__UID__"));
    }

    @Test
    public void testProcess_nameTransformDisabled_keepsOriginal() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildWithNameFlagDisabled();
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("__NAME__", "testuser"));

        String cmd = cp.process(attributes, "/scripts/search.sh");

        Assert.assertTrue(cmd.contains("-__NAME__ \"testuser\""));
    }

    @Test
    public void testProcess_transformsPassword() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildOpenBsd();
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(
                AttributeBuilder.build("__PASSWORD__", new GuardedString("secret123".toCharArray()))
        );

        String cmd = cp.process(attributes, "/scripts/create.sh");

        Assert.assertTrue(cmd.contains("-password \"secret123\""));
        Assert.assertFalse(cmd.contains("__PASSWORD__"));
    }

    @Test
    public void testProcess_multivaluedAttribute_commaSeparated() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(
                AttributeBuilder.build("type", Arrays.asList("user", "person", "admin"))
        );

        String cmd = cp.process(attributes, "/scripts/create.sh");

        Assert.assertTrue(cmd.contains("-type \"user\",\"person\",\"admin\""));
    }

    @Test
    public void testProcess_whitespaceReplacement_enabled() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildOpenBsd(); // whitespace replacement enabled
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("fullName", "John Doe"));

        String cmd = cp.process(attributes, "/scripts/update.sh");

        Assert.assertTrue(cmd.contains("\"John---Doe\""));
    }

    @Test
    public void testProcess_whitespaceReplacement_disabled() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic(); // whitespace replacement disabled
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("fullName", "John Doe"));

        String cmd = cp.process(attributes, "/scripts/update.sh");

        Assert.assertTrue(cmd.contains("\"John Doe\""));
    }

    @Test
    public void testProcess_doubleQuotesInValue_escaped() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("desc", "He said \"hello\""));

        String cmd = cp.process(attributes, "/scripts/update.sh");

        Assert.assertTrue(cmd.contains("\"He said \"\"hello\"\"\""));
    }

    @Test
    public void testProcess_emptyMarkerValue_sentAsAttribute() {
        // In the real connector, updateDelta() sends the emptyAttribute marker as the value
        // when an attribute is set to null: AttributeBuilder.build(name, emptyValue)
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic(); // emptyAttr = "null"
        CommandProcessor cp = processor("shell", "dash", dc);
        Set<Attribute> attributes = attrs(AttributeBuilder.build("email", "null"));

        String cmd = cp.process(attributes, "/scripts/update.sh");

        Assert.assertTrue(cmd.contains("-email \"null\""));
    }

    @Test
    public void testProcess_nullAttributeSet_returnsJustCommand() {
        DynamicConfiguration dc = DynamicConfigurationTestBuilder.buildExchangeBasic();
        CommandProcessor cp = processor("shell", "dash", dc);

        String cmd = cp.process(null, "/scripts/search.sh");

        Assert.assertEquals(cmd, "sh /scripts/search.sh");
    }
}
