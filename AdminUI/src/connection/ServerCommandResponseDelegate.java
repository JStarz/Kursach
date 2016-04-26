package connection;

import base.User;

import java.util.List;

public interface ServerCommandResponseDelegate {
    void userCreated(boolean successful, String errorDescription);
    void userDeleted(boolean successful, String errorDescription);
    void credentialsChanged(boolean successful, String errorDescription);
    void roleAssociated(boolean successful, String errorDescription);
    void userRoleDeleted(boolean successful, String errorDescription);
    void userRolesDeleted(boolean successful, String errorDescription);
    void roleCreated(boolean successful, String errorDescription);
    void roleDeleted(boolean successful, String errorDescription);
    void roleNameChanged(boolean successful, String errorDescription);
    void permissionsManaged(boolean successful, String errorDescription);
    void resourceChanged(boolean successful, String errorDescription);
    void associatedRoleWithRole(boolean successful, String errorDescription);
    void deleteAssociatedRole(boolean successful, String errorDescription);
    void deleteAllAssociatedRole(boolean successful, String errorDescription);

    void availableAssociativeRoles(String[] roles, String errorDescription);
    void getUserPermissions(List<User> user, String errorDescription);
    void getUsersPermissions(List<User> users, String errorDescription);
    void getUsers(String[] users, String errorDescription);
    void getRoles(String[] roles, String errorDescription);
    void getRolesForUser(String[] roles, String errorDescription);
    void getResources(String[] resources, String errorDescription);
    void getDistinctResources(String[] roles, String errorDescription);
    void getRolePermissions(String resource, String perms, String errorDescription);
    void distinctedResources(String[] distinctedResources, String errorDescription);
    void associativeRoles(String[] roles, String errorDescription);
}
