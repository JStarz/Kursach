package com.company.dao;

import com.company.core.Client;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseUtils {

    private static Connection connection;

    public static void prepare() {
        prepareDriver();
        prepareConnection();
    }

    public static void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void finallyBlock(Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String readClientRoles(Client client) {
        Statement stmt = null;
        ResultSet rs = null;
        String out = null;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select * from persons");

                while(rs.next()) {
                    final String currentUserName = rs.getString(1),
                            currentUserPassword = rs.getString(2),
                            currentUserRoles = rs.getString(4);
                    if (client.getName().equals(currentUserName) && client.getPassword().equals(currentUserPassword)) {
                        out = currentUserRoles;
                        break;
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static String readClientRoles(String clientName) {
        Statement stmt = null;
        ResultSet rs = null;
        String out = null;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select * from persons");

                while(rs.next()) {
                    final String currentUserName = rs.getString(1), currentUserRoles = rs.getString(4);
                    if (clientName.equals(currentUserName)) {
                        out = currentUserRoles;
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean addUser(String name, String password, String admin) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (!userExists(name)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "insert into persons(name, password, type) values (\'" + name + "\', \'" + password + "\', \'" + admin + "\')"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean deleteUser(String name) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (userExists(name)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "DELETE FROM persons WHERE name = \'" + name + "\'"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean updateUser(String oldName, String newName, String newPassword, String newType) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (userExists(oldName)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "update persons set name = \'" + newName + "\', password = \'" + newPassword + "\', type = \'" + newType + "\' where name = \'" + oldName +"\'"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static List<String> getUsers() {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> out = null;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select * from persons");
                out = new ArrayList<>();
                while(rs.next()) out.add(rs.getString(1));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean isAdmin(Client client) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select * from persons");

                while(rs.next()) {
                    final String currentUserName = rs.getString(1), currentUserPassword = rs.getString(2);
                    if (client.getName().equals(currentUserName) && client.getPassword().equals(currentUserPassword)) {
                        out = rs.getString(3).equals("ADMIN");
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static String[] getRolePermsAndResource(String roleName) {
        Statement stmt = null;
        ResultSet rs = null;
        String[] out = new String[2];

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select * from matrix");
                while(rs.next()) {
                    if (roleName.equals(rs.getString(1))) {
                        out[0] = rs.getString(2); // perms
                        out[1] = rs.getString(3); // resource
                        break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Failed to make connection!");
        }
        return out;
    }

    public static boolean createRole(String roleName, String resource, String perm) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (!roleExists(roleName)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "insert into matrix(role, permission, resurs) values (\'" + roleName + "\', \'" + perm + "\', \'" + resource + "\')"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean deleteRole(String roleName) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(roleName)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "delete from matrix where role = \'" + roleName + "\'"
                    );
                    finallyBlock(stmt, rs);

                    final Map<String, String> userAndRole = getUserWithRole(roleName);

                    if (userAndRole != null) {
                        for (Map.Entry<String, String> userWithRole : userAndRole.entrySet()) {
                            final String oldRole = userWithRole.getValue();
                            final String oldName = userWithRole.getKey();
                            final String newRole = getPreparedRoleName(oldRole, roleName);

                            stmt = connection.createStatement();
                            rs = stmt.executeQuery(
                                    "update persons set role = \'" + newRole + "\' where name = \'" + oldName + "\'"
                            );
                            finallyBlock(stmt, rs);
                        }
                    }

                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "select * from matrix"
                    );

                    while (rs.next()) {
                        final String role = rs.getString(1);
                        final String associativeRole = rs.getString(4);

                        if (associativeRole != null && associativeRole.contains(roleName)) {
                            final String newAssociative;
                            if (associativeRole.contains("|")) {
                                if (associativeRole.indexOf(roleName) > 0) {
                                    newAssociative = associativeRole.replace("|" + roleName, "");
                                } else {
                                    newAssociative = associativeRole.replace(roleName + "|", "");
                                }
                            } else {
                                newAssociative = "";
                            }
                            connection.createStatement().executeQuery(
                                    "update matrix set associative_role = \'" + newAssociative + "\' where role = \'" + role + "\'"
                            );
                        }
                    }

                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean associateRoleWithUser(String user, String role) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role) && userExists(user)) {
                    final String roles = readClientRoles(user);
                    if (roles == null) {
                        stmt = connection.createStatement();
                        rs = stmt.executeQuery(
                                "update persons set role = \'" + role + "\' where name = \'" + user + "\'"
                        );
                        out = true;
                    } else {
                        if (!roles.contains(role)) {
                            final String newRoleField = roles.length() > 0 ? roles + "|" + role : role;
                            stmt = connection.createStatement();
                            rs = stmt.executeQuery(
                                    "update persons set role = \'" + newRoleField + "\' where name = \'" + user + "\'"
                            );
                            out = true;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }

        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static Map<String, String> getUserWithRole(String roleName) {
        Map<String, String> usersAndRoles = new HashMap<>();
        Statement stmt = null;
        ResultSet rs = null;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery(
                        "select * from persons"
                );
                while(rs.next()) {
                    final String name = rs.getString(1);
                    final String role = rs.getString(4);
                    if (role.contains(roleName)) {
                        usersAndRoles.put(name, role);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return usersAndRoles.size() > 0 ? usersAndRoles : null;
    }

    public static List<String> getRoles() {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> roles = new ArrayList<>();

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select * from matrix");
                while(rs.next()) roles.add(rs.getString(1));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return roles.size() > 0 ? roles : null;
    }

    private static boolean roleExists(String roleName) {
        final List<String> roles = getRoles();
        return roles != null && roles.contains(roleName);
    }

    private static boolean userExists(String name) {
        final List<String> users = getUsers();
        return users != null && users.contains(name);
    }

    private static String getPreparedRoleName(String oldRole, String roleName) {
        if (oldRole.indexOf(roleName) > 0) {
            return oldRole.replace("|" + roleName, "");
        } else {
            if (oldRole.contains("|")) {
                return oldRole.replace(roleName + "|", "");
            } else {
                return oldRole.replace(roleName, "");
            }
        }
    }

    private static String getRemovedRoleName(String oldRoleField, String roleName) {
        if (oldRoleField.contains("|")) {
            if (oldRoleField.indexOf(roleName) > 0) {
                return oldRoleField.replace("|" + roleName, "");
            } else {
                return oldRoleField.replace(roleName + "|", "");
            }
        } else {
            return "";
        }
    }

    private static void prepareDriver() {
        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("DATABASE UTILS: Where is your Oracle JDBC Driver?");
            e.printStackTrace();

        }
    }

    private static void prepareConnection() {
        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:orcl",
                    "c##vasya",
                    "Jvas1993"
            );

        } catch (SQLException e) {

            System.out.println("DATABASE UTILS: Connection Failed! Check output console");
            e.printStackTrace();

        }
    }

    public static boolean deleteUserRole(String userName, String roleName) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(roleName) && userExists(userName)) {
                    final String userRoles = readClientRoles(userName);
                    if (userRoles != null && userRoles.contains(roleName)) {
                        final String newRoleField = getRemovedRoleName(userRoles, roleName);
                        stmt = connection.createStatement();
                        rs = stmt.executeQuery(
                                "update persons set role = \'" + newRoleField + "\' where name = \'" + userName + "\'"
                        );
                        out = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean deleteUserRoles(String user) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (userExists(user)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "update persons set role = \'\' where name = \'" + user + "\'"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static String readAssociativeRolesForRole(String role) {
        Statement stmt = null;
        ResultSet rs = null;
        String out = null;

        if (connection != null) {
            try {
                if (roleExists(role)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "select ASSOCIATIVE_ROLE from matrix where role = \'" + role + "\'"
                    );
                    if (rs.next()) {
                        out = rs.getString(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }
        return out;
    }

    public static boolean changeRoleName(String oldRoleName, String newRoleName) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(oldRoleName) && !roleExists(newRoleName)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "update matrix set role = \'" + newRoleName + "\' where role = \'" + oldRoleName + "\'"
                    );
                    finallyBlock(stmt, rs);

                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "select * from matrix"
                    );

                    while (rs.next()) {
                        final String role = rs.getString(1);
                        final String associativeRole = rs.getString(4);

                        if (associativeRole != null && associativeRole.contains(oldRoleName)) {
                            final String newAssociative = associativeRole.replace(oldRoleName, newRoleName);
                            connection.createStatement().executeQuery(
                                    "update matrix set associative_role = \'" + newAssociative + "\' where role = \'" + role + "\'"
                            );
                        }
                    }

                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean addPermissionsToRole(String role, String permissions) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "select permission from matrix where role = \'" + role + "\'"
                    );

                    if (rs.next()) {
                        final String perms = rs.getString(1);
                        finallyBlock(stmt, rs);

                        final List<String> currentPermissions = getPermsFromString(perms);
                        final List<String> permissionsForAppend = getPermsFromString(permissions);

                        currentPermissions.addAll(
                                permissionsForAppend.stream().filter(perm -> !currentPermissions.contains(perm)).collect(Collectors.toList())
                        );

                        final String newPerms = createStringFromPermsList(
                                currentPermissions
                        );

                        if (!newPerms.isEmpty()) {
                            stmt = connection.createStatement();
                            rs = stmt.executeQuery(
                                    "update matrix set permission = \'" + newPerms + "\' where role = \'" + role + "\'"
                            );
                        } else {
                            stmt = connection.createStatement();
                            rs = stmt.executeQuery(
                                    "update matrix set permission = \'\' where role = \'" + role + "\'"
                            );
                        }

                        out = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    private static List<String> getPermsFromString(String perms) {
        List<String> out = new ArrayList<>();
        if (perms != null) {
            if (perms.contains("|")) {
                Collections.addAll(out, perms.split("\\|"));
            } else {
                out.add(perms);
            }
        }
        return out;
    }

    private static String createStringFromPermsList(List<String> perms) {
        String out = "";
        for (int i = 0; perms != null && i < perms.size(); i++) {
            if (i == perms.size() - 1) out += perms.get(i);
            else out += perms.get(i) + "|";
        }
        return out;
    }

    public static boolean deletePermissionsFromRole(String role, String permissions) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "select permission from matrix where role = \'" + role + "\'"
                    );

                    if (rs.next()) {
                        final String perms = rs.getString(1);
                        finallyBlock(stmt, rs);

                        final List<String> currentPermissions = getPermsFromString(perms);
                        final List<String> permissionsForDelete = getPermsFromString(permissions);

                        currentPermissions.removeAll(
                                permissionsForDelete.stream().filter(currentPermissions::contains).collect(Collectors.toList())
                        );

                        final String newPerms = createStringFromPermsList(
                                currentPermissions
                        );

                        if (!newPerms.isEmpty()) {
                            stmt = connection.createStatement();
                            rs = stmt.executeQuery(
                                    "update matrix set permission = \'" + newPerms + "\' where role = \'" + role + "\'"
                            );
                        } else {
                            stmt = connection.createStatement();
                            rs = stmt.executeQuery(
                                    "update matrix set permission = \'\' where role = \'" + role + "\'"
                            );
                        }

                        out = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean deleteAllPermissionsFromRole(String role) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "update matrix set permission = \'\' where role = \'" + role + "\'"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean changeResursFromRole(String role, String newResource) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role)) {
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "update matrix set resurs = \'" + newResource + "\' where role = \'" + role + "\'"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean associateRoleWithRole(String role, String associativeRole) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role) && roleExists(associativeRole)) {
                    String oldAssociativeField = readAssociativeRolesForRole(role);

                    final String newAssociativeField;
                    if (oldAssociativeField != null) {
                        newAssociativeField = oldAssociativeField.isEmpty() ? associativeRole : oldAssociativeField + "|" + associativeRole;
                    } else {
                        newAssociativeField = associativeRole;
                    }

                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(
                            "update matrix set associative_role = \'" + newAssociativeField + "\' where role = \'" + role + "\'"
                    );
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean deleteAssociatedRole(String role, String associativeRole) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role) && roleExists(associativeRole)) {
                    String ass = readAssociativeRolesForRole(role);

                    if (ass != null && ass.contains(associativeRole)) {
                        final String newAssociativeField;
                        if (ass.indexOf(associativeRole) > 0) {
                            newAssociativeField = ass.replace("|" + associativeRole, "");
                        } else {
                            newAssociativeField = ass.replace(
                                    associativeRole + (ass.contains("|") ? "|" : ""),
                                    ""
                            );
                        }

                        stmt = connection.createStatement();
                        rs = stmt.executeQuery(
                                "update matrix set associative_role = \'" + newAssociativeField + "\' where role = \'" + role + "\'"
                        );
                        out = true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static boolean deleteAllAssociatedRoles(String role) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                if (roleExists(role)) {
                    final String ass = readAssociativeRolesForRole(role);
                    if (ass != null) {
                        stmt = connection.createStatement();
                        rs = stmt.executeQuery(
                                "update matrix set associative_role = \'\' where role = \'" + role + "\'"
                        );
                    }
                    out = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        } else {
            System.out.println("Prepare connection before query!");
        }

        return out;
    }

    public static List<String> getAllResources() {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> resources = null;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select key from resources");
                resources = new ArrayList<>();
                while(rs.next()) resources.add(rs.getString(1));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        }

        return resources;
    }

    public static List<String> getDistinctRolesForUser(String name) {
        if (connection != null) {
            final List<String> roles = getRoles();
            final List<String> userRoles = getPermsFromString(readClientRoles(name));

            if (roles != null) {
                return roles.stream().filter(s -> !userRoles.contains(s)).collect(Collectors.toList());
            }
        }
        return null;
    }

    public static boolean managePermissions(String role, String resource, String perms) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean out = false;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("update matrix set permission = \'" + perms + "\' where role = \'" + role + "\'");
                out = true;
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        }

        return out;
    }

    public static List<String> getDistinctResourcesForRole(String role) {
        final String resource = getRolePermsAndResource(role)[1];
        Statement stmt = null;
        ResultSet rs = null;
        List<String> resources = null;

        if (connection != null) {
            try {
                stmt = connection.createStatement();
                rs = stmt.executeQuery("select key from resources where key != \'" + resource + "\'");
                resources = new ArrayList<>();
                while (rs.next()) resources.add(rs.getString(1));
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                finallyBlock(stmt, rs);
            }
        }

        return resources;
    }

    public static List<String> getAvailableAssociativeRoles(String role) {
        final List<String> currentAssociativeRoles = getPermsFromString(readAssociativeRolesForRole(role));
        final List<String> allRoles = getRoles();

        if (allRoles != null)
            return allRoles.stream().filter(currRole -> !currentAssociativeRoles.contains(currRole) && !currRole.equals(role)).collect(Collectors.toList());

        return null;
    }

    public static List<String> getAssociativeRoles(String role) {
        final String ass = readAssociativeRolesForRole(role);
        final List<String> out = new ArrayList<>();

        if (ass != null) {
            if (ass.contains("|")) Collections.addAll(out, ass.split("\\|"));
            else out.add(ass);
        }

        return out;
    }
}
