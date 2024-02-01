package com.inalogy.midpoint.connector.ssh.cmd;

import java.io.IOException;
import java.io.StringReader;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.framework.common.exceptions.ConfigurationException;

import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;

/**
 * This class implements the HostKeyVerifier interface from the SSHJ library.
 * It provides a means to handle verification of known hosts for SSH connections.
 *
 */
public class ConnectorKnownHostsVerifier implements HostKeyVerifier  {
    private final List<OpenSSHKnownHosts.KnownHostEntry> entries = new ArrayList<>();

    public ConnectorKnownHostsVerifier parse(final String[] knownHosts) {
        if (knownHosts == null) {
            return this;
        }
        for (String knownHost : knownHosts) {
            try {
                final OpenSSHKnownHosts hosts = new OpenSSHKnownHosts(new StringReader(knownHost));
                for ( OpenSSHKnownHosts.KnownHostEntry entry : hosts.entries() ) {
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            } catch (IOException e) {
                throw new ConfigurationException("Error parsing known hosts entry "+knownHost+": "+e.getMessage(), e);
            }
        }

        return this;
    }


    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        if (entries.isEmpty()) {
            return true;
        }

        final KeyType type = KeyType.fromKey(key);
        if (type == KeyType.UNKNOWN) {
            return false;
        }

        for (OpenSSHKnownHosts.KnownHostEntry entry : entries) {
            try {
                if (entry.appliesTo(type, hostname) && entry.verify(key)) {
                    return true;
                }
            } catch (IOException e) {
                throw new ConfigurationException("Error verifying known hosts entry "+e+": "+e.getMessage(), e);
            }
        }
        return false;
    }

    @Override
    public List<String> findExistingAlgorithms(String s, int i) {
        return null;
    }
}