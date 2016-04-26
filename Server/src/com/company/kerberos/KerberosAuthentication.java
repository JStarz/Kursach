package com.company.kerberos;

import com.company.dao.DatabaseUtils;
import com.company.json.JSON;
import com.company.json.JSONConstants;

interface KerberosAuthenticationFinished {
    void authSuccessful(KerberosToken newToken, KerberosClient client);
    void authUnsuccessful(KerberosClient client);
}

public class KerberosAuthentication {

    KerberosAuthenticationFinished delegate;
    int retries = 3;

    public KerberosAuthentication(KerberosAuthenticationFinished delegate, int retries) {
        this.delegate = delegate;
        this.retries = retries;
    }

    public void auth(KerberosClient client, long refreshTokenTime, KerberosTokenUpdateType updateTokenType) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                System.out.println("AUTH: auth thread started");

                KerberosToken token = null;
                boolean authSuccessful = false;

                try {
                    for (int i = 0; i < retries; i++) {
                        System.out.println("AUTH: wait user credentials (retries = " + (i + 1) + ")");
                        final JSON userCredentials = new JSON(client.inStream.readLine());
                        if (userCredentials.getTypeValue().equals(JSONConstants.Auth)) {
                            System.out.println("AUTH: good json, start checking user in db");
                            final String name = userCredentials.getValueForKey(JSONConstants.Name),
                                    password = userCredentials.getValueForKey(JSONConstants.Password);
                            if (DatabaseUtils.checkUser(name, password)) {
                                System.out.println("AUTH: good user, start creating token");
                                token = new KerberosToken(refreshTokenTime, updateTokenType);
                                authSuccessful = true;
                                client.username = name;
                                client.password = password;
                                break;
                            } else {
                                client.wrongCredentials(retries - i - 1);
                            }
                        } else System.out.println("AUTH: bad json");
                    }

                } catch (Exception e) {
                    authSuccessful = false;
                }

                if (delegate != null) {
                    if (authSuccessful) {
                        System.out.println("AUTH: successful");
                        delegate.authSuccessful(token, client);
                    } else {
                        System.out.println("AUTH: unsuccessful");
                        delegate.authUnsuccessful(client);
                    }
                }
            }
        }.run();
    }
}
