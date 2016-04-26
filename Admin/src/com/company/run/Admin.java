package com.company.run;

import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Admin {

    final Socket adminSocket;
    final BufferedReader inStream;
    final PrintWriter outStream;

    private String token;

    public Admin(Socket adminSocket) throws IOException {
        this.adminSocket = adminSocket;
        this.inStream = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
        this.outStream = new PrintWriter(adminSocket.getOutputStream(), true);
    }

    private JSON createJSONWithToken() {
        final JSON json = new JSON();
        json.addBodyContent(JSONConstants.Token, token);
        return json;
    }

    public void close() throws IOException {
        inStream.close();
        outStream.close();
        adminSocket.close();
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

    public void createUser(String createUserName, String createUserPassword, String createUserType) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.CreateUser);
        json.addBodyContent(JSONConstants.Name, createUserName);
        json.addBodyContent(JSONConstants.Password, createUserPassword);
        json.addBodyContent(JSONConstants.NewType, createUserType);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.CreateUserResult)) {
            System.out.println("Create user: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteUser(String deleteUserName) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteUser);
        json.addBodyContent(JSONConstants.Name, deleteUserName);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteUserResult)) {
            System.out.println("Delete user: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void changeUserCredentials(String oldUserName, String newUserName, String newUserPassword, String newUserType) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.ChangeUserCredentials);
        json.addBodyContent(JSONConstants.Name, oldUserName);
        json.addBodyContent(JSONConstants.NewName, newUserName);
        json.addBodyContent(JSONConstants.NewPassword, newUserPassword);
        json.addBodyContent(JSONConstants.NewType, newUserType);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.ChangeUserCredentialsResult)) {
            System.out.println("Change user credentials: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void associateRoleWithUser(String userNameForRole, String roleNameForUser) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.AssociateRoleWithUser);
        json.addBodyContent(JSONConstants.Name, userNameForRole);
        json.addBodyContent(JSONConstants.Role, roleNameForUser);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.AssociateRoleResult)) {
            System.out.println("Associate role for user: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteUserRole(String userNameForRoleRemoving, String roleNameForRemovingFromUser) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteUserRole);
        json.addBodyContent(JSONConstants.Name, userNameForRoleRemoving);
        json.addBodyContent(JSONConstants.Role, roleNameForRemovingFromUser);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteRoleResult)) {
            System.out.println("Delete role from user: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteUserRoles(String userName) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteUserRoles);
        json.addBodyContent(JSONConstants.Name, userName);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteRoleResult)) {
            System.out.println("Delete all roles from user: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void getUsers() throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.GetUsers);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.AllUsers)) {
            System.out.println("All users: ");
            for (String user : response.getValueForKey(JSONConstants.Value).split(";"))
                System.out.println(user);
        }
    }

    public void getUsersPermissions() throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.GetUsersPermissions);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.AllPermissionsList)) {
            final String permsValue = response.getValueForKey(JSONConstants.Value);
            final String[] users = permsValue.split("<>");
            for (String user : users) {
                final String[] userAndPerms = user.split(">>");
                System.out.println("User " + userAndPerms[0]);

                if (userAndPerms[1].contains(";")) {
                    for (String currPerm : userAndPerms[1].split(";")) {
                        System.out.println(currPerm.replace(":", " => "));
                    }
                } else {
                    System.out.println(userAndPerms[1].replace(":", " => "));
                }
            }
        }
    }

    public void getUserPermissions(String userName) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.GetUserPermissions);
        json.addBodyContent(JSONConstants.Name, userName);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.PermissionsList)) {
            final String permsValue = response.getValueForKey(JSONConstants.Value);
            if (permsValue.contains(";")) {
                for (String currPerm : permsValue.split(";")) {
                    System.out.println(currPerm.replace(":", " => "));
                }
            } else {
                System.out.println(permsValue.replace(":", " => "));
            }
        }
    }

    public void createRole(String newRoleName, String newRoleResource, String newRolePermissions) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.CreateRole);
        json.addBodyContent(JSONConstants.Role, newRoleName);
        json.addBodyContent(JSONConstants.Resource, newRoleResource);
        json.addBodyContent(JSONConstants.PermissionsList, newRolePermissions);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.CreateRoleResult)) {
            System.out.println("Create role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteRole(String roleNameForRemoving) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteRole);
        json.addBodyContent(JSONConstants.Role, roleNameForRemoving);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteRoleResult)) {
            System.out.println("Delete role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void changeRoleName(String roleNameForRename, String newRoleNameForRename) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.ChangeRoleName);
        json.addBodyContent(JSONConstants.Role, roleNameForRename);
        json.addBodyContent(JSONConstants.NewRoleName, newRoleNameForRename);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.ChangeRoleNameResult)) {
            System.out.println("Change role name: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void addPermissionsToRole(String roleNameForAddPerms, String resourceNameForAddPerms, String permissions) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.AddPermissionsToRole);
        json.addBodyContent(JSONConstants.Role, roleNameForAddPerms);
        json.addBodyContent(JSONConstants.Resource, resourceNameForAddPerms);
        json.addBodyContent(JSONConstants.PermissionsList, permissions);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.AddPermissionsToRoleResult)) {
            System.out.println("Add permissions to role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deletePermissionsFromRole(String roleNameForDelPerms, String resourceNameForDelPerms, String delPermissions) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeletePermissionsFromRole);
        json.addBodyContent(JSONConstants.Role, roleNameForDelPerms);
        json.addBodyContent(JSONConstants.Resource, resourceNameForDelPerms);
        json.addBodyContent(JSONConstants.PermissionsList, delPermissions);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeletePermissionsFromRoleResult)) {
            System.out.println("Delete permissions from role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteAllPermissionsFromRole(String roleNameForDelAllPerms, String resourceNameForDelAllPerms) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteAllPermissionsFromRole);
        json.addBodyContent(JSONConstants.Role, roleNameForDelAllPerms);
        json.addBodyContent(JSONConstants.Resource, resourceNameForDelAllPerms);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteAllPermissionsFromRoleResult)) {
            System.out.println("Delete all permissions from role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void changeResursFromRole(String roleNameForChangeResourceName, String oldResourceName, String newResourceName) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.ChangeResursFromRole);
        json.addBodyContent(JSONConstants.Role, roleNameForChangeResourceName);
        json.addBodyContent(JSONConstants.Resource, oldResourceName);
        json.addBodyContent(JSONConstants.NewResurs, newResourceName);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.ChangeResursFromRoleResult)) {
            System.out.println("Change resurs from role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void associateRoleWithRole(String roleNameForAssociate, String associativeRoleName) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.AssociateRoleWithRole);
        json.addBodyContent(JSONConstants.Role, roleNameForAssociate);
        json.addBodyContent(JSONConstants.AssociativeRole, associativeRoleName);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.AssociateRoleResult)) {
            System.out.println("Associate role with role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteAssociatedRole(String roleNameForDeleteAssociate, String associativeRoleNameForDelete) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteAssociatedRole);
        json.addBodyContent(JSONConstants.Role, roleNameForDeleteAssociate);
        json.addBodyContent(JSONConstants.AssociativeRole, associativeRoleNameForDelete);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteAssociatedRoleResult)) {
            System.out.println("Delete associated role: " + response.getValueForKey(JSONConstants.Value));
        }
    }

    public void deleteAllAssociatedRoles(String roleNameForDeleteAllAssociatedRoles) throws IOException {
        JSON json = createJSONWithToken();
        json.addTypeContent(JSONConstants.DeleteAllAssociatedRole);
        json.addBodyContent(JSONConstants.Role, roleNameForDeleteAllAssociatedRoles);

        outStream.println(json.getStringRepresentation());

        JSON response = new JSON(inStream.readLine());
        if (response.getTypeValue().equals(JSONConstants.Denied)) {
            System.out.println("Permission denied. Reason: " + response.getValueForKey(JSONConstants.Reason));
        }
        if (response.getTypeValue().equals(JSONConstants.DeleteAssociatedRoleResult)) {
            System.out.println("Delete all associated roles: " + response.getValueForKey(JSONConstants.Value));
        }
    }
}
