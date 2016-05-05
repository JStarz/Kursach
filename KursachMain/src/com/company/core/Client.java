package com.company.core;

import com.company.base.iam.Resource;
import com.company.base.model.ClientModel;
import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

interface ClientRequests {
    void clientRequest(Client client, String request);
}

public class Client extends ClientModel {

    final Socket clientSocket;
    final BufferedReader inStream;
    final PrintWriter outStream;

    ClientRequests delegate;

    public Client(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.outStream = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void startListenClientRequests() {
        Client self = this;
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    while (true) {
                        final String request = inStream.readLine();
                        if (delegate != null) delegate.clientRequest(self, request);
                    }
                } catch (IOException e) {
                    close();
                }

            }
        }.start();
    }

    public void close() {
        try {
            inStream.close();
        } catch (IOException e1) {
        }
        try {
            clientSocket.close();
        } catch (IOException e1) {
        }
        outStream.close();
        System.out.println("Connection closed with client " + name);
    }

    public void parseModelFromString(String content) {
        final JSON json = new JSON(content);
        if (json.getTypeValue().equals(JSONConstants.Auth)) {
            this.name = json.getValueForKey(JSONConstants.Name);
            this.password = json.getValueForKey(JSONConstants.Password);
        }
    }

    public void wrongCredentials(String desc) {
        final JSON wrongCredentials = new JSON();
        wrongCredentials.addTypeContent(JSONConstants.AuthFailed);
        wrongCredentials.addBodyContent(JSONConstants.Reason, desc);
        outStream.println(wrongCredentials.getStringRepresentation());
    }

    public void updateConnection(String oldToken, String newToken) {
        final JSON json = new JSON();
        json.addTypeContent(JSONConstants.TokenUpdate);
        json.addBodyContent(JSONConstants.NewToken, newToken);
        if (oldToken != null) json.addBodyContent(JSONConstants.OldToken, oldToken);
        outStream.println(json.getStringRepresentation());
    }

    public void closeConnection(String closeDefinition) {
        try {
            final JSON json = new JSON();
            json.addTypeContent(JSONConstants.ConnectionClose);
            json.addBodyContent(JSONConstants.Reason, closeDefinition);

            outStream.println(json.getStringRepresentation());
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResource(Resource resource) {
        if (resource != null) {
            final String responseFromResourceServer = resource.getResourceValueFromResourceServer();

            final JSON json = new JSON();
            json.addTypeContent(JSONConstants.TakeResource);
            json.addBodyContent(
                    JSONConstants.Value,
                    (responseFromResourceServer != null && !responseFromResourceServer.isEmpty())
                            ? responseFromResourceServer
                            : "Resource doesn't exist!"
            );

            outStream.println(json.getStringRepresentation());
        } else {
            // TODO: resource access denied, add log
            permissionDenied("Access denied!");
        }
    }

    public void resourceSetted(boolean isSetted) {
        if (isSetted) {
            final JSON json = new JSON();

            json.addTypeContent(JSONConstants.SetResourceResult);
            json.addBodyContent(
                    JSONConstants.Value,
                    JSONConstants.SetResourceResultSuccess
            );

            outStream.println(json.getStringRepresentation());
        } else {
            // TODO: resource access denied, add log
            permissionDenied("Access denied!");
        }
    }

    public void permissionDenied(String deniedDefinition) {
        final JSON json = new JSON();
        json.addTypeContent(JSONConstants.Denied);
        json.addBodyContent(JSONConstants.Reason, deniedDefinition);

        outStream.println(json.getStringRepresentation());
    }

    public void saidBadToken() {
        final JSON json = new JSON();
        json.addTypeContent(JSONConstants.BadToken);
        outStream.println(json.getStringRepresentation());
    }

    public void permissionsList(List<String> perms) {
        final JSON json = new JSON();
        final String permissionsValue = getStringFromList(perms);

        json.addTypeContent(JSONConstants.PermissionsList);
        json.addBodyContent(JSONConstants.Value, permissionsValue);

        outStream.println(json.getStringRepresentation());
    }

    public void sendUsersList(List<String> users) {
        final JSON json = new JSON();
        final String usersValue = getStringFromList(users);

        json.addTypeContent(JSONConstants.AllUsers);
        json.addBodyContent(JSONConstants.Value, usersValue);

        outStream.println(json.getStringRepresentation());
    }

    public void sendUsersPermissionsList(Map<String, List<String>> users) {
        final JSON json = new JSON();
        String out = "";

        for (Map.Entry<String, List<String>> pair : users.entrySet()) {
            final String usersValue = getStringFromList(pair.getValue());
            out += pair.getKey() + ">>" + usersValue + "<>";
        }

        json.addTypeContent(JSONConstants.AllPermissionsList);
        json.addBodyContent(JSONConstants.Value, out.substring(0, out.length() - 2));

        outStream.println(json.getStringRepresentation());
    }

    private String getStringFromList(List<String> list) {
        String value = "";
        if (list.size() > 0) {
            for (int i = 0, permsSize = list.size(); i < permsSize; i++) {
                if (i != list.size() - 1) value += list.get(i) + ";";
                else value += list.get(i);
            }
        } else value = " ";
        return value;
    }

    public void userCreated(boolean userCreated) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.CreateUserResult);
        json.addBodyContent(JSONConstants.Value, userCreated ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void userDeleted(boolean userDeleted) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.DeleteUserResult);
        json.addBodyContent(JSONConstants.Value, userDeleted ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void userChanged(boolean userChanged) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.ChangeUserCredentialsResult);
        json.addBodyContent(JSONConstants.Value, userChanged ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void roleCreated(boolean roleCreated) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.CreateRoleResult);
        json.addBodyContent(JSONConstants.Value, roleCreated ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void roleDeleted(boolean roleDeleted) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.DeleteRoleResult);
        json.addBodyContent(JSONConstants.Value, roleDeleted ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void roleAssociated(boolean roleAssociated) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.AssociateRoleResult);
        json.addBodyContent(JSONConstants.Value, roleAssociated ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void roleNameChanged(boolean roleNameChanged) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.ChangeRoleNameResult);
        json.addBodyContent(JSONConstants.Value, roleNameChanged ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void permissionsAdded(boolean permissionsAdded) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.AddPermissionsToRoleResult);
        json.addBodyContent(JSONConstants.Value, permissionsAdded ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void permissionsDeleted(boolean permissionsDeleted) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.DeletePermissionsFromRoleResult);
        json.addBodyContent(JSONConstants.Value, permissionsDeleted ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void allPermissionsDeleted(boolean allPermissionsDeleted) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.DeleteAllPermissionsFromRoleResult);
        json.addBodyContent(JSONConstants.Value, allPermissionsDeleted ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void resourceNameChanged(boolean resourceNameChanged) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.ChangeResursFromRoleResult);
        json.addBodyContent(JSONConstants.Value, resourceNameChanged ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void associatedRoleDeleted(boolean associatedRoleDeleted) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.DeleteAssociatedRoleResult);
        json.addBodyContent(JSONConstants.Value, associatedRoleDeleted ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void allRoles(List<String> allRoles) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetRolesResult);
        json.addBodyContent(JSONConstants.Value, getStringFromList(allRoles));

        outStream.println(json.getStringRepresentation());
    }

    public void allResources(List<String> allResources) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetResourcesResult);
        json.addBodyContent(JSONConstants.Value, getStringFromList(allResources));

        outStream.println(json.getStringRepresentation());
    }

    public void distinctRoles(List<String> allResources) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.DistinctRolesForUserResult);
        json.addBodyContent(JSONConstants.Value, getStringFromList(allResources));

        outStream.println(json.getStringRepresentation());
    }

    public void rolePerms(String[] rolePerms) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetRolesPermissionsResult);
        json.addBodyContent(JSONConstants.Resource, rolePerms[1]);
        json.addBodyContent(JSONConstants.PermissionsList, rolePerms[0]);

        outStream.println(json.getStringRepresentation());
    }

    public void permissionsManaged(boolean result) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.ManagePermissionsResult);
        json.addBodyContent(JSONConstants.Value, result ? "Success" : "Failed");

        outStream.println(json.getStringRepresentation());
    }

    public void distinctResources(List<String> distinctedResources) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetDistinctedResourcesResult);
        json.addBodyContent(JSONConstants.Value, getStringFromList(distinctedResources));

        outStream.println(json.getStringRepresentation());
    }

    public void availableAssociativeRoles(List<String> associativeRoles) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetAvailableAssociativeRolesResult);
        json.addBodyContent(JSONConstants.Value, getStringFromList(associativeRoles));

        outStream.println(json.getStringRepresentation());
    }

    public void associativeRoles(List<String> associativeRoles) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetAssociativeRolesResult);
        json.addBodyContent(JSONConstants.Value, getStringFromList(associativeRoles));

        outStream.println(json.getStringRepresentation());
    }

    public void resourcePath(String resourcePath, String resourceName) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.GetResourcePathResult);
        json.addBodyContent(JSONConstants.Value, resourcePath);
        json.addBodyContent(JSONConstants.Name, resourceName);

        outStream.println(json.getStringRepresentation());
    }

    public void resourceDownloaded(String fileContent) {
        final JSON json = new JSON();

        json.addTypeContent(JSONConstants.StartDownloadResult);
        json.addBodyContent(JSONConstants.Value, fileContent);

        outStream.println(json.getStringRepresentation());
    }
}
