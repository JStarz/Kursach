package com.company.kerberos;

import com.company.core.Client;

public interface KerberosResponses {
    void closeKerberosServerConnection(KerberosConnection connection, Client client, String closeDefinition);
    void updateKerberosServerConnection(KerberosConnection connection, Client client, String oldToken, String newToken);
    void wrongCredentials(KerberosConnection connection, Client client, String wrongDescription);
}
