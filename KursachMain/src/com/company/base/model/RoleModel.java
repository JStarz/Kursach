package com.company.base.model;

import com.company.base.iam.Permission;
import com.company.base.iam.Resource;
import com.company.base.iam.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoleModel {

    private List<Role> userRoles;

    public RoleModel(String currentUserRoles) {
        this.userRoles = new ArrayList<>();

        if (!currentUserRoles.isEmpty()) {
            final List<String> roleNames = parseRoleNames(currentUserRoles);
            for (String roleName : roleNames) {
                final Role role = new Role(roleName);
                userRoles.add(role);
            }
        }
    }

    private List<String> parseRoleNames(String content) {
        List<String> computedNames = new ArrayList<>();

        if (content.contains("|")) {
            Collections.addAll(computedNames, content.split("\\|"));
        } else {
            computedNames.add(content);
        }

        return computedNames;
    }

    public Role getRoleForName(String roleName) {
        for (Role role : userRoles) {
            if (role.getRoleName().equals(roleName)) {
                return role;
            }
        }
        return null;
    }

    public void removeRoleByName(String roleName) {
        final Role role = getRoleForName(roleName);
        if (role != null) userRoles.remove(role);
    }

    public void addRole(Role role) {
        if (!userRoles.contains(role)) userRoles.add(role);
    }

    public List<String> getResourcePath(Resource resource) {
        for (Role role : userRoles)
            if (role.isResourceGranted(resource))
                return resource.getResourcePath();
        return null;
    }

    public Resource getResource(Resource resource, Permission perm) {
        for (Role role : userRoles)
            if (role.isResourceGranted(resource, perm))
                return resource;
        return null;
    }

    public boolean setResource(Resource resource, Permission permission, String value) {
        for (Role role : userRoles)
            if (role.isResourceGranted(resource, permission) && resource.setResource(value))
                return true;
        return false;
    }

    public List<String> getPermissions() {
        List<String> out = new ArrayList<>();
        String str;

        for (Role role : userRoles)
            if ( !(str = role.getResourcePermission()).isEmpty() )
                out.add(str);

        return out;
    }

    public void removeAllRoles() {
        userRoles.clear();
    }

    public void changeRoleName(String oldName, String newName) {
        userRoles.stream().filter(role -> role.getRoleName().equals(oldName)).forEach(role -> role.setRoleName(newName));
    }

    public void addPerms(String role, String resource, String perms) {
        userRoles.stream()
                .filter(userRole -> userRole.getRoleName().equals(role))
                .forEach(userRole -> userRole.addPerms(resource, perms));
    }

    public void removePerms(String role, String resource, String perms) {
        userRoles.stream()
                .filter(userRole -> userRole.getRoleName().equals(role))
                .forEach(userRole -> userRole.removePerms(resource, perms));
    }

    public void removeAllPerms(String role, String resource) {
        userRoles.stream()
                .filter(userRole -> userRole.getRoleName().equals(role))
                .forEach(userRole -> userRole.removeAllPerms(resource));
    }

    public void changeResourceName(String role, String oldResource, String newResource) {
        userRoles.stream()
                .filter(userRole -> userRole.getRoleName().equals(role))
                .forEach(userRole -> userRole.changeResourceName(oldResource, newResource));
    }

    public void associateRoleWithRole(String role, String associativeRole) {
        userRoles.stream()
                .filter(userRole -> userRole.getRoleName().equals(role))
                .forEach(userRole -> userRole.associateRole(associativeRole));
    }

    public void deleteAssociatedRole(String role) {
        List<Role> newRoles = new ArrayList<>();
        for (int i = 0; i < userRoles.size(); i++) {
            final Role newRole = new Role(userRoles.get(i).getRoleName());
            newRoles.add(newRole);
        }
        userRoles = newRoles;
    }

    public void updateRolePerms(String role, String resource, String permissions) {
        userRoles.stream()
                .filter(userRole -> userRole.getRoleName().equals(role))
                .forEach(userRole -> userRole.updatePermissions(resource, permissions));
    }
}
