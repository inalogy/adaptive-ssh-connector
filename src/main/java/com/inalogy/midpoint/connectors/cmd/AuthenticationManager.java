package com.inalogy.midpoint.connectors.cmd;

import java.io.IOException;

import com.inalogy.midpoint.connectors.ssh.SshConfiguration;

import com.evolveum.polygon.common.GuardedStringAccessor;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordUtils;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;

/**
 * This class is responsible for authentication based on configuration.
 * Currently only password authentication and pubKey without password is supported.
 */
public class AuthenticationManager {

    private final SshConfiguration configuration;
    private String hostDesc;
    private String connectionDesc;
    private static final Log LOG = Log.getLog(AuthenticationManager.class);

    public AuthenticationManager(SshConfiguration configuration) {
        this.configuration = configuration;
    }

    protected void authenticate(SSHClient ssh) {
        switch (configuration.getAuthenticationScheme()) {
            case SshConfiguration.AUTHENTICATION_SCHEME_PASSWORD:
                authenticatePassword(ssh);
                break;
            case SshConfiguration.AUTHENTICATION_SCHEME_PUBLIC_KEY:
                authenticatePublicKey(ssh);
                break;
            default:
                throw new ConfigurationException("Unknown authentication scheme '"+configuration.getAuthenticationScheme()+"'");
        }
    }

    private void authenticatePassword(SSHClient ssh) {
        GuardedString password = configuration.getPassword();
        if (password == null) {
            throw new ConfigurationException("No authentication password configured '"+configuration.getAuthenticationScheme()+"'");
        }
        LOG.ok("Authenticating to {0} using password authentication", getConnectionDesc());
        password.access( passwordChars -> {
            try {
                ssh.authPassword(configuration.getUsername(), passwordChars);
            } catch (UserAuthException e) {
                LOG.error("SSH password authentication as {0} to {1} failed: {2}", configuration.getUsername(), getHostDesc(), e.getMessage());
                throw new ConnectionFailedException("SSH password authentication as "+configuration.getUsername()+" to "+getHostDesc()+" failed: " + e.getMessage(), e);
            } catch (TransportException e) {
                LOG.error("Communication error during SSH password authentication as {0} to {1} failed: {2}", configuration.getUsername(), getHostDesc(), e.getMessage());
                throw new ConnectionFailedException("Communication error during SSH public key authentication as "+configuration.getUsername()+" to "+getHostDesc()+" failed: " + e.getMessage(), e);
            }
        });
    }

    private void authenticatePublicKey(SSHClient ssh) {
        LOG.ok("Authenticating to {0} using public key authentication", getConnectionDesc());
        try {
            GuardedStringAccessor privateKey = new GuardedStringAccessor();
            GuardedStringAccessor passphrase = new GuardedStringAccessor();

            if (configuration.getPrivateKey() != null) {
                configuration.getPrivateKey().access(privateKey);
            }
            if (configuration.getPassphrase() != null) {
                configuration.getPassphrase().access(passphrase);
            }

            if (privateKey.getClearChars() != null) {
                KeyProvider keyProvider;
                try {
                    if (passphrase.getClearChars() != null) {
                        keyProvider = ssh.loadKeys(privateKey.getClearString(), null, PasswordUtils.createOneOff(passphrase.getClearChars()));
                    } else {
                        keyProvider = ssh.loadKeys(privateKey.getClearString(), null, null);
                    }
                } catch (IOException e) {
                    throw new ConfigurationException("Error parsing private key for SSH public key authentication", e);
                }
                ssh.authPublickey(configuration.getUsername(), keyProvider);
            } else {
                ssh.authPublickey(configuration.getUsername());
            }
        } catch (UserAuthException e) {
            LOG.error(e, "SSH public key authentication as {0} to {1} failed: {2}", configuration.getUsername(), getHostDesc(), e.getMessage());
            throw new ConnectionFailedException("SSH public key authentication as "+configuration.getUsername()+" to "+getHostDesc()+" failed: " + e.getMessage(), e);
        } catch (TransportException e) {
            LOG.error(e, "Communication error during SSH public key authentication as {0} to {1} failed: {2}", configuration.getUsername(), getHostDesc(), e.getMessage());
            throw new ConnectionFailedException("Communication error during SSH public key authentication as "+configuration.getUsername()+" to "+getHostDesc()+" failed: " + e.getMessage(), e);
        }
    }

    protected String getConnectionDesc() {
        if (connectionDesc == null) {
            connectionDesc = configuration.getUsername() + "@" + getHostDesc();
        }
        return connectionDesc;
    }

    protected String getHostDesc() {
        if (hostDesc == null) {
            // TODO: port
            hostDesc = configuration.getHost();
        }
        return hostDesc;
    }
}
