package sample.user;

import base.Permission;
import connection.Connection;
import connection.ServerAuthResponseDelegate;
import connection.UserCommandResponseDelegate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sample.ui.UILoader;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class UserController implements UserCommandResponseDelegate, ServerAuthResponseDelegate {

    @FXML private ComboBox<String> resourcesComboBox;
    @FXML private Label selectResourceLabel;
    @FXML private TextArea resourceContentTextView;
    @FXML private Button changeResourceButton;
    @FXML private ProgressIndicator changeResourceIndicator;
    @FXML private Label changeResourceResultLabel;
    @FXML private TableView<Permission> permissionsTableView;
    @FXML private TableColumn resourceColumn;
    @FXML private TableColumn permissionsColumn;

    private Connection connection;

    @FXML private void initialize() {
        selectResourceLabel.setVisible(false);

        resourcesComboBox.setItems(null);
        resourcesComboBox.setVisible(false);

        resourceContentTextView.setText(null);
        resourceContentTextView.setVisible(false);

        changeResourceIndicator.setVisible(false);
        changeResourceButton.setVisible(false);
        changeResourceResultLabel.setVisible(false);
        permissionsTableView.setVisible(false);
    }

    public void reload() {
        selectResourceLabel.setVisible(false);

        resourcesComboBox.setItems(null);
        resourcesComboBox.setVisible(false);

        resourceContentTextView.setText(null);
        resourceContentTextView.setVisible(false);

        changeResourceIndicator.setVisible(false);
        changeResourceButton.setVisible(false);
        permissionsTableView.setVisible(false);

        connection.authResponse = this;
        connection.commandResponse = this;
        connection.getPermissions();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void updateErrorLabelWithText(boolean succesful, String text) {
        if (!succesful) {
            changeResourceResultLabel.setStyle("-fx-text-fill: red;");
        } else {
            changeResourceResultLabel.setStyle("-fx-text-fill: green;");
        }

        changeResourceResultLabel.setText(text);
    }

    public void resourceSelected(ActionEvent event) {
        final String resource = resourcesComboBox.getValue();

        if (resource != null && !resource.isEmpty()) {
            connection.readResource(resource);
        } else {
            updateErrorLabelWithText(false, "Set good resource value!");
        }
    }

    public void changeResourceTapped(ActionEvent event) {
        changeResourceIndicator.setVisible(true);

        final String resourceName = resourcesComboBox.getValue();
        final String resourceValue = resourceContentTextView.getText();

        if (resourceName != null && !resourceName.isEmpty() && resourceValue != null && !resourceValue.isEmpty()) {
            connection.writeResource(resourceName, resourceValue);
        } else {
            updateErrorLabelWithText(false, "Fill all fields!!!");
        }
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
        moveToLoginScreen();
    }

    @Override
    public void connectionClose(String reason) {
        moveToLoginScreen();
    }

    @Override
    public void authSuccesful() {

    }

    @Override
    public void readResource(String value, String error) {
        if (error == null) {
            resourceContentTextView.setVisible(true);
            resourceContentTextView.setText(value);

            changeResourceButton.setVisible(true);
        } else {
            updateErrorLabelWithText(false, error);
        }
    }

    @Override
    public void writeResource(boolean success, String error) {
        changeResourceIndicator.setVisible(false);
        updateErrorLabelWithText(success, success ? "Success!" : error);
    }

    @Override
    public void getPermissions(Map<String, String> perms, String error) {
        if (perms != null) {
            selectResourceLabel.setVisible(true);

            final ObservableList<String> comboBoxData = FXCollections.observableArrayList(perms.keySet());
            resourcesComboBox.setItems(comboBoxData);
            resourcesComboBox.setVisible(true);

            final ObservableList<Permission> tableViewData = FXCollections.observableArrayList(perms.entrySet().stream().map(entry -> new Permission(entry.getKey(), entry.getValue())).collect(Collectors.toList()));

            resourceColumn.setCellValueFactory(
                    new PropertyValueFactory<Permission, String>("resource")
            );
            permissionsColumn.setCellValueFactory(
                    new PropertyValueFactory<Permission, String>("permissions")
            );

            permissionsTableView.setItems(tableViewData);
            permissionsTableView.setVisible(true);
        } else {
            updateErrorLabelWithText(false, error);
        }
    }

    private void moveToLoginScreen() {
        UILoader.getInstance().loadLoginScreen();
    }
}
