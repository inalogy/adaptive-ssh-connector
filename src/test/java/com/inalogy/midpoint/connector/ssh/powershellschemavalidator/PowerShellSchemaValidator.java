package com.inalogy.midpoint.connector.ssh.powershellschemavalidator;
import java.io.*;

public class PowerShellSchemaValidator {
    /** This class validate powershell script with current implementation of schema
     *  For this test to work every powershell script must contain $returnHeader with expected values that specify columns
     */

    protected String createScriptPath = System.getProperty("user.dir") + "/samples/createScript.ps1";
    public  String findReturnHeaderInScript(String filePath) {
        String returnHeader = "$returnHeader";
        File file = new File(filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(returnHeader)) {
                    return line;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
