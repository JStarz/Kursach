package com.company.dao;

import java.sql.*;

public class DatabaseUtils {

    public static boolean checkUser(String name, String password) {

        try {

            Class.forName("oracle.jdbc.driver.OracleDriver");

        } catch (ClassNotFoundException e) {

            System.out.println("DATABASE UTILS: Where is your Oracle JDBC Driver?");
            e.printStackTrace();
            return false;

        }

        Connection connection;

        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:orcl",
                    "c##vasya",
                    "Jvas1993"
            );

        } catch (SQLException e) {

            System.out.println("DATABASE UTILS: Connection Failed! Check output console");
            e.printStackTrace();
            return false;

        }

        if (connection != null) {

            try {
                Statement stmt = connection.createStatement();

                ResultSet rs = stmt.executeQuery("select * from persons");

                while(rs.next()) {
                    final String currentUserName = rs.getString(1), currentUserPassword = rs.getString(2);
                    if (name.equals(currentUserName) && password.equals(currentUserPassword)) return true;
                }

                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("DATABASE UTILS: Failed to make connection!");
        }

        return false;
    }
}
