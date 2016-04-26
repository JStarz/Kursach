package com.company.base.iam;

import com.company.json.JSON;
import com.company.json.JSONConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Resource {

    private Socket toResourceServer;
    private BufferedReader inStream;
    private PrintWriter outStream;

    String resource;

    public Resource(String resourceName) {
        this.resource = resourceName;
    }

    public String getResourceValueFromResourceServer() {
        String responsedResource = null;
        try {
            toResourceServer = new Socket("localhost", 9002);
            inStream = new BufferedReader(new InputStreamReader(toResourceServer.getInputStream()));
            outStream = new PrintWriter(toResourceServer.getOutputStream(), true);

            final JSON request = new JSON();
            request.addTypeContent(JSONConstants.GetResource);
            request.addBodyContent(JSONConstants.Resource, resource);
            outStream.println(request.getStringRepresentation());

            responsedResource = inStream.readLine();
        } catch (Exception e) {
            System.out.println("Unable connect to resource server!");
        } finally {
            try {
                inStream.close();
                outStream.close();
                toResourceServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responsedResource;
    }

    public boolean setResource(String newValue) {
        try {
            toResourceServer = new Socket("localhost", 9002);
            inStream = new BufferedReader(new InputStreamReader(toResourceServer.getInputStream()));
            outStream = new PrintWriter(toResourceServer.getOutputStream(), true);

            final JSON request = new JSON();
            request.addTypeContent(JSONConstants.SetResource);
            request.addBodyContent(JSONConstants.Resource, resource);
            request.addBodyContent(JSONConstants.Value, newValue);
            outStream.println(request.getStringRepresentation());

            return true;
        } catch (Exception e) {
            System.out.println("Unable connect to resource server!");
        } finally {
            try {
                inStream.close();
                outStream.close();
                toResourceServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource1 = (Resource) o;

        return resource.equals(resource1.resource);

    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }
}
