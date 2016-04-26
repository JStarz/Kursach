package com.company.run;

import java.net.Socket;
import java.util.Scanner;

public class Main {

    final static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        final Admin admin = new Admin(new Socket("localhost", 9001));

        System.out.println("Name: ");
        final String name = in.nextLine();
        System.out.println("Password: ");
        final String password = in.nextLine();

        if (admin.auth(name, password)) {
            cycle: while (true) {
                printMenu();

                final int option = in.nextInt();

                switch (option) {
                    case 1:
                        System.out.println("New user name: ");
                        final String createUserName = in.next();
                        System.out.println("New user password: ");
                        final String createUserPassword = in.next();
                        System.out.println("Write user type (ADMIN or REGISTER): ");
                        final String createUserType = in.next();
                        admin.createUser(createUserName, createUserPassword, createUserType);
                        break;

                    case 2:
                        System.out.println("User name for delete: ");
                        final String deleteUserName = in.next();
                        admin.deleteUser(deleteUserName);
                        break;

                    case 3:
                        System.out.println("User name for changing credentials: ");
                        final String oldUserName = in.next();
                        System.out.println("New user name: ");
                        final String newUserName = in.next();
                        System.out.println("New user password: ");
                        final String newUserPassword = in.next();
                        System.out.println("New user type: ");
                        final String newUserType = in.next();
                        admin.changeUserCredentials(oldUserName, newUserName, newUserPassword, newUserType);
                        break;

                    case 4:
                        System.out.println("User name: ");
                        final String userNameForRole = in.next();
                        System.out.println("Role name: ");
                        final String roleNameForUser = in.next();
                        admin.associateRoleWithUser(userNameForRole, roleNameForUser);
                        break;

                    case 5:
                        System.out.println("User name: ");
                        final String userNameForRoleRemoving = in.next();
                        System.out.println("Role name: ");
                        final String roleNameForRemovingFromUser = in.next();
                        admin.deleteUserRole(userNameForRoleRemoving, roleNameForRemovingFromUser);
                        break;

                    case 6:
                        System.out.println("User name: ");
                        final String userNameForRolesClear = in.next();
                        admin.deleteUserRoles(userNameForRolesClear);
                        break;

                    case 7:
                        admin.getUsers();
                        break;

                    case 8:
                        admin.getUsersPermissions();
                        break;

                    case 9:
                        System.out.println("User name: ");
                        final String userNameForReadingPerms = in.next();
                        admin.getUserPermissions(userNameForReadingPerms);
                        break;

                    case 10:
                        System.out.println("New role name: ");
                        final String newRoleName = in.next();
                        System.out.println("Role resource: ");
                        final String newRoleResource = in.next();
                        System.out.println("New role permissions: ");
                        final String newRolePermissions = in.next();
                        admin.createRole(newRoleName, newRoleResource, newRolePermissions);
                        break;

                    case 11:
                        System.out.println("Role name: ");
                        final String roleNameForRemoving = in.next();
                        admin.deleteRole(roleNameForRemoving);
                        break;

                    case 12:
                        System.out.println("Role name: ");
                        final String roleNameForRename = in.next();
                        System.out.println("New role name: ");
                        final String newRoleNameForRename = in.next();
                        admin.changeRoleName(roleNameForRename, newRoleNameForRename);
                        break;

                    case 13:
                        System.out.println("Role name: ");
                        final String roleNameForAddPerms = in.next();
                        System.out.println("Resource name: ");
                        final String resourceNameForAddPerms = in.next();
                        System.out.println("Permissions: ");
                        final String addPermissions = in.next();
                        admin.addPermissionsToRole(roleNameForAddPerms, resourceNameForAddPerms, addPermissions);
                        break;

                    case 14:
                        System.out.println("Role name: ");
                        final String roleNameForDelPerms = in.next();
                        System.out.println("Resource name: ");
                        final String resourceNameForDelPerms = in.next();
                        System.out.println("Permissions: ");
                        final String delPermissions = in.next();
                        admin.deletePermissionsFromRole(roleNameForDelPerms, resourceNameForDelPerms, delPermissions);
                        break;

                    case 15:
                        System.out.println("Role name: ");
                        final String roleNameForDelAllPerms = in.next();
                        System.out.println("Resource name: ");
                        final String resourceNameForDelAllPerms = in.next();
                        admin.deleteAllPermissionsFromRole(roleNameForDelAllPerms, resourceNameForDelAllPerms);
                        break;

                    case 16:
                        System.out.println("Role name: ");
                        final String roleNameForChangeResourceName = in.next();
                        System.out.println("Resource name: ");
                        final String oldResourceName = in.next();
                        System.out.println("New resource name: ");
                        final String newResourceName = in.next();
                        admin.changeResursFromRole(roleNameForChangeResourceName, oldResourceName, newResourceName);
                        break;

                    case 17:
                        System.out.println("Role name: ");
                        final String roleNameForAssociate = in.next();
                        System.out.println("Associative role name: ");
                        final String associativeRoleName = in.next();
                        admin.associateRoleWithRole(roleNameForAssociate,associativeRoleName);
                        break;

                    case 18:
                        System.out.println("Role name: ");
                        final String roleNameForDeleteAssociate = in.next();
                        System.out.println("Associative role name: ");
                        final String associativeRoleNameForDelete = in.next();
                        admin.deleteAssociatedRole(roleNameForDeleteAssociate, associativeRoleNameForDelete);
                        break;

                    case 19:
                        System.out.println("Role name: ");
                        final String roleNameForDeleteAllAssociatedRoles = in.next();
                        admin.deleteAllAssociatedRoles(roleNameForDeleteAllAssociatedRoles);
                        break;

                    case 20:
                        break cycle;

                    default:
                        System.out.println(">>>> Incorrect input. Try again! <<<<");
                        break;
                }
            }
        } else {
            admin.close();
        }
    }

    private static void printMenu() {
        System.out.println("\nSelect command:");
        System.out.println("1. Create user");
        System.out.println("2. Delete user");
        System.out.println("3. Change user credentials");
        System.out.println("4. Associate role with user");
        System.out.println("5. Delete user role");
        System.out.println("6. Delete user roles");
        System.out.println("7. Get users");
        System.out.println("8. Get users permissions");
        System.out.println("9. Get user permissions");
        System.out.println("10. Create role");
        System.out.println("11. Delete role");
        System.out.println("12. Change role name");
        System.out.println("13. Add permissions to role");
        System.out.println("14. Delete permissions from role");
        System.out.println("15. Delete all permissions from role");
        System.out.println("16. Change resurs from role");
        System.out.println("17. Associate role with role");
        System.out.println("18. Delete associated role");
        System.out.println("19. Delete all associated roles");
        System.out.println("20. Quit");
    }
}
