package com.inalogy.midpoint.connectors.ssh;


import com.inalogy.midpoint.connectors.cmd.CommandProcessor;
import com.inalogy.midpoint.connectors.cmd.SessionManager;

import com.inalogy.midpoint.connectors.utils.dynamicconfig.DynamicConfiguration;
import org.identityconnectors.framework.common.objects.Attribute;
import org.testng.annotations.Test;

import java.util.Set;

public class TestClient {
    private TestProcessor testProcessor;


    private void init() {
        testProcessor = new TestProcessor();
    }

    @Test
    public void testExec1() {
        init();
        SessionManager session = new SessionManager(testProcessor.getConfiguration(), testProcessor.dynamicConfiguration);
        CommandProcessor cmd = new CommandProcessor(testProcessor.getConfiguration(), session, DynamicConfiguration.getInstance());

        Set<Attribute> attributes = AttributeProcessor.getTestAttributeSet1();

        String command = cmd.process(attributes, testProcessor.getProperties().getProperty("testScriptPath"));
        System.out.println("[testExec] INFO command: " + command);

        session.initSshClient();
        String response = session.exec(command);
        session.disposeSshClient();

        System.out.println("[testExec] INFO response: " + response);
    }

    @Test
    public void testExec2() {
        init();
        SessionManager session = new SessionManager(testProcessor.getConfiguration(), testProcessor.dynamicConfiguration);
        CommandProcessor cmd = new CommandProcessor(testProcessor.getConfiguration(),session, testProcessor.dynamicConfiguration);

        Set<Attribute> attributes = AttributeProcessor.getTestAttributeSet2();

        String command = cmd.process(attributes, testProcessor.getProperties().getProperty("testScriptPath"));
        System.out.println("[testExec] INFO command: " + command);

        session.initSshClient();
        String response = session.exec(command);
        session.disposeSshClient();

        System.out.println("[testExec] INFO response: " + response);
    }
}