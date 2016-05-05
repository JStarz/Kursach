package sample.user;

import base.Permission;
import connection.Connection;
import connection.ServerAuthResponseDelegate;
import connection.UserCommandResponseDelegate;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import sample.ui.UILoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController implements UserCommandResponseDelegate, ServerAuthResponseDelegate {

    @FXML private Button downloadButton;
    @FXML private StackPane stackPane;
    @FXML private TreeView<String> treeView;
    @FXML private TableView<Permission> permissionsTableView;
    @FXML private TableColumn resourceColumn;
    @FXML private TableColumn permissionsColumn;

    private Connection connection;
    private final TreeItem<String> root = new TreeItem<>("/");
    private Map<String, String> permissions = new HashMap<>();

    @FXML private void initialize() {
        Platform.runLater(this::reload);
    }

    public void reload() {
        stackPane.setVisible(false);
        permissionsTableView.setVisible(false);

        treeView.setRoot(root);
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) downloadButton.setDisable(newValue.getChildren().size() != 0);
        });

        root.getChildren().clear();

        connection.authResponse = this;
        connection.commandResponse = this;
        connection.getPermissions();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void reloadPermissions(ActionEvent event) {
        reload();
    }

    public void logout(ActionEvent event) {
        try {
            connection.close();
        } catch (IOException e) {

        } finally {
            moveToLoginScreen();
        }
    }

    @Override
    public void authFailed(String description) {
        showAlert("Auth Failed", description, null, Alert.AlertType.ERROR, this::moveToLoginScreen);
    }

    @Override
    public void connectionClose(String reason) {
        showAlert("Connection Close", reason, null, Alert.AlertType.ERROR, this::moveToLoginScreen);
    }

    @Override
    public void authSuccesful() {

    }

    @Override
    public void resourcePath(String resource, String path, String name) {
        permissions.put(name, resource);

        if (path.equals("/")) {
            root.getChildren().add(new TreeItem<>(name));
        } else {
            final String[] folders = path.split("/");

            for (int i = 0; i < folders.length; i++) {
                final String folder = folders[i], prevFolder = i == 0 ? null : folders[i - 1];

                if (folder != null && !folder.isEmpty()) {
                    TreeItem<String> treeFolder = root.getChildren().stream().filter(children -> children.getValue().equals(folder)).findFirst().orElse(null);

                    if (treeFolder == null) {
                        treeFolder = new TreeItem<>(folder, new ImageView( UILoader.getInstance().getFolderImage() ));
                        treeFolder.getChildren().add(new TreeItem<>(name));

                        if (i == 0) root.getChildren().add(0, treeFolder);
                        else {
                            TreeItem<String> findedRoot = root.getChildren().stream().filter(children -> children.getValue().equals(prevFolder)).findFirst().orElse(null);
                            if (findedRoot == null) findedRoot = new TreeItem<>(prevFolder);
                            findedRoot.getChildren().add(0, treeFolder);
                        }
                    } else {
                        treeFolder.getChildren().add(new TreeItem<>(name));
                    }
                }
            }
        }
    }

    @Override
    public void readResource(String value, String error) {
        /*if (error == null) {
            resourceContentTextView.setVisible(true);
            resourceContentTextView.setText(value);

            changeResourceButton.setVisible(true);
        } else {
            updateErrorLabelWithText(false, error);
        }*/
    }

    @Override
    public void writeResource(boolean success, String error) {
        //changeResourceIndicator.setVisible(false);
        //updateErrorLabelWithText(success, success ? "Success!" : error);
    }

    @Override
    public void getPermissions(Map<String, String> perms, String error) {
        if (perms != null) {
            perms.keySet().forEach(e -> connection.getResourcePath(e));

            final ObservableList<Permission> tableViewData = FXCollections.observableArrayList(perms.entrySet().stream().map(entry -> new Permission(entry.getKey(), entry.getValue())).collect(Collectors.toList()));

            resourceColumn.setCellValueFactory(
                    new PropertyValueFactory<Permission, String>("resource")
            );
            permissionsColumn.setCellValueFactory(
                    new PropertyValueFactory<Permission, String>("permissions")
            );

            permissionsTableView.setItems(tableViewData);
            permissionsTableView.setVisible(true);

            stackPane.setVisible(true);
        } else {
            showAlert("Error", "Click OK for next try", error, Alert.AlertType.ERROR, () -> connection.getPermissions());
        }
    }

    private void moveToLoginScreen() {
        UILoader.getInstance().loadLoginScreen();
    }

    public void downloadButton(ActionEvent actionEvent) {
        final String name = treeView.getSelectionModel().getSelectedItem().getValue();
        final String key = permissions.get(name);
        final Permission permission = permissionsTableView.getItems().stream().filter(p -> p.getResource().equals(permissions.get(name))).findFirst().orElse(null);

        UILoader.getInstance().loadDownloadManager(connection, name, key, permission.getPermissions().contains("READ"), permission.getPermissions().contains("WRITE"));
    }

    interface AlertOkButtonAction {
        void act();
    }

    private void showAlert(String title, String description, String headerText, Alert.AlertType type, AlertOkButtonAction action) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(description);

        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> action.act());
    }
}
