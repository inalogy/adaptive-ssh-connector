package com.inalogy.midpoint.connectors.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class FileHashCalculator {
    public static String calculateSHA256(String filePath)  {
        if (filePath == null || filePath.isEmpty()){
            return null;
        }
        Path path = Paths.get(filePath);
        MessageDigest sha256Digest;
        try{
            //TODO need to check if SHA-256 is supported
            sha256Digest = MessageDigest.getInstance("SHA-256");}
        catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 missing on target system");
        }
        if (sha256Digest != null){

        try (InputStream is = Files.newInputStream(path);
             DigestInputStream dis = new DigestInputStream(is, sha256Digest)) {
            byte[] buffer = new byte[4096];
            while (dis.read(buffer) != -1) {
            }
            byte[] hashBytes = sha256Digest.digest();

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (IOException e){
            System.out.println("IOException in FileHashCalculator: " + e);
        }
    }
        throw new RuntimeException("SHA-256 missing on target system");
    }
}