package connection;

import javafx.application.Platform;
import json.JSON;
import json.JSONConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Connection {

    final Socket clientSocket;
    final BufferedReader inStream;
    final PrintWriter outStream;

    private String token;

    public ServerAuthResponseDelegate authResponse;
    public DownloadFileDelegate downloadResponse;
    public UserCommandResponseDelegate commandResponse;

    public Connection(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.outStream = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void close() throws IOException {
        inStream.close();
        outStream.close();
        clientSocket.close();
    }

    public void auth(String name, String password) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                JSON json = new JSON();
                json.addTypeContent(JSONConstants.Auth);
                json.addBodyContent(JSONConstants.Name, name);
                json.addBodyContent(JSONConstants.Password, password);

                outStream.println(json.getStringRepresentation());

                try {
                    JSON newJson = new JSON(inStream.readLine());
                    if (newJson.getTypeValue().equals(JSONConstants.AuthFailed)) {
                        if (authResponse != null) {
                            Platform.runLater(() -> authResponse.authFailed(newJson.getValueForKey(JSONConstants.Reason)));
                        }
                    } else if (newJson.getTypeValue().equals(JSONConstants.ConnectionClose)) {
                        if (authResponse != null) {
                            Platform.runLater(() -> authResponse.connectionClose(newJson.getValueForKey(JSONConstants.Reason)));
                        }
                    } else if (newJson.getTypeValue().equals(JSONConstants.TokenUpdate)) {
                        if (authResponse != null) {
                            token = newJson.getValueForKey(JSONConstants.NewToken);
                            Platform.runLater(() -> authResponse.authSuccesful());
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void readResource(String name) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                final JSON getResource = new JSON();
                getResource.addBodyContent(JSONConstants.Token, token);
                getResource.addTypeContent(JSONConstants.GetResource);
                getResource.addBodyContent(JSONConstants.Resource, name);

                outStream.println(getResource.getStringRepresentation());

                try {
                    final JSON getResourceResponse = new JSON(inStream.readLine());
                    if (getResourceResponse.getTypeValue().equals(JSONConstants.TakeResource)) {
                        if (commandResponse != null) {
                            final String resourceValue = getResourceResponse.getValueForKey(JSONConstants.Value);
                            Platform.runLater(() -> commandResponse.readResource(resourceValue, null));
                        }
                    } else if (getResourceResponse.getTypeValue().equals(JSONConstants.Denied)) {
                        final String error = getResourceResponse.getValueForKey(JSONConstants.Reason);
                        Platform.runLater(() -> commandResponse.readResource(null, error));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void writeResource(String writeResource, String newResourceValue) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                final JSON setResource = new JSON();
                setResource.addTypeContent(JSONConstants.SetResource);
                setResource.addBodyContent(JSONConstants.Token, token);
                setResource.addBodyContent(JSONConstants.Resource, writeResource);
                setResource.addBodyContent(JSONConstants.Value, newResourceValue);

                outStream.println(setResource.getStringRepresentation());

                try {
                    final JSON setResourceResponse = new JSON(inStream.readLine());
                    if (setResourceResponse.getTypeValue().equals(JSONConstants.SetResourceResult)) {
                        if (commandResponse != null) {
                            final boolean resourceSetted = setResourceResponse.getValueForKey(JSONConstants.Value).equals("Success");
                            Platform.runLater(() -> commandResponse.writeResource(resourceSetted, null));
                        }
                    } else if (setResourceResponse.getTypeValue().equals(JSONConstants.Denied)) {
                        final String error = setResourceResponse.getValueForKey(JSONConstants.Reason);
                        Platform.runLater(() -> commandResponse.writeResource(false, error));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void getPermissions() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                final JSON getPerms = new JSON();
                getPerms.addBodyContent(JSONConstants.Token, token);
                getPerms.addTypeContent(JSONConstants.GetPermissions);

                outStream.println(getPerms.getStringRepresentation());

                try {
                    final JSON getPermsResponse = new JSON(inStream.readLine());
                    if (getPermsResponse.getTypeValue().equals(JSONConstants.PermissionsList)) {
                        if (commandResponse != null) {
                            final Map<String, String> perms = new HashMap<>();
                            final String permsValue = getPermsResponse.getValueForKey(JSONConstants.Value);
                            if (permsValue.contains(";")) {
                                for (String currPerm : permsValue.split(";")) {
                                    final String[] strs = currPerm.split(":");
                                    perms.put(strs[0], strs[1]);
                                }
                            } else {
                                final String[] strs = permsValue.split(":");
                                perms.put(strs[0], strs[1]);
                            }
                            Platform.runLater(() -> commandResponse.getPermissions(perms, null));
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void startDownload(String resource) {
        new Thread() {
            @Override
            public void run() {
                super.run();

                baseSleep(50);

                final JSON startDownload = new JSON();
                startDownload.addBodyContent(JSONConstants.Token, token);
                startDownload.addBodyContent(JSONConstants.Resource, resource);
                startDownload.addTypeContent(JSONConstants.StartDownload);

                outStream.println(startDownload.getStringRepresentation());

                try {
                    final JSON startDownloadResponse = new JSON(inStream.readLine());
                    if (startDownloadResponse.getTypeValue().equals(JSONConstants.StartDownloadResult)) {
                        if (downloadResponse != null) {
                            final String file = startDownloadResponse.getValueForKey(JSONConstants.Value);
                            Platform.runLater(() -> downloadResponse.receiveFile(resource, file));
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                }
            }
        }.start();
    }

    public void stopDownload(String resource) {
        // TODO: implement
    }

    public void getResourcePath(String resource) {
        synchronized (resource) {
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    baseSleep(50);

                    final JSON getResourcePath = new JSON();
                    getResourcePath.addBodyContent(JSONConstants.Token, token);
                    getResourcePath.addBodyContent(JSONConstants.Resource, resource);
                    getResourcePath.addTypeContent(JSONConstants.GetResourcePath);

                    outStream.println(getResourcePath.getStringRepresentation());

                    try {
                        final JSON getPathResponse = new JSON(inStream.readLine());

                        if (getPathResponse.getTypeValue().equals(JSONConstants.GetResourcePathResult)) {
                            if (commandResponse != null) {
                                final String pathValue = getPathResponse.getValueForKey(JSONConstants.Value);
                                final String nameValue = getPathResponse.getValueForKey(JSONConstants.Name);
                                Platform.runLater(() -> commandResponse.resourcePath(resource, pathValue, nameValue));
                            }
                        }
                    } catch (IOException e) {
                        Platform.runLater(() -> authResponse.connectionClose(e.getMessage()));
                    } catch (Exception e) {
                        Platform.runLater(() -> authResponse.connectionClose("Bad server authResponse!"));
                    }
                }
            }.start();
        }
    }

    private void baseSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }
}
