package com.company.kerberos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class KerberosServer implements KerberosTokenExpired, KerberosAuthenticationFinished {

    final int serverPort;
    final ServerSocket serverSocket;

    ConcurrentHashMap<KerberosToken, KerberosClient> clients;
    KerberosAuthentication authentication;

    public KerberosServer(int serverPort, long refreshTokenTime, KerberosTokenUpdateType updateTokenType) throws IOException {
        System.out.println("KERBEROS: start server");

        this.serverPort = serverPort;
        this.serverSocket = new ServerSocket(serverPort);
        this.clients = new ConcurrentHashMap<>();
        this.authentication = new KerberosAuthentication(this, 3);

        runAcceptThread(refreshTokenTime, updateTokenType);
    }

    private void runAcceptThread(long refreshTokenTime, KerberosTokenUpdateType updateTokenType) {
        System.out.println("KERBEROS: start accept thread");

        new Thread() {
            @Override
            public void run() {
                super.run();
                System.out.println("KERBEROS: accept thread started");
                try {
                    while(true) {
                        System.out.println("KERBEROS: wait a client");
                        final Socket newClientSocket = serverSocket.accept();
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    System.out.println("KERBEROS: new client accepted");
                                    final KerberosClient newClient = new KerberosClient(newClientSocket);
                                    System.out.println("KERBEROS: new client starting auth");
                                    authentication.auth(newClient, refreshTokenTime, updateTokenType);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void tokenExpired(KerberosToken token) {
        final KerberosClient client = clients.get(token);

        switch (token.type) {
            case UPDATE:
                // TODO: add accountability for removing client
                if (client != null) {
                    KerberosToken newToken = new KerberosToken(token.ttl, KerberosTokenUpdateType.UPDATE);
                    client.updateConnection(token, newToken);
                    clients.remove(token);
                    clients.put(newToken, client);
                }
                break;

            case CLOSE:
                // TODO: add accountability for removing client
                if (client != null) {
                    client.closeConnection(KerberosConstants.Expired);
                    clients.remove(token, client);
                }
                break;

            default:
                // TODO: add accountability for removing client
                if (client != null) {
                    client.closeConnection(KerberosConstants.BadToken);
                    clients.remove(token, client);
                }
                break;
        }

    }

    @Override
    public void authSuccessful(KerberosToken newToken, KerberosClient newClient) {
        // TODO: add accountability about user successful log in
        if (newToken != null) {
            newToken.delegate = this;
            newClient.isAuth = true;

            clients.put(newToken, newClient);
            newClient.updateConnection(null, newToken);
        }
    }

    @Override
    public void authUnsuccessful(KerberosClient client) {
        // TODO: add accountability about user unsuccessful log in
        client.isAuth = false;
        client.closeConnection("Bad credentials or used very much retries!");
    }
}
