package com.company.kerberos;

import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.*;
import java.net.Socket;

public class KerberosClient {

    final Socket clientSocket;
    final BufferedReader inStream;
    final PrintWriter outStream;

    boolean isAuth = false;
    String username;
    String password;

    public KerberosClient(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.outStream = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void closeConnection(String closeDefinition) {
        try {
            JSON json = new JSON();
            json.addTypeContent(JSONConstants.ConnectionClose);
            json.addBodyContent(JSONConstants.Reason, closeDefinition);

            outStream.println(json.getStringRepresentation());
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateConnection(KerberosToken oldToken, KerberosToken newToken) {
        JSON json = new JSON();
        json.addTypeContent(JSONConstants.TokenUpdate);
        json.addBodyContent(JSONConstants.NewToken, newToken.toString());
        if (oldToken != null) json.addBodyContent(JSONConstants.OldToken, oldToken.toString());

        outStream.println(json.getStringRepresentation());
    }

    public void wrongCredentials(int retriesCount) {
        if (retriesCount == 0) closeConnection("No such retries. Connection close.");
        else {
            final JSON wrongCredentials = new JSON();
            wrongCredentials.addTypeContent(JSONConstants.AuthFailed);
            wrongCredentials.addBodyContent(JSONConstants.Reason, "Wrong username or password! You have " + retriesCount + " retries");
            outStream.println(wrongCredentials.getStringRepresentation());
        }
    }
}
