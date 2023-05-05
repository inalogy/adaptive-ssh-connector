package com.inalogy.midpoint.connectors.ssh;


import com.inalogy.midpoint.connectors.cmd.CommandProcessor;
import com.inalogy.midpoint.connectors.cmd.SessionProcessor;

import org.identityconnectors.framework.common.objects.Attribute;
import org.testng.annotations.Test;

import java.util.Set;

public class TestClient {
    private TestProcessor testProcessor;

    private void init() {
        testProcessor = new TestProcessor();
    }

    @Test
    public void testExec() {
        init();
        CommandProcessor cmd = new CommandProcessor(testProcessor.getConfiguration());
        SessionProcessor session = new SessionProcessor(testProcessor.getConfiguration());

        Set<Attribute> attributes = AttributeProcessor.getTestAttributeSet();

        String command = cmd.process(attributes, testProcessor.getProperties().getProperty("testScriptPath"));
        System.out.println("[testExec] INFO command: " + command);

        String response = session.exec(command);
        System.out.println("[testExec] INFO response: " + response);
    }
}