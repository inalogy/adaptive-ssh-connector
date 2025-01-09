package com.inalogy.midpoint.connectors.ssh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestSshResponseHandler {

    TestProcessor testProcessor = new TestProcessor();
    private void init() {
        testProcessor = new TestProcessor();
    }

    public  String readFileAsString(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
