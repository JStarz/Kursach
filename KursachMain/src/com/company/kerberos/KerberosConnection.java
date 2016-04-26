package com.company.kerberos;

import com.company.core.Client;
import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.*;
import java.net.Socket;

public class KerberosConnection {

    final Socket toKerberosServer;
    final BufferedReader inStream;
    final PrintWriter outStream;

    public KerberosResponses delegate;
    public Client client;

    public KerberosConnection(String address, int port, Client client) throws Exception {
        this.client = client;
        this.toKerberosServer = new Socket(address, port);
        this.inStream = new BufferedReader(new InputStreamReader(toKerberosServer.getInputStream()));
        this.outStream = new PrintWriter(toKerberosServer.getOutputStream(), true);
    }

    public void login(String userCredentials) {
        client.parseModelFromString(userCredentials);
        outStream.println(userCredentials);
        startListen();
    }

    private void startListen() {
        final KerberosConnection self = this;
        boolean closeConnection = false;
        while (!closeConnection) {
            try {
                final JSON response = new JSON(inStream.readLine());

                switch (response.getTypeValue()) {
                    case JSONConstants.ConnectionClose:
                        final String closeDefinition = response.getValueForKey(JSONConstants.Reason);
                        delegate.closeKerberosServerConnection(self, client, closeDefinition);
                        closeConnection = true;
                        break;
                    case JSONConstants.TokenUpdate:
                        final String newToken = response.getValueForKey(JSONConstants.NewToken);
                        final String oldToken = response.getValueForKey(JSONConstants.OldToken);
                        delegate.updateKerberosServerConnection(self, client, oldToken, newToken);
                        break;
                    case JSONConstants.AuthFailed:
                        final String failDescribe = response.getValueForKey(JSONConstants.Reason);
                        delegate.wrongCredentials(self, client, failDescribe);
                        break;
                    default:
                        // TODO: add accountability for
                        break;
                }
            } catch (IOException e) {
                closeConnection = true;
                close();
            }
        }
    }

    private void close() {
        try {
            inStream.close();
        } catch (IOException e1) {
        }
        try {
            toKerberosServer.close();
        } catch (IOException e1) {
        }
        outStream.close();
        System.out.println("Connection closed with kerberos server");
    }
}
