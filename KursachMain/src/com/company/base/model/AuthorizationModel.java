package com.company.base.model;

import com.company.base.exceptions.UserNotAdminException;
import com.company.base.iam.Permission;
import com.company.base.iam.Resource;
import com.company.base.iam.Role;
import com.company.core.Client;
import com.company.dao.DatabaseUtils;

import javax.xml.crypto.Data;
import java.lang.reflect.Array;
import java.util.*;

public class AuthorizationModel {

    RoleModel roles;
    boolean isAdmin;

    public AuthorizationModel(Client client) {
        final String clientRoles = DatabaseUtils.readClientRoles(client);
        roles = new RoleModel( clientRoles == null ? "" : clientRoles );
        isAdmin = DatabaseUtils.isAdmin(client);
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Resource getResource(String resourceName, String rule) {
        return roles.getResource(new Resource(resourceName), new Permission(rule));
    }

    public boolean setResource(String resourceName, String rule, String newValue) {
        return roles.setResource(new Resource(resourceName), new Permission(rule), newValue);
    }

    public List<String> getResourcePath(String resource) {
        return roles.getResourcePath(new Resource(resource));
    }

    public List<String> getPermissions() {
        return roles.getPermissions();
    }

    public String downloadFile(String resource) {
        return new Resource(resource).getResourceValueFromResourceServer();
    }

    public List<String> getUsers() throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();
        return DatabaseUtils.getUsers();
    }

    public Map<String, List<String>> getUsersPermissions() throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        Map<String, List<String>> out = new HashMap<>();
        final List<String> users = DatabaseUtils.getUsers();
        if (users != null) {
            users.stream().forEach(user -> {
                final String userRoles = DatabaseUtils.readClientRoles(user);
                final RoleModel userModel = new RoleModel(userRoles);
                final List<String> perms = userModel.getPermissions();

                out.put(user, perms);
            });
        }
        return out;
    }

    public List<String> getUserPermissions(String user) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        final String userRoles = DatabaseUtils.readClientRoles(user);
        final RoleModel userModel = new RoleModel(userRoles);
        return userModel.getPermissions();
    }

    public boolean createUser(String name, String password, String admin) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.addUser(name, password, admin);
    }

    public boolean deleteUser(String name) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteUser(name);
    }

    public boolean changeUserCredentials(String oldName, String newName, String newPassword, String newType) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.updateUser(oldName, newName, newPassword, newType);
    }

    public boolean createRole(String roleName, String resource, String perms) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.createRole(roleName, resource, perms);
    }

    public boolean deleteRole(String roleName) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteRole(roleName);
    }

    public void removeRoleFromRoleModel(String roleName) {
        roles.removeRoleByName(roleName);
    }

    public void changeRoleNameInRoleModel(String oldName, String newName) {
        roles.changeRoleName(oldName, newName);
    }

    public void addPermsToRoleModel(String role, String resource, String perms) {
        roles.addPerms(role, resource, perms);
    }

    public void changeResursName(String role, String oldName, String newName) {
        roles.changeResourceName(role, oldName, newName);
    }

    public void associateRole(String role, String associativeRole) {
        roles.associateRoleWithRole(role, associativeRole);
    }

    public void removePermsFromRoleModel(String role, String resource, String perms) {
        roles.removePerms(role, resource, perms);
    }

    public void removeAllPerms(String role, String resource) {
        roles.removeAllPerms(role, resource);
    }

    public void removeAllRoles() {
        roles.removeAllRoles();
    }

    public boolean associateRoleWithUser(String user, String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.associateRoleWithUser(user, role);
    }

    public void associateRole(String role) {
        roles.addRole(new Role(role));
    }

    public void deleteAssociatedRole(String role) {
        roles.deleteAssociatedRole(role);
    }

    public boolean deleteUserRole(String user, String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteUserRole(user, role);
    }

    public boolean deleteUserRoles(String user) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteUserRoles(user);
    }

    public boolean changeRoleName(String oldRoleName, String newRoleName) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.changeRoleName(oldRoleName, newRoleName);
    }

    public boolean addPermissionsToRole(String role, String permissions) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.addPermissionsToRole(role, permissions);
    }

    public boolean deletePermissionsFromRole(String role, String permissions) throws UserNotAdminException  {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deletePermissionsFromRole(role, permissions);
    }

    public boolean deleteAllPermissions(String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteAllPermissionsFromRole(role);
    }

    public boolean changeResursFromRole(String role, String newResource) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.changeResursFromRole(role, newResource);
    }

    public boolean associateRoleWithRole(String role, String associativeRole) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.associateRoleWithRole(role, associativeRole);
    }

    public boolean deleteAssociatedRole(String role, String associativeRole) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteAssociatedRole(role, associativeRole);
    }

    public boolean deleteAssociatedRoles(String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.deleteAllAssociatedRoles(role);
    }

    public List<String> getRoles() throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getRoles();
    }

    public List<String> getRolesForUser(String name) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        final String roles = DatabaseUtils.readClientRoles(name);
        if (roles != null) {
            return Arrays.asList(roles.split("\\|"));
        }
        return new ArrayList<>();
    }

    public List<String> getResources() throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getAllResources();
    }

    public List<String> getDistinctRolesForUser(String name) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getDistinctRolesForUser(name);
    }

    public String[] getRolePerms(String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getRolePermsAndResource(role);
    }

    public boolean managePermissions(String role, String resource, String perms) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.managePermissions(role, resource, perms);
    }

    public void updateRolePerms(String role, String resource, String permissions) {
        roles.updateRolePerms(role, resource, permissions);
    }

    public List<String> getDistinctResourcesForRole(String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getDistinctResourcesForRole(role);
    }

    public List<String> getAvailableAssociativeRoles(String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getAvailableAssociativeRoles(role);
    }

    public List<String> getAssociativeRoles(String role) throws UserNotAdminException {
        if (!isAdmin) throw new UserNotAdminException();

        return DatabaseUtils.getAssociativeRoles(role);
    }
}
