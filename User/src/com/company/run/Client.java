package com.company.run;

import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    final Socket clientSocket;
    final BufferedReader inStream;
    final PrintWriter outStream;

    private String token;

    public Client(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.outStream = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void close() throws IOException {
        inStream.close();
        outStream.close();
        clientSocket.close();
    }

    public boolean auth(String name, String password) throws IOException {
        JSON json = new JSON();
        json.addTypeContent(JSONConstants.Auth);
        json.addBodyContent(JSONConstants.Name, name);
        json.addBodyContent(JSONConstants.Password, password);

        outStream.println(json.getStringRepresentation());

        JSON newJson = new JSON(inStream.readLine());
        if (newJson.getTypeValue().equals(JSONConstants.AuthFailed)) {
            System.out.println("Auth failed: " + newJson.getValueForKey(JSONConstants.Reason));
            return false;
        } else if (newJson.getTypeValue().equals(JSONConstants.ConnectionClose)) {
            System.out.println("Connection close: " + newJson.getValueForKey(JSONConstants.Reason));
            return false;
        } else if (newJson.getTypeValue().equals(JSONConstants.TokenUpdate)) {
            token = newJson.getValueForKey(JSONConstants.NewToken);
            System.out.println("==============================================");
            System.out.println("Auth successful");
            System.out.println("New token: " + newJson.getValueForKey(JSONConstants.NewToken));
            System.out.println("==============================================");
            return true;
        }

        return false;
    }

    public boolean readResource(String name) throws IOException {
        final JSON getResource = new JSON();
        getResource.addBodyContent(JSONConstants.Token, token);
        getResource.addTypeContent(JSONConstants.GetResource);
        getResource.addBodyContent(JSONConstants.Resource, name);

        outStream.println(getResource.getStringRepresentation());

        final JSON getResourceResponse = new JSON(inStream.readLine());
        if (getResourceResponse.getTypeValue().equals(JSONConstants.TakeResource)) {
            System.out.println(">>>> Response resource: \"" + getResourceResponse.getValueForKey(JSONConstants.Value) + "\" <<<<");
            return true;
        } else {
            if (getResourceResponse.getTypeValue().equals(JSONConstants.Denied)) {
                System.out.println(">>>> Can\'t read resource: \"" + getResourceResponse.getValueForKey(JSONConstants.Reason) + "\" <<<<");
            }
            return false;
        }
    }

    public boolean writeResource(String writeResource, String newResourceValue) throws IOException {
        final JSON setResource = new JSON();
        setResource.addTypeContent(JSONConstants.SetResource);
        setResource.addBodyContent(JSONConstants.Token, token);
        setResource.addBodyContent(JSONConstants.Resource, writeResource);
        setResource.addBodyContent(JSONConstants.Value, newResourceValue);

        outStream.println(setResource.getStringRepresentation());

        final JSON setResourceResponse = new JSON(inStream.readLine());
        if (setResourceResponse.getTypeValue().equals(JSONConstants.SetResourceResult)) {
            System.out.println(">>>> Result: \"" + setResourceResponse.getValueForKey(JSONConstants.Value) + "\" <<<<");
            return true;
        } else {
            if (setResourceResponse.getTypeValue().equals(JSONConstants.Denied)) {
                System.out.println(">>>> Can\'t write resource: \"" + setResourceResponse.getValueForKey(JSONConstants.Reason) + "\" <<<<");
            }
            return false;
        }
    }

    public boolean getPermissions() throws IOException {
        final JSON getPerms = new JSON();
        getPerms.addBodyContent(JSONConstants.Token, token);
        getPerms.addTypeContent(JSONConstants.GetPermissions);

        outStream.println(getPerms.getStringRepresentation());

        final JSON getPermsResponse = new JSON(inStream.readLine());
        if (getPermsResponse.getTypeValue().equals(JSONConstants.PermissionsList)) {
            System.out.println("\nMy permission list:");
            final String permsValue = getPermsResponse.getValueForKey(JSONConstants.Value);
            if (permsValue.contains(";")) {
                for (String currPerm : permsValue.split(";")) {
                    System.out.println(currPerm.replace(":", " => "));
                }
            } else {
                System.out.println(permsValue.replace(":", " => "));
            }
            return true;
        }
        return false;
    }
}
