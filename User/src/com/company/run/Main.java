package com.company.run;

import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    final static Scanner in = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        final Client client = new Client(new Socket("localhost", 9001));

        System.out.println("Name: ");
        final String name = in.nextLine();
        System.out.println("Password: ");
        final String password = in.nextLine();

        if (client.auth(name, password)) {
            cycle: while (true) {
                System.out.println("\nSelect command:");
                System.out.println("1. Read resource");
                System.out.println("2. Write resource");
                System.out.println("3. Get permissions");
                System.out.println("4. Quit");

                final int option = in.nextInt();

                switch (option) {
                    case 1:
                        System.out.println("Write resource name to read: ");
                        final String readResource = in.next();
                        client.readResource(readResource);
                        break;

                    case 2:
                        System.out.println("Write resource name to write: ");
                        final String writeResource = in.next();
                        System.out.println("Write new resource value: ");
                        final String newResourceValue = in.next();
                        client.writeResource(writeResource, newResourceValue);
                        break;

                    case 3:
                        client.getPermissions();
                        break;

                    case 4:
                        break cycle;

                    default:
                        System.out.println(">>>> Incorrect input. Try again! <<<<");
                        break;
                }

                in.nextLine();
            }
        } else {
            client.close();
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    Socket toServer = new Socket("localhost", 9001);
                    final BufferedReader inStream = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
                    final PrintWriter outStream = new PrintWriter(toServer.getOutputStream(), true);

                    JSON json = new JSON();
                    json.addTypeContent(JSONConstants.Auth);
                    json.addBodyContent(JSONConstants.Name, "Vova");
                    json.addBodyContent(JSONConstants.Password, "123456");

                    outStream.println(json.getStringRepresentation());

                    final String input = inStream.readLine();
                    JSON newJson = new JSON(input);
                    if (newJson.getTypeValue().equals(JSONConstants.AuthFailed)) {
                        System.out.println(newJson.getValueForKey(JSONConstants.Reason));
                    }
                    if (newJson.getTypeValue().equals(JSONConstants.TokenUpdate)) {
                        System.out.println("New token = " + newJson.getValueForKey(JSONConstants.NewToken));
                        final String token = newJson.getValueForKey(JSONConstants.NewToken);

                        final JSON getResource = new JSON();
                        getResource.addBodyContent(JSONConstants.Token, token);
                        getResource.addTypeContent(JSONConstants.GetResource);
                        getResource.addBodyContent(JSONConstants.Resource, "CAR");

                        outStream.println(getResource.getStringRepresentation());

                        final JSON getResourceResponse = new JSON(inStream.readLine());
                        if (getResourceResponse.getTypeValue().equals(JSONConstants.TakeResource))
                            System.out.println("Response resource: " + getResourceResponse.getValueForKey(JSONConstants.Value));
                        else System.out.println("Bad response: " + getResourceResponse.getStringRepresentation());

                        final JSON getPerms = new JSON();
                        getPerms.addBodyContent(JSONConstants.Token, token);
                        getPerms.addTypeContent(JSONConstants.GetPermissions);

                        outStream.println(getPerms.getStringRepresentation());

                        final JSON getPermsResponse = new JSON(inStream.readLine());
                        if (getPermsResponse.getTypeValue().equals(JSONConstants.PermissionsList)) {
                            System.out.println("My permission list:");
                            final String permsValue = getPermsResponse.getValueForKey(JSONConstants.Value);
                            if (permsValue.contains(";")) {
                                for (String currPerm : permsValue.split(";")) {
                                    System.out.println(currPerm.replace(":", " => "));
                                }
                            } else {
                                System.out.println(permsValue.replace(":", " => "));
                            }
                        }

                        final JSON getUsers = new JSON();
                        getUsers.addBodyContent(JSONConstants.Token, token);
                        getUsers.addTypeContent(JSONConstants.GetUsers);

                        outStream.println(getUsers.getStringRepresentation());

                        final JSON getUsersResponse = new JSON(inStream.readLine());
                        if (getUsersResponse.getTypeValue().equals(JSONConstants.AllUsers)) {
                            System.out.println("All users list:");
                            final String permsValue = getUsersResponse.getValueForKey(JSONConstants.Value);
                            if (permsValue.contains(";")) {
                                for (String currPerm : permsValue.split(";")) {
                                    System.out.println(currPerm.replace(":", " => "));
                                }
                            } else {
                                System.out.println(permsValue.replace(":", " => "));
                            }
                        } else System.out.println("Bad response: " + getUsersResponse.getStringRepresentation());

                        final JSON getUsersPerms = new JSON();
                        getUsersPerms.addBodyContent(JSONConstants.Token, token);
                        getUsersPerms.addTypeContent(JSONConstants.GetUsersPermissions);

                        outStream.println(getUsersPerms.getStringRepresentation());

                        final JSON getUsersPermsResponse = new JSON(inStream.readLine());
                        if (getUsersPermsResponse.getTypeValue().equals(JSONConstants.AllPermissionsList)) {
                            System.out.println("All permissions list:");
                            final String permsValue = getUsersPermsResponse.getValueForKey(JSONConstants.Value);
                            final String[] users = permsValue.split("<>");
                            for (String user : users) {
                                final String[] userAndPerms = user.split(">>");
                                System.out.println("User " + userAndPerms[0]);

                                if (userAndPerms[1].contains(";")) {
                                    for (String currPerm : userAndPerms[1].split(";")) {
                                        System.out.println(currPerm.replace(":", " => "));
                                    }
                                } else {
                                    System.out.println(userAndPerms[1].replace(":", " => "));
                                }
                            }
                        } else System.out.println("Bad response: " + getUsersPermsResponse.getStringRepresentation());
                    }
                } catch (Exception e) {

                }
            }
        };
    }

}
