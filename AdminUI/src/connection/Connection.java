package connection;

import base.User;
import javafx.application.Platform;
import json.JSON;
import json.JSONConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection {

    final private String SUCCESS = "Success";

    final Socket adminSocket;
    final BufferedReader inStream;
    final PrintWriter outStream;

    public ServerAuthResponseDelegate authResponse;
    public ServerCommandResponseDelegate commandResponse;

    private String token;

    public Connection(Socket adminSocket) throws Exception {
        this.adminSocket = adminSocket;
        this.inStream = new BufferedReader(new InputStreamReader(adminSocket.getInputStream()));
        this.outStream = new PrintWriter(adminSocket.getOutputStream(), true);
    }

    public void close() throws IOException {
        inStream.close();
        outStream.close();
        adminSocket.close();
    }

    public void auth(String name, String password) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                JSON json = new JSON();
                json.addTypeContent(JSONConstants.Auth);
                json.addBodyContent(JSONConstants.Name, name);
                json.addBodyContent(JSONConstants.Password, password);

                outStream.println(json.getStringRepresentation());

                try {
                    JSON newJson = new JSON(inStream.readLine());
                    if (newJson.getTypeValue().equals(JSONConstants.AuthFailed)) {
                        if (authResponse != null) {
                            Platform.runLater(() -> authResponse.authFailed(newJson.getValueForKey(JSONConstants.Reason)));
                        }
                    } else if (newJson.getTypeValue().equals(JSONConstants.ConnectionClose)) {
                        if (authResponse != null) {
                            Platform.runLater(() -> authResponse.connectionClose(newJson.getValueForKey(JSONConstants.Reason)));
                        }
                    } else if (newJson.getTypeValue().equals(JSONConstants.TokenUpdate)) {
                        if (authResponse != null) {
                            token = newJson.getValueForKey(JSONConstants.NewToken);
                            Platform.runLater(() -> authResponse.authSuccesful());
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void createUser(String createUserName, String createUserPassword, String createUserType) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.CreateUser);
                    json.addBodyContent(JSONConstants.Name, createUserName);
                    json.addBodyContent(JSONConstants.Password, createUserPassword);
                    json.addBodyContent(JSONConstants.NewType, createUserType);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.userCreated(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.CreateUserResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.userCreated(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void deleteUser(String deleteUserName) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DeleteUser);
                    json.addBodyContent(JSONConstants.Name, deleteUserName);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.userDeleted(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DeleteUserResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.userDeleted(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void changeUserCredentials(String oldUserName, String newUserName, String newUserPassword, String newUserType) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.ChangeUserCredentials);
                    json.addBodyContent(JSONConstants.Name, oldUserName);
                    json.addBodyContent(JSONConstants.NewName, newUserName);
                    json.addBodyContent(JSONConstants.NewPassword, newUserPassword);
                    json.addBodyContent(JSONConstants.NewType, newUserType);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.credentialsChanged(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.ChangeUserCredentialsResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.credentialsChanged(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void deleteUserRole(String userNameForRoleRemoving, String roleNameForRemovingFromUser) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DeleteUserRole);
                    json.addBodyContent(JSONConstants.Name, userNameForRoleRemoving);
                    json.addBodyContent(JSONConstants.Role, roleNameForRemovingFromUser);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.userRoleDeleted(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DeleteRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.userRoleDeleted(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getUsers() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetUsers);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getUsers(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.AllUsers)) {
                        Platform.runLater(() -> commandResponse.getUsers(response.getValueForKey(JSONConstants.Value).split(";"), null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getRolesForUser(String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetRolesForUser);
                    json.addBodyContent(JSONConstants.Name, name);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getRolesForUser(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetRolesResult)) {
                        Platform.runLater(() -> commandResponse.getRolesForUser(response.getValueForKey(JSONConstants.Value).split(";"), null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getAllRoles() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetRoles);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getRoles(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetRolesResult)) {
                        Platform.runLater(() -> commandResponse.getRoles(response.getValueForKey(JSONConstants.Value).split(";"), null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void associateRoleWithUser(String name, String role) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.AssociateRoleWithUser);
                    json.addBodyContent(JSONConstants.Name, name);
                    json.addBodyContent(JSONConstants.Role, role);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.roleAssociated(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.AssociateRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.roleAssociated(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void deleteUserRoles(String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DeleteUserRoles);
                    json.addBodyContent(JSONConstants.Name, name);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.userRolesDeleted(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DeleteRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.userRolesDeleted(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getUsersPermissions() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetUsersPermissions);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getUsersPermissions(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.AllPermissionsList)) {
                        final List<User> usvery = new ArrayList<>();
                        final String permsValue = response.getValueForKey(JSONConstants.Value);
                        final String[] users = permsValue.split("<>");
                        for (String user : users) {
                            final String[] userAndPerms = user.split(">>");

                            if (userAndPerms[1].contains(";")) {
                                for (String currPerm : userAndPerms[1].split(";")) {
                                    final String[] splittedResourceAndPerms = currPerm.split(":");
                                    usvery.add(new User(userAndPerms[0], splittedResourceAndPerms[0], splittedResourceAndPerms[1]));
                                }
                            } else {
                                final String[] splittedResourceAndPerms = userAndPerms[1].split(":");
                                usvery.add(new User(userAndPerms[0], splittedResourceAndPerms[0], splittedResourceAndPerms[1]));
                            }
                        }
                        Platform.runLater(() -> commandResponse.getUsersPermissions(usvery, null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getUserPermissions(String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetUserPermissions);
                    json.addBodyContent(JSONConstants.Name, name);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getUserPermissions(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.PermissionsList)) {
                        final List<User> usvery = new ArrayList<>();
                        final String permsValue = response.getValueForKey(JSONConstants.Value);
                        if (permsValue.contains(";")) {
                            for (String currPerm : permsValue.split(";")) {
                                final String[] splittedResourceAndPerms = currPerm.split(":");
                                usvery.add(new User(name, splittedResourceAndPerms[0], splittedResourceAndPerms[1]));
                            }
                        } else {
                            final String[] splittedResourceAndPerms = permsValue.split(":");
                            usvery.add(new User(name, splittedResourceAndPerms[0], splittedResourceAndPerms[1]));
                        }
                        Platform.runLater(() -> commandResponse.getUserPermissions(usvery, null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getResources() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetResources);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getResources(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetResourcesResult)) {
                        final String value = response.getValueForKey(JSONConstants.Value);
                        if (value.contains(";"))
                            Platform.runLater(() -> commandResponse.getResources(value.split(";"), null));
                        else Platform.runLater(() -> commandResponse.getResources(new String[]{value}, null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void createRole(String newRoleName, String newRoleResource, String newRolePermissions) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.CreateRole);
                    json.addBodyContent(JSONConstants.Role, newRoleName);
                    json.addBodyContent(JSONConstants.Resource, newRoleResource);
                    json.addBodyContent(JSONConstants.PermissionsList, newRolePermissions);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.roleCreated(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.CreateRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.roleCreated(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void deleteRole(String role) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DeleteRole);
                    json.addBodyContent(JSONConstants.Role, role);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.roleDeleted(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DeleteRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.roleDeleted(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getDistinctRolesForUser(String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DistinctRolesForUser);
                    json.addBodyContent(JSONConstants.Name, name);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getDistinctResources(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DistinctRolesForUserResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.getDistinctResources(result.split(";"), null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void changeRoleName(String oldRole, String newRole) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.ChangeRoleName);
                    json.addBodyContent(JSONConstants.Role, oldRole);
                    json.addBodyContent(JSONConstants.NewRoleName, newRole);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.roleNameChanged(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.ChangeRoleNameResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.roleNameChanged(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getRolePermissions(String role) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetRolesPermissions);
                    json.addBodyContent(JSONConstants.Name, role);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.getRolePermissions(null, null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetRolesPermissionsResult)) {
                        final String resource = response.getValueForKey(JSONConstants.Resource);
                        final String perms = response.getValueForKey(JSONConstants.PermissionsList);
                        Platform.runLater(() -> commandResponse.getRolePermissions(resource, perms, null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void managePermissions(String role, String resource, String perms) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.ManagePermissions);
                    json.addBodyContent(JSONConstants.Role, role);
                    json.addBodyContent(JSONConstants.Resource, resource);
                    json.addBodyContent(JSONConstants.PermissionsList, perms);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.permissionsManaged(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.ManagePermissionsResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.permissionsManaged(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void changeResursFromRole(String roleNameForChangeResourceName, String newResourceName) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.ChangeResursFromRole);
                    json.addBodyContent(JSONConstants.Role, roleNameForChangeResourceName);
                    json.addBodyContent(JSONConstants.NewResurs, newResourceName);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.resourceChanged(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.ChangeResursFromRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.resourceChanged(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getAvailableAssociativeRolesForRole(String role) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetAvailableAssociativeRoles);
                    json.addBodyContent(JSONConstants.Role, role);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.availableAssociativeRoles(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetAvailableAssociativeRolesResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.availableAssociativeRoles(result.split(";"), null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void associateRoleWithRole(String roleNameForAssociate, String associativeRoleName) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.AssociateRoleWithRole);
                    json.addBodyContent(JSONConstants.Role, roleNameForAssociate);
                    json.addBodyContent(JSONConstants.AssociativeRole, associativeRoleName);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.associatedRoleWithRole(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.AssociateRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.associatedRoleWithRole(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getAssociativeRolesForRole(String role) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetAssociativeRoles);
                    json.addBodyContent(JSONConstants.Role, role);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.associativeRoles(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetAssociativeRolesResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        if (result.contains(";")) Platform.runLater(() -> commandResponse.associativeRoles(result.split(";"), null));
                        else Platform.runLater(() -> commandResponse.associativeRoles(new String[]{result}, null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getDistinctResourceForRole(String role) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.GetDistinctedResources);
                    json.addBodyContent(JSONConstants.Role, role);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.distinctedResources(null, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.GetDistinctedResourcesResult)) {
                        final String value = response.getValueForKey(JSONConstants.Value);
                        if (value.contains(";")) Platform.runLater(() -> commandResponse.distinctedResources(value.split(";"), null));
                        else Platform.runLater(() -> commandResponse.distinctedResources(new String[]{value}, null));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void deleteAssociatedRole(String roleNameForDeleteAssociate, String associativeRoleNameForDelete) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DeleteAssociatedRole);
                    json.addBodyContent(JSONConstants.Role, roleNameForDeleteAssociate);
                    json.addBodyContent(JSONConstants.AssociativeRole, associativeRoleNameForDelete);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.deleteAssociatedRole(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DeleteAssociatedRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.deleteAssociatedRole(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void deleteAllAssociatedRoles(String roleNameForDeleteAllAssociatedRoles) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                try {
                    JSON json = createJSONWithToken();
                    json.addTypeContent(JSONConstants.DeleteAllAssociatedRole);
                    json.addBodyContent(JSONConstants.Role, roleNameForDeleteAllAssociatedRoles);

                    outStream.println(json.getStringRepresentation());

                    JSON response = new JSON(inStream.readLine());
                    if (response.getTypeValue().equals(JSONConstants.Denied)) {
                        Platform.runLater(() -> commandResponse.deleteAllAssociatedRole(false, response.getValueForKey(JSONConstants.Reason)));
                    }
                    if (response.getTypeValue().equals(JSONConstants.DeleteAssociatedRoleResult)) {
                        final String result = response.getValueForKey(JSONConstants.Value);
                        Platform.runLater(() -> commandResponse.deleteAllAssociatedRole(result.equals(SUCCESS), result));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    private JSON createJSONWithToken() {
        final JSON json = new JSON();
        json.addBodyContent(JSONConstants.Token, token);
        return json;
    }

    private void baseSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }
}
