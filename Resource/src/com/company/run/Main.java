package com.company.run;

import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static ConcurrentHashMap<String, String> resources;
    private static ConcurrentHashMap<String, String> path = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> names = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        final ServerSocket resourceServer = new ServerSocket(9002);
        resources = createResourceMap();

        if (resources != null) {
            System.out.println("READY TO LISTEN RESOURCE REQUESTS");
            while (true) {
                final Socket newConnection = resourceServer.accept();

                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        BufferedReader inStream = null;
                        PrintWriter outStream = null;
                        try {
                            inStream = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
                            outStream = new PrintWriter(newConnection.getOutputStream(), true);

                            final JSON request = new JSON(inStream.readLine());

                            if (request.getTypeValue().equals(JSONConstants.GetResource)) {
                                final String resource = request.getValueForKey(JSONConstants.Resource);
                                final String outputResourceValue = resources.get(resource);

                                outStream.println(outputResourceValue);
                                System.out.println("I give resource <" + resource + "> with value <" + outputResourceValue + ">");
                            }
                            if (request.getTypeValue().equals(JSONConstants.SetResource)) {
                                final String resourceKey = request.getValueForKey(JSONConstants.Resource);
                                final String resourceValue = request.getValueForKey(JSONConstants.Value);

                                resources.replace(resourceKey, resourceValue);
                                updateDBWithNewResourceValue(resourceKey, resourceValue);
                            }
                            if (request.getTypeValue().equals(JSONConstants.GetResourcePath)) {
                                final String resource = request.getValueForKey(JSONConstants.Resource);
                                final String resourcePath = path.get(resource);
                                final String resourceName = names.get(resource);

                                outStream.println(resourcePath);
                                outStream.println(resourceName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                assert inStream != null;
                                inStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            assert outStream != null;
                            outStream.close();
                            try {
                                newConnection.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        } else System.out.println("Resources not instant!");
    }

    private static ConcurrentHashMap<String, String> createResourceMap() {

        Connection connection;
        String baseDir = "D:\\Other\\Kursovaya\\Resources\\";

        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:orcl",
                    "c##vasya",
                    "Jvas1993"
            );

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return null;

        }

        if (connection != null) {

            try {
                Statement stmt = connection.createStatement();

                ResultSet rs = stmt.executeQuery("select * from resources");

                ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

                while(rs.next()) {
                    final String resourceKey = rs.getString(1);
                    final String resourceName = rs.getString(2);
                    final String resourcePath = rs.getString(3);

                    final String resourceValue = baseDir + resourcePath + resourceName;

                    try (BufferedReader br = new BufferedReader(new FileReader(new File(resourceValue)))) {
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();

                        while (line != null) {
                            sb.append(line);
                            sb.append(System.lineSeparator());
                            line = br.readLine();
                        }

                        map.put(resourceKey, sb.toString());
                    }

                    path.put(resourceKey, resourcePath);
                    names.put(resourceKey, resourceName);
                }

                connection.close();

                return map;
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to make connection!");
        }

        return null;
    }

    private static void updateDBWithNewResourceValue(String resourceKey, String resourceValue) {

        Connection connection;

        try {

            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:orcl",
                    "c##vasya",
                    "Jvas1993"
            );

        } catch (SQLException e) {

            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;

        }

        if (connection != null) {

            try {
                ResultSet rs = connection.createStatement().executeQuery(
                        "select value from resources where key = \'" + resourceKey + "\'"
                );

                while (rs.next()) {
                    try (PrintWriter pw = new PrintWriter(new FileWriter(new File(rs.getString(1))))) {
                        pw.print(resourceValue);
                    }
                }

                connection.close();

            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to make connection!");
        }
    }
}
