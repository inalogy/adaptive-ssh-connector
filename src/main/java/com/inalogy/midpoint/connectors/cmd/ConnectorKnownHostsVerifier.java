package com.inalogy.midpoint.connectors.cmd;

import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

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
}