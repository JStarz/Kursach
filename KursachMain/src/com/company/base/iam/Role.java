package com.company.base.iam;

import com.company.dao.DatabaseUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Role {

    String roleName;
    ConcurrentHashMap<Resource, List<Permission>> permsForResource;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        return roleName.equals(role.roleName);

    }

    @Override
    public int hashCode() {
        return roleName.hashCode();
    }

    public Role(String roleName) {
        this.permsForResource = new ConcurrentHashMap<>();

        readRolePerms(roleName);
    }

    private void readAssociativeRolePerms(String roleName) {

        final String associativeRoles = DatabaseUtils.readAssociativeRolesForRole(roleName);

        if (associativeRoles != null && !associativeRoles.isEmpty()) {
            if (associativeRoles.contains("|")) {
                for (String associativeRole : associativeRoles.split("\\|")) {
                    final Role role = new Role(associativeRole);
                    role.getPermsForResource().entrySet().stream()
                            .forEach(entry -> {
                                final Resource resource = entry.getKey();
                                final List<Permission> permissions = entry.getValue();
                                List<Permission> currentPermissions = permsForResource.get(resource);

                                if (currentPermissions != null) {
                                    permsForResource.remove(resource);
                                    currentPermissions.addAll(
                                            permissions.stream()
                                                    .filter(perm -> !currentPermissions.contains(perm))
                                                    .collect(Collectors.toList())
                                    );
                                    permsForResource.put(resource, currentPermissions);
                                } else {
                                    permsForResource.put(resource, permissions);
                                }
                            });
                }
            } else {
                final Role role = new Role(associativeRoles);
                role.getPermsForResource().entrySet().stream()
                        .forEach(entry -> {
                            final Resource resource = entry.getKey();
                            final List<Permission> permissions = entry.getValue();
                            List<Permission> currentPermissions = permsForResource.get(resource);

                            if (currentPermissions != null) {
                                permsForResource.remove(resource);
                                currentPermissions.addAll(
                                        permissions.stream()
                                                .filter(perm -> !currentPermissions.contains(perm))
                                                .collect(Collectors.toList())
                                );
                                permsForResource.put(resource, currentPermissions);
                            } else {
                                permsForResource.put(resource, permissions);
                            }
                        });
            }
        }
    }

    private void readRolePerms(String roleName) {

        final String[] fromDB = DatabaseUtils.getRolePermsAndResource(roleName);

        if (fromDB != null) {
            final Resource resource = new Resource(fromDB[1]);
            List<Permission> permissions = new ArrayList<>();

            final String perms = fromDB[0];
            if (perms.contains("|")) {
                for (String perm : perms.split("\\|")) {
                    final Permission permission = new Permission(perm);
                    permissions.add(permission);
                }
            } else {
                permissions.add(new Permission(perms));
            }

            permsForResource.put(resource, permissions);
            readAssociativeRolePerms(roleName);
            this.roleName = roleName;
        }
    }

    private void updatePermissionListForResource(Resource resource, List<Permission> newPerms) {
        final Resource myEqualResource = getEqualResource(resource);
        if (myEqualResource != null) {
            final List<Permission> myPerms = permsForResource.get(myEqualResource);
            myPerms.addAll(newPerms);
            permsForResource.remove(myEqualResource);
            permsForResource.put(myEqualResource, myPerms);
        }
    }

    private Resource getEqualResource(Resource resource) {
        for (Resource res : permsForResource.keySet())
            if (res.equals(resource))
                return res;
        return null;
    }

    public String getRoleName() {
        return roleName;
    }

    public ConcurrentHashMap<Resource, List<Permission>> getPermsForResource() {
        return permsForResource;
    }

    public boolean isResourceGranted(Resource resource, Permission permission) {
        for (Map.Entry<Resource, List<Permission>> map : permsForResource.entrySet())
            if (map.getKey().equals(resource))
                return map.getValue().contains(permission);
        return false;
    }

    public boolean isResourceGranted(Resource resource) {
        for (Map.Entry<Resource, List<Permission>> map : permsForResource.entrySet())
            if (map.getKey().equals(resource))
                return true;
        return false;
    }

    public String getResourcePermission() {
        String out = "";
        for (Map.Entry<Resource, List<Permission>> entry : permsForResource.entrySet()) {
            out += entry.getKey().resource + ":";
            for (Permission permission : entry.getValue()) {
                out += permission.permission + "|";
            }
            if (out.charAt(out.length() - 1) == '|')
                out = out.substring(0, out.length() - 1);
            out += ";";
        }
        if (out.charAt(out.length() - 1) == ';')
            out = out.substring(0, out.length() - 1);
        return out;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void addPerms(String resource, String perms) {
        List<Permission> newPerms = new ArrayList<>();
        if (perms.contains("|")) {
            for (String perm : perms.split("\\|")) {
                newPerms.add(new Permission(perm));
            }
        } else {
            newPerms.add(new Permission(perms));
        }

        permsForResource.entrySet().stream()
                .filter(entry -> entry.getKey().resource.equals(resource))
                .forEach(
                        entry -> entry.getValue().addAll(
                                newPerms.stream().filter(
                                        item -> !entry.getValue().contains(item)).collect(Collectors.toList()
                                )
                        )
                );
    }

    public void removePerms(String resource, String perms) {
        List<Permission> newPerms = new ArrayList<>();
        if (perms.contains("|")) {
            for (String perm : perms.split("\\|")) {
                newPerms.add(new Permission(perm));
            }
        } else {
            newPerms.add(new Permission(perms));
        }

        permsForResource.entrySet().stream()
                .filter(entry -> entry.getKey().resource.equals(resource))
                .forEach(
                        entry -> entry.getValue().removeAll(
                                newPerms.stream().filter(
                                        item -> entry.getValue().contains(item)).collect(Collectors.toList()
                                )
                        )
                );
    }

    public void removeAllPerms(String resource) {
        permsForResource.entrySet().stream()
                .filter(entry -> entry.getKey().resource.equals(resource))
                .forEach(entry -> entry.getValue().clear());
    }

    public void changeResourceName(String oldName, String newName) {
        permsForResource.forEach((key, value) -> {
            if (key.resource.equals(oldName)) key.resource = newName;
        });
    }

    public void associateRole(String roleName) {
        final Role role = new Role(roleName);

        role.getPermsForResource()
                .forEach((key, value) -> {
                    List<Permission> currentPermissions = permsForResource.get(key);

                    if (currentPermissions != null) {
                        permsForResource.remove(key);
                        currentPermissions.addAll(
                                value.stream()
                                        .filter(perm -> !currentPermissions.contains(perm))
                                        .collect(Collectors.toList())
                        );
                        permsForResource.put(key, currentPermissions);
                    } else {
                        permsForResource.put(key, value);
                    }
                });
    }

    public void updatePermissions(String resource, String permissions) {
        final Resource findedResource = permsForResource.entrySet().stream().filter(resourceListEntry -> resourceListEntry.getKey().resource.equals(resource)).findFirst().get().getKey();
        final List<Permission> perms = new ArrayList<>();
        for (String newPerm : permissions.split("\\|")) {
            perms.add(new Permission(newPerm));
        }
        permsForResource.put(findedResource, perms);
    }
}
