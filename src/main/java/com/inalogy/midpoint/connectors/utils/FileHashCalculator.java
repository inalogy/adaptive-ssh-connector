package com.inalogy.midpoint.connectors.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.identityconnectors.common.logging.Log;

/**
 * The FileHashCalculator class provides functionality to compute
 * the hash of a file. It uses SHA-256 hash algorithm
 */
public class FileHashCalculator {
    private static final Log LOG = Log.getLog(FileHashCalculator.class);
    public static String calculateSHA256(String filePath)  {
        if (filePath == null || filePath.isEmpty()){
            return null;
            //When loading schema first time this condition occurs
        }
        Path path = Paths.get(filePath);

        if (!Files.exists(path)){
            throw new RuntimeException("Filepath for schemaConfig is invalid. Path: " + filePath);
        }

        MessageDigest sha256Digest;
        try{
            sha256Digest = MessageDigest.getInstance("SHA-256");}
        catch (NoSuchAlgorithmException e){
            LOG.error("SHA-256 missing on target system {0}", e.getMessage());
            throw new RuntimeException("SHA-256 missing on target system {0}", e);
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
            LOG.error("IOException in FileHashCalculator: {0}", e.getMessage());
        }
    }
        throw new RuntimeException("Error occurred while calculating hash of schemaConfig.json");
    }
}