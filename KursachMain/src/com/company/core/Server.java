package com.company.core;

import com.company.base.exceptions.UserNotAdminException;
import com.company.base.iam.Resource;
import com.company.base.model.AuthorizationModel;
import com.company.dao.DatabaseUtils;
import com.company.json.JSON;
import com.company.json.JSONConstants;
import com.company.kerberos.KerberosConnection;
import com.company.kerberos.KerberosResponses;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Server implements KerberosResponses, ClientRequests {

    final int serverPort;
    final ServerSocket serverSocket;

    List<KerberosConnection> kerberosConnections;

    ConcurrentHashMap<String, Client> activeClients;
    ConcurrentHashMap<Client, AuthorizationModel> activeClientsPermissions;

    public Server(int serverPort) throws Exception {
        System.out.println("SERVER: init started");

        this.serverPort = serverPort;
        this.serverSocket = new ServerSocket(serverPort);

        System.out.println( InetAddress.getLocalHost() );

        this.activeClients = new ConcurrentHashMap<>();
        this.activeClientsPermissions = new ConcurrentHashMap<>();
        this.kerberosConnections = Collections.synchronizedList(new ArrayList<>());

        runAcceptThread();
    }

    private void runAcceptThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                System.out.println("SERVER: accept thread started");

                while (true) {
                    try {
                        System.out.println("SERVER: wait a client");
                        Client client = new Client(serverSocket.accept());

                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    System.out.println("SERVER: client accepted");
                                    KerberosConnection kerberos = createKerberosConnection(client);
                                    System.out.println("SERVER: kerberos connection created");
                                    kerberos.login(client.inStream.readLine());
                                    System.out.println("SERVER: session finished with " + client.getName());
                                } catch (Exception e) {
                                    client.close();
                                }
                            }
                        }.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private KerberosConnection createKerberosConnection(Client client) throws Exception {
        KerberosConnection kerberos = new KerberosConnection("localhost", 9000, client);
        kerberos.delegate = this;
        kerberosConnections.add(kerberos);
        return kerberos;
    }

    private AuthorizationModel authorize(Client client) {
        return new AuthorizationModel(client);
    }

    @Override
    public void closeKerberosServerConnection(KerberosConnection connection, Client client, String closeDefinition) {
        // TODO: add log
        System.out.println("CONNECTION CLOSE: " + closeDefinition);
        client.closeConnection(closeDefinition);
        activeClients.entrySet().removeIf(item -> item.getValue().equals(client));
        activeClientsPermissions.remove(client);
    }

    @Override
    public void updateKerberosServerConnection(KerberosConnection connection, Client client, String oldToken, String newToken) {
        // TODO: add log
        System.out.println("NEW TOKEN: " + newToken);
        if (oldToken != null) activeClients.remove(oldToken, client);
        activeClients.put(newToken, client);
        final AuthorizationModel perms = authorize(client);
        activeClientsPermissions.put(client, perms);
        client.updateConnection(oldToken, newToken);
        client.delegate = this;
        client.startListenClientRequests();
    }

    @Override
    public void wrongCredentials(KerberosConnection connection, Client client, String wrongDescription) {
        // TODO: add log
        System.out.println("WRONG CREDENTIALS: " + wrongDescription);
        client.wrongCredentials(wrongDescription);
        try {
            connection.login(client.inStream.readLine());
        } catch (IOException e) {
            client.close();
        }
    }

    @Override
    public void clientRequest(Client client, String request) {
        final JSON json = new JSON(request);
        final String token = json.getValueForKey(JSONConstants.Token);
        if (token != null && (activeClients.get(token).compareTo(client) == 0)) {
            final String requestType = json.getTypeValue();
            final AuthorizationModel model = activeClientsPermissions.get(client);

            switch (requestType) {
                case JSONConstants.GetResource:
                    final Resource resource = model.getResource(
                            json.getValueForKey(JSONConstants.Resource),
                            "READ"
                    );
                    client.sendResource(resource);
                    break;

                case JSONConstants.SetResource:
                    final boolean isSet = model.setResource(
                            json.getValueForKey(JSONConstants.Resource),
                            "WRITE",
                            json.getValueForKey(JSONConstants.Value)
                    );
                    client.resourceSetted(isSet);
                    break;

                case JSONConstants.GetPermissions:
                    final List<String> perms = model.getPermissions();
                    client.permissionsList(perms);
                    break;

                // ADMIN FUNCTIONS
                case JSONConstants.GetRoles:
                    try {
                        final List<String> allRoles = model.getRoles();
                        client.allRoles(allRoles);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetDistinctedResources:
                    try {
                        final List<String> distinctedResources = model.getDistinctResourcesForRole(json.getValueForKey(JSONConstants.Role));
                        client.distinctResources(distinctedResources);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetRolesPermissions:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Name);
                        final String[] rolePerms = model.getRolePerms(role);
                        client.rolePerms(rolePerms);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.ManagePermissions:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final String res = json.getValueForKey(JSONConstants.Resource);
                        final String permissions = json.getValueForKey(JSONConstants.PermissionsList);
                        final boolean result = model.managePermissions(role, res, permissions);
                        if (result) activeClientsPermissions.forEach((client1, authorizationModel) -> authorizationModel.updateRolePerms(role, res, permissions));
                        client.permissionsManaged(result);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetResources:
                    try {
                        final List<String> allResources = model.getResources();
                        client.allResources(allResources);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DistinctRolesForUser:
                    try {
                        final String name = json.getValueForKey(JSONConstants.Name);
                        final List<String> allResources = model.getDistinctRolesForUser(name);
                        client.distinctRoles(allResources);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetRolesForUser:
                    try {
                        final String name = json.getValueForKey(JSONConstants.Name);
                        final List<String> rolesForUser = model.getRolesForUser(name);
                        client.allRoles(rolesForUser);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.CreateUser:
                    try {
                        final boolean userCreated = model.createUser(
                                json.getValueForKey(JSONConstants.Name),
                                json.getValueForKey(JSONConstants.Password),
                                json.getValueForKey(JSONConstants.NewType)
                        );
                        client.userCreated(userCreated);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteUser:
                    try {
                        final String username = json.getValueForKey(JSONConstants.Name);

                        final boolean userDeleted = model.deleteUser(username);

                        if (userDeleted) {
                            activeClients.entrySet().removeIf(
                                    activeClient -> activeClient.getValue().getName().equals(username)
                            );
                            activeClientsPermissions.entrySet().removeIf(
                                    activeClient -> activeClient.getKey().getName().equals(username)
                            );
                            kerberosConnections.stream().filter(
                                    kerberosConnection -> kerberosConnection.client.getName().equals(username)
                            ).forEach(
                                    kerberosConnection -> kerberosConnection.client.closeConnection("You are deleted!")
                            );
                        }

                        client.userDeleted(userDeleted);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.ChangeUserCredentials:
                    try {
                        final String oldName = json.getValueForKey(JSONConstants.Name);
                        final String newName = json.getValueForKey(JSONConstants.NewName);
                        final String newPassword = json.getValueForKey(JSONConstants.NewPassword);
                        final String newType = json.getValueForKey(JSONConstants.NewType);

                        final boolean userChanged = model.changeUserCredentials(oldName, newName, newPassword, newType);
                        if (userChanged) {
                            activeClients.entrySet().stream()
                                    .filter(entry -> entry.getValue().getName().equals(oldName))
                                    .forEach(entry -> {
                                        activeClientsPermissions.entrySet().stream()
                                                .filter(permsEntry -> permsEntry.getKey().getName().equals(oldName))
                                                .forEach(permsEntry -> {
                                                    permsEntry.getKey().setName(newName);
                                                    permsEntry.getKey().setPassword(newPassword);
                                                    permsEntry.getValue().setAdmin(newType.equals("ADMIN"));
                                                });
                                        entry.getValue().setName(newName);
                                        entry.getValue().setPassword(newPassword);
                                    });
                        }
                        client.userChanged(userChanged);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.AssociateRoleWithUser:
                    try {
                        final String user = json.getValueForKey(JSONConstants.Name);
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final boolean roleAssociated = model.associateRoleWithUser(user, role);

                        if (roleAssociated) {
                            activeClientsPermissions.entrySet().stream()
                                    .filter(entry -> entry.getKey().getName().equals(user))
                                    .forEach(entry -> entry.getValue().associateRole(role));
                        }
                        client.roleAssociated(roleAssociated);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteUserRole:
                    try {
                        final String user = json.getValueForKey(JSONConstants.Name);
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final boolean roleRemoved = model.deleteUserRole(user, role);

                        if (roleRemoved) {
                            activeClientsPermissions.entrySet().stream()
                                    .filter(entry -> entry.getKey().getName().equals(user))
                                    .forEach(entry -> entry.getValue().removeRoleFromRoleModel(role));
                        }
                        client.roleDeleted(roleRemoved);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteUserRoles:
                    try {
                        final String user = json.getValueForKey(JSONConstants.Name);
                        final boolean rolesRemoved = model.deleteUserRoles(user);

                        if (rolesRemoved) {
                            activeClientsPermissions.entrySet().stream()
                                    .filter(entry -> entry.getKey().getName().equals(user))
                                    .forEach(entry -> entry.getValue().removeAllRoles());
                        }
                        client.roleDeleted(rolesRemoved);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetUsers:
                    try {
                        final List<String> users = model.getUsers();
                        client.sendUsersList(users);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetUsersPermissions:
                    try {
                        final Map<String, List<String>> usersPermissions = model.getUsersPermissions();
                        client.sendUsersPermissionsList(usersPermissions);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetUserPermissions:
                    try {
                        final List<String> userPermissions = model.getUserPermissions(json.getValueForKey(JSONConstants.Name));
                        client.permissionsList(userPermissions);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.CreateRole:
                    try {
                        final boolean roleCreated = model.createRole(
                                json.getValueForKey(JSONConstants.Role),
                                json.getValueForKey(JSONConstants.Resource),
                                json.getValueForKey(JSONConstants.PermissionsList)
                        );
                        client.roleCreated(roleCreated);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteRole:
                    try {
                        final String roleName = json.getValueForKey(JSONConstants.Role);
                        final boolean roleDeleted = model.deleteRole(roleName);
                        if (roleDeleted) {
                            activeClientsPermissions.forEach((key, value) -> value.removeRoleFromRoleModel(roleName));
                        }
                        client.roleDeleted(roleDeleted);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.ChangeRoleName:
                    try {
                        final String oldRoleName = json.getValueForKey(JSONConstants.Role);
                        final String newRoleName = json.getValueForKey(JSONConstants.NewRoleName);
                        final boolean roleNameChanged = model.changeRoleName(oldRoleName, newRoleName);
                        if (roleNameChanged) {
                            activeClientsPermissions.forEach((key, value) -> value.changeRoleNameInRoleModel(oldRoleName, newRoleName));
                        }
                        client.roleNameChanged(roleNameChanged);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.AddPermissionsToRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final String toResource = json.getValueForKey(JSONConstants.Resource);
                        final String permissions = json.getValueForKey(JSONConstants.PermissionsList);
                        final boolean permissionsAdded = model.addPermissionsToRole(role, permissions);
                        if (permissionsAdded) {
                            activeClientsPermissions.forEach((key, value) -> value.addPermsToRoleModel(role,toResource,permissions));
                        }
                        client.permissionsAdded(permissionsAdded);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeletePermissionsFromRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final String toResource = json.getValueForKey(JSONConstants.Resource);
                        final String permissions = json.getValueForKey(JSONConstants.PermissionsList);
                        final boolean permissionsDeleted = model.deletePermissionsFromRole(role, permissions);
                        if (permissionsDeleted) {
                            activeClientsPermissions.forEach((key, value) -> value.removePermsFromRoleModel(role, toResource, permissions));
                        }
                        client.permissionsDeleted(permissionsDeleted);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetAssociativeRoles:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final List<String> associativeRoles = model.getAssociativeRoles(role);
                        client.associativeRoles(associativeRoles);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteAllPermissionsFromRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final String toResource = json.getValueForKey(JSONConstants.Resource);
                        final boolean allPermissionsDeleted = model.deleteAllPermissions(role);
                        if (allPermissionsDeleted) {
                            activeClientsPermissions.forEach((key, value) -> value.removeAllPerms(role, toResource));
                        }
                        client.allPermissionsDeleted(allPermissionsDeleted);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.GetAvailableAssociativeRoles:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final List<String> associativeRoles = model.getAvailableAssociativeRoles(role);
                        client.availableAssociativeRoles(associativeRoles);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.ChangeResursFromRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final List<String> list = DatabaseUtils.getDistinctResourcesForRole(role);
                        final String oldResource = DatabaseUtils.getAllResources().stream().filter(res -> !list.contains(res)).collect(Collectors.toList()).get(0);
                        final String newResource = json.getValueForKey(JSONConstants.NewResurs);
                        final boolean resourceNameChanged = model.changeResursFromRole(role, newResource);
                        if (resourceNameChanged) {
                            activeClientsPermissions.forEach((key, value) -> value.changeResursName(role, oldResource, newResource));
                        }
                        client.resourceNameChanged(resourceNameChanged);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.AssociateRoleWithRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final String associativeRole = json.getValueForKey(JSONConstants.AssociativeRole);
                        final boolean roleAssociated = model.associateRoleWithRole(role, associativeRole);
                        if (roleAssociated) {
                            activeClientsPermissions.forEach((key, value) -> value.associateRole(role, associativeRole));
                        }
                        client.roleAssociated(roleAssociated);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteAssociatedRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final String associativeRole = json.getValueForKey(JSONConstants.AssociativeRole);
                        final boolean associatedRoleDeleted = model.deleteAssociatedRole(role, associativeRole);
                        if (associatedRoleDeleted) {
                            activeClientsPermissions.forEach((key, value) -> value.deleteAssociatedRole(role));
                        }
                        client.associatedRoleDeleted(associatedRoleDeleted);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                case JSONConstants.DeleteAllAssociatedRole:
                    try {
                        final String role = json.getValueForKey(JSONConstants.Role);
                        final boolean associatedRolesDeleted = model.deleteAssociatedRoles(role);
                        if (associatedRolesDeleted) {
                            activeClientsPermissions.forEach((key, value) -> value.deleteAssociatedRole(role));
                        }
                        client.associatedRoleDeleted(associatedRolesDeleted);
                    } catch (UserNotAdminException e) {
                        client.permissionDenied("Unrecognized command");
                    }
                    break;

                default:
                    // TODO: unsupported request parameter, add to log
                    break;
            }
        } else {
            // TODO: bad token! add to log
            client.saidBadToken();
        }
    }
}
