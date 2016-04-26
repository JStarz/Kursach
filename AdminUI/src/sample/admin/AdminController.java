package sample.admin;

import base.User;
import connection.Connection;
import connection.ServerAuthResponseDelegate;
import connection.ServerCommandResponseDelegate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sample.ui.UILoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AdminController implements ServerAuthResponseDelegate, ServerCommandResponseDelegate {

    final Image okImage = new Image(new File("src/sample/ui/ok.png").toURI().toString());
    final Image badImage = new Image(new File("src/sample/ui/delete.png").toURI().toString());

    final EventHandler<ActionEvent> userSelectedForRolesRequesting = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (selectUserComboBox.getValue() != null && !selectUserComboBox.getValue().isEmpty()) {
                passwordLabel.setVisible(true);
                passwordLabel.setText("Role");
                getAllRolesComboBox.setVisible(true);
                getAllRolesComboBox.setItems(null);
                getAllRolesIndicator.setVisible(true);

                connection.getRolesForUser(selectUserComboBox.getValue());
            }
        }
    };
    final EventHandler<ActionEvent> userSelectedForDistinctRolesRequesting = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (selectUserComboBox.getValue() != null && !selectUserComboBox.getValue().isEmpty()) {
                passwordLabel.setVisible(true);
                passwordLabel.setText("Role");
                getAllRolesComboBox.setVisible(true);
                getAllRolesComboBox.setItems(null);
                getAllRolesIndicator.setVisible(true);

                connection.getDistinctRolesForUser(selectUserComboBox.getValue());
            }
        }
    };
    final EventHandler<ActionEvent> userSelectedForGetPermissions = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (selectUserComboBox.getValue() != null && !selectUserComboBox.getValue().isEmpty()) {
                requestUsersIndicator.setVisible(true);
                connection.getUserPermissions(selectUserComboBox.getValue());
            }
        }
    };
    final EventHandler<ActionEvent> roleSelectedForGetResourceAndPerms = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (selectUserComboBox.getValue() != null && !selectUserComboBox.getValue().isEmpty()) {
                getAllRolesComboBox.setVisible(true);
                getAllRolesComboBox.setItems(null);
                getAllRolesIndicator.setVisible(true);

                passwordLabel.setVisible(true);
                passwordLabel.setText("Resource");

                final String selectedItem = listView.getSelectionModel().getSelectedItem();
                if (selectedItem.equals("Manage role permissions"))
                    connection.getRolePermissions(selectUserComboBox.getValue());
                if (selectedItem.equals("Change resurs from role"))
                    connection.getDistinctResourceForRole(selectUserComboBox.getValue());
                if (selectedItem.equals("Associate role with role")) {
                    connection.getAvailableAssociativeRolesForRole(selectUserComboBox.getValue());
                    passwordLabel.setText("Associate role");
                }
            }
        }
    };
    final EventHandler<ActionEvent> roleSelectedForGetAssociativeRoles = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if (selectUserComboBox.getValue() != null && !selectUserComboBox.getValue().isEmpty()) {
                getAllRolesComboBox.setVisible(true);
                getAllRolesComboBox.setItems(null);
                getAllRolesIndicator.setVisible(true);

                passwordLabel.setVisible(true);
                passwordLabel.setText("Associated role");

                connection.getAssociativeRolesForRole(selectUserComboBox.getValue());
            }
        }
    };

    @FXML private CheckBox readCheckBox;
    @FXML private CheckBox writeCheckBox;
    @FXML private TableColumn userColumnTableView;
    @FXML private TableColumn resourceColumnTableView;
    @FXML private TableColumn permissionsColumnTableView;
    @FXML private TableView<User> tableView;
    @FXML private ComboBox<String> getAllRolesComboBox;
    @FXML private ProgressIndicator getAllRolesIndicator;
    @FXML private ComboBox<String> selectUserComboBox;
    @FXML private ProgressIndicator requestUsersIndicator;
    @FXML private TextField thirdTextInputField;
    @FXML private ComboBox<String> secondComboBox;
    @FXML private Label fourthLabel;
    @FXML private Label titleLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label passwordLabel;
    @FXML private Label nameLabel;
    @FXML private TextField passwordTextField;
    @FXML private ProgressIndicator responseIndicator;
    @FXML private ImageView responseImageView;
    @FXML private Label errorLabel;
    @FXML private TextField nameTextField;
    @FXML private ListView<String> listView;
    @FXML private ComboBox<String> comboBox;

    private ObservableList<String> listViewData = FXCollections.observableArrayList("Create User", "Delete User", "Change User Credentials", "Associate Role With User", "Delete user role", "Delete user roles", "Get users permissions", "Get user permissions", "Create Role", "Delete role", "Change role name", "Manage role permissions", "Change resurs from role", "Associate role with role", "Delete associated role", "Delete all associated roles");
    private ObservableList<String> userTypeComboBoxData = FXCollections.observableArrayList("ADMIN", "REGISTER");

    private Connection connection;

    @FXML private void initialize() {
        updateListView();
        createUserScene();
    }

    @FXML private void doAction(ActionEvent event) {
        final int selectedIndex = listView.getSelectionModel().getSelectedIndices().get(0);
        final String name, password, type, oldName, role, resource, perms, newRole, tempPerms, associativeRole;
        switch (selectedIndex) {
            case 0:
                name = nameTextField.getText();
                password = passwordTextField.getText();
                type = comboBox.getValue();
                if (goodValue(name) && goodValue(password) && goodValue(type)) {
                    updateUIForRequest(true);
                    connection.createUser(name, password, type);
                } else {
                    updateErrorLabelWithText(false, "Fill all fields!");
                }
                break;

            case 1:
                name = selectUserComboBox.getValue();
                if (goodValue(name)) {
                    updateUIForRequest(true);
                    connection.deleteUser(name);
                } else {
                    updateErrorLabelWithText(false, "Set valid user name for delete!");
                }
                break;

            case 2:
                oldName = selectUserComboBox.getValue();
                name = passwordTextField.getText();
                password = thirdTextInputField.getText();
                type = secondComboBox.getValue();
                if (goodValue(oldName) && goodValue(name) && goodValue(password) && goodValue(type)) {
                    updateUIForRequest(true);
                    connection.changeUserCredentials(oldName, name, password, type);
                } else {
                    updateErrorLabelWithText(false, "Fill all fields!");
                }
                break;

            case 3:
                name = selectUserComboBox.getValue();
                role = getAllRolesComboBox.getValue();
                if (goodValue(name) && goodValue(role)) {
                    updateUIForRequest(true);
                    connection.associateRoleWithUser(name, role);
                } else {
                    updateErrorLabelWithText(false, "Select all fields!");
                }
                break;

            case 4:
                name = selectUserComboBox.getValue();
                role = getAllRolesComboBox.getValue();
                if (goodValue(name) && goodValue(role)) {
                    updateUIForRequest(true);
                    connection.deleteUserRole(name, role);
                } else {
                    updateErrorLabelWithText(false, "Select all fields!");
                }
                break;

            case 5:
                name = selectUserComboBox.getValue();
                if (goodValue(name)) {
                    updateUIForRequest(true);
                    connection.deleteUserRoles(name);
                } else {
                    updateErrorLabelWithText(false, "Select all fields!");
                }
                break;

            case 6: break;
            case 7: break;

            case 8:
                name = nameTextField.getText();
                resource = getAllRolesComboBox.getValue();
                tempPerms = readCheckBox.isSelected() ? "READ" : "";
                perms = writeCheckBox.isSelected() ? (tempPerms.length() > 0 ? tempPerms + "|WRITE" : "WRITE") : tempPerms;
                if (goodValue(name) && goodValue(resource) && goodValue(perms)) {
                    updateUIForRequest(true);
                    connection.createRole(name, resource, perms);
                } else {
                    updateErrorLabelWithText(false, "Fill all fields!");
                }
                break;

            case 9:
                role = selectUserComboBox.getValue();
                if (goodValue(role)) {
                    updateUIForRequest(true);
                    connection.deleteRole(role);
                } else {
                    updateErrorLabelWithText(false, "Select role!");
                }
                break;

            case 10:
                role = selectUserComboBox.getValue();
                newRole = passwordTextField.getText();
                if (goodValue(role) && goodValue(newRole)) {
                    updateUIForRequest(true);
                    connection.changeRoleName(role, newRole);
                } else {
                    updateErrorLabelWithText(false, "Fill all fields!");
                }
                break;

            case 11:
                role = selectUserComboBox.getValue();
                resource = getAllRolesComboBox.getValue();
                tempPerms = readCheckBox.isSelected() ? "READ" : "";
                perms = writeCheckBox.isSelected() ? (tempPerms.length() > 0 ? tempPerms + "|WRITE" : "WRITE") : tempPerms;
                if (goodValue(role) && goodValue(resource) && goodValue(perms)) {
                    updateUIForRequest(true);
                    connection.managePermissions(role, resource, perms);
                } else {
                    updateErrorLabelWithText(false, "Select all fields!");
                }
                break;

            case 12:
                role = selectUserComboBox.getValue();
                resource = getAllRolesComboBox.getValue();
                if (goodValue(role) && goodValue(resource)) {
                    updateUIForRequest(true);
                    connection.changeResursFromRole(role, resource);
                } else {
                    updateErrorLabelWithText(false, "Select all fields!");
                }
                break;

            case 13:
                role = selectUserComboBox.getValue();
                associativeRole = getAllRolesComboBox.getValue();
                if (goodValue(role) && goodValue(associativeRole)) {
                    updateUIForRequest(true);
                    connection.associateRoleWithRole(role, associativeRole);
                } else {
                    updateErrorLabelWithText(false, "Select all fields!");
                }
                break;

            case 14:
                role = selectUserComboBox.getValue();
                associativeRole = getAllRolesComboBox.getValue();
                if (goodValue(role) && goodValue(associativeRole)) {
                    updateUIForRequest(true);
                    connection.deleteAssociatedRole(role, associativeRole);
                } else {
                    updateErrorLabelWithText(false, "Select all roles!");
                }
                break;

            case 15:
                role = selectUserComboBox.getValue();
                if (goodValue(role)) {
                    updateUIForRequest(true);
                    connection.deleteAllAssociatedRoles(role);
                } else {
                    updateErrorLabelWithText(false, "Select all roles!");
                }
                break;

            default: break;
        }
    }

    public void hideAllViews() {
        getAllRolesComboBox.setVisible(false);
        getAllRolesIndicator.setVisible(false);
        selectUserComboBox.setVisible(false);
        requestUsersIndicator.setVisible(false);
        thirdTextInputField.setVisible(false);
        secondComboBox.setVisible(false);
        fourthLabel.setVisible(false);
        userTypeLabel.setVisible(false);
        passwordLabel.setVisible(false);
        nameLabel.setVisible(false);
        passwordTextField.setVisible(false);
        responseIndicator.setVisible(false);
        nameTextField.setVisible(false);
        comboBox.setVisible(false);
        tableView.setVisible(false);
        readCheckBox.setVisible(false);
        readCheckBox.setSelected(false);
        writeCheckBox.setVisible(false);
        writeCheckBox.setSelected(false);

        selectUserComboBox.setOnAction(null);
        getAllRolesComboBox.setOnAction(null);
    }

    public void clearTextFields() {
        passwordTextField.setText("");
        nameTextField.setText("");
        thirdTextInputField.setText("");
    }

    public void updateErrorLabelWithText(boolean succesful, String text) {
        if (!succesful) {
            errorLabel.setStyle("-fx-text-fill: red;");
        } else {
            errorLabel.setStyle("-fx-text-fill: green;");
        }

        errorLabel.setText(text);
    }

    public void updateImageViewWithResult(boolean succesful) {
        responseImageView.setImage(!succesful ? badImage : okImage);
    }

    public void updateUIForRequest(boolean requestStarted) {
        responseImageView.setVisible(!requestStarted);
        responseIndicator.setVisible(requestStarted);
    }

    public void updateUIToDefaultState() {
        responseIndicator.setVisible(false);

        clearTextFields();
        updateImageViewWithResult(true);
        updateErrorLabelWithText(false, "");
    }

    public void updateListView() {
        listView.setItems(listViewData);
        listView.setCellFactory((list) -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText((item == null || empty) ? null : item);
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            titleLabel.setText(observable.getValue());

            final int selectedIndex = listView.getSelectionModel().getSelectedIndices().get(0);
            System.out.println(listView.getSelectionModel().getSelectedItem());
            switch (selectedIndex) {
                case 0: createUserScene(); break;
                case 1: deleteUserScene(); break;
                case 2: changeUserCredentialsScene(); break;
                case 3: associateRoleWithUserScene(); break;
                case 4: deleteUserRoleScene(); break;
                case 5: deleteUserRolesScene(); break;
                case 6: getUsersPermissionsScene(); break;
                case 7: getUserPermissionsScene(); break;
                case 8: createRoleScene(); break;
                case 9: deleteRoleScene(); break;
                case 10: changeRoleNameScene(); break;
                case 11: manageRolePermissions(); break;
                case 12: changeResursFromRoleScene(); break;
                case 13: associateRoleWithRoleScene(); break;
                case 14: deleteAssociatedRoleScene(); break;
                case 15: deleteAllAssociatedRolesScene(); break;

                default: break;
            }
        }));
    }

    private void deleteAllAssociatedRolesScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Role");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void deleteAssociatedRoleScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Role");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        selectUserComboBox.setOnAction(roleSelectedForGetAssociativeRoles);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void associateRoleWithRoleScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Role");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        selectUserComboBox.setOnAction(roleSelectedForGetResourceAndPerms);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void changeResursFromRoleScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Role");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        selectUserComboBox.setOnAction(roleSelectedForGetResourceAndPerms);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void manageRolePermissions() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Role");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        selectUserComboBox.setOnAction(roleSelectedForGetResourceAndPerms);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void changeRoleNameScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");
        passwordLabel.setVisible(true);
        passwordLabel.setText("New role name");
        passwordTextField.setVisible(true);
        passwordTextField.setText("");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void deleteRoleScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");

        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        requestUsersIndicator.setVisible(true);

        connection.getAllRoles();
    }

    private void createRoleScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");
        nameTextField.setVisible(true);
        nameTextField.setText("");
        passwordLabel.setVisible(true);
        passwordLabel.setText("Resource");
        userTypeLabel.setVisible(true);
        userTypeLabel.setText("Permissions");

        readCheckBox.setVisible(true);
        writeCheckBox.setVisible(true);

        getAllRolesComboBox.setVisible(true);
        getAllRolesComboBox.setItems(null);

        getAllRolesIndicator.setVisible(true);

        connection.getResources();
    }

    private void getUserPermissionsScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");
        selectUserComboBox.setVisible(true);
        selectUserComboBox.setOnAction(userSelectedForGetPermissions);
        selectUserComboBox.setItems(null);

        requestUsersIndicator.setVisible(true);

        connection.getUsers();
    }

    private void getUsersPermissionsScene() {
        updateUIToDefaultState();
        hideAllViews();

        requestUsersIndicator.setVisible(true);

        connection.getUsersPermissions();
    }

    private void deleteUserRolesScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");
        selectUserComboBox.setVisible(true);
        selectUserComboBox.setItems(null);
        requestUsersIndicator.setVisible(true);

        connection.getUsers();
    }

    private void deleteUserRoleScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");
        selectUserComboBox.setVisible(true);
        selectUserComboBox.setOnAction(userSelectedForRolesRequesting);
        selectUserComboBox.setItems(null);

        requestUsersIndicator.setVisible(true);

        connection.getUsers();
    }

    private void associateRoleWithUserScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        //passwordLabel.setVisible(true);
        selectUserComboBox.setVisible(true);
        //getAllRolesComboBox.setVisible(true);

        nameLabel.setText("Name");
        passwordLabel.setText("Role");
        selectUserComboBox.setItems(null);
        getAllRolesComboBox.setItems(null);

        selectUserComboBox.setOnAction(userSelectedForDistinctRolesRequesting);

        requestUsersIndicator.setVisible(true);
        //getAllRolesIndicator.setVisible(true);

        connection.getUsers();
        //connection.getAllRoles();
    }

    private void changeUserCredentialsScene() {
        updateUIToDefaultState();
        hideAllViews();

        selectUserComboBox.setVisible(true);
        passwordLabel.setVisible(true);
        passwordTextField.setVisible(true);
        userTypeLabel.setVisible(true);
        thirdTextInputField.setVisible(true);
        secondComboBox.setVisible(true);
        fourthLabel.setVisible(true);
        nameLabel.setVisible(true);

        selectUserComboBox.setItems(null);
        secondComboBox.setItems(null);
        secondComboBox.setItems(userTypeComboBoxData);

        nameLabel.setText("Old Name");
        passwordLabel.setText("New Name");
        userTypeLabel.setText("New Password");
        fourthLabel.setText("New User Type");

        connection.getUsers();

        requestUsersIndicator.setVisible(true);
    }

    public void deleteUserScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameLabel.setText("Name");

        selectUserComboBox.setItems(null);
        selectUserComboBox.setVisible(true);

        connection.getUsers();

        requestUsersIndicator.setVisible(true);
    }

    public void createUserScene() {
        updateUIToDefaultState();
        hideAllViews();

        nameLabel.setVisible(true);
        nameTextField.setVisible(true);
        passwordLabel.setVisible(true);
        passwordTextField.setVisible(true);
        userTypeLabel.setVisible(true);
        comboBox.setVisible(true);

        nameLabel.setText("Name");
        passwordLabel.setText("Password");
        userTypeLabel.setText("User Type");
        comboBox.setItems(null);
        comboBox.setItems(userTypeComboBoxData);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.connection.authResponse = this;
        this.connection.commandResponse = this;
    }

    private boolean goodValue(String value) {
        return value != null && !value.isEmpty();
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

    private void moveToLoginScreen() {
        UILoader.getInstance().loadLoginScreen();
    }

    @FXML public void onLogout(ActionEvent actionEvent) {
        try {
            connection.close();
        } catch (IOException e) {

        } finally {
            moveToLoginScreen();
        }
    }

    @Override
    public void userCreated(boolean successful, String description) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, description);
        updateImageViewWithResult(successful);
    }

    @Override
    public void userDeleted(boolean successful, String description) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, description);
        updateImageViewWithResult(successful);
    }

    @Override
    public void credentialsChanged(boolean successful, String description) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, description);
        updateImageViewWithResult(successful);
    }

    @Override
    public void roleAssociated(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void userRoleDeleted(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void userRolesDeleted(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void roleCreated(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void roleDeleted(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void roleNameChanged(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void permissionsManaged(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void resourceChanged(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void associatedRoleWithRole(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void deleteAssociatedRole(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void deleteAllAssociatedRole(boolean successful, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(successful, errorDescription);
        updateImageViewWithResult(successful);
    }

    @Override
    public void availableAssociativeRoles(String[] roles, String errorDescription) {
        final ObservableList<String> rolesList = FXCollections.observableArrayList(roles);
        getAllRolesComboBox.setItems(rolesList);
        getAllRolesIndicator.setVisible(false);
    }

    @Override
    public void getUserPermissions(List<User> user, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(user != null, user != null ? "Success" : errorDescription);
        updateImageViewWithResult(user != null);

        requestUsersIndicator.setVisible(false);
        tableView.setVisible(true);

        if (user != null) {
            updateTableViewWithArray(user);
        }
    }

    @Override
    public void getUsersPermissions(List<User> users, String errorDescription) {
        updateUIForRequest(false);
        updateErrorLabelWithText(users != null, users != null ? "Success" : errorDescription);
        updateImageViewWithResult(users != null);

        requestUsersIndicator.setVisible(false);
        tableView.setVisible(true);

        if (users != null) {
            updateTableViewWithArray(users);
        }
    }

    @Override
    public void getUsers(String[] users, String errorDescription) {
        final ObservableList<String> usersList = FXCollections.observableArrayList(users);
        selectUserComboBox.setItems(usersList);
        requestUsersIndicator.setVisible(false);
    }

    @Override
    public void getRoles(String[] roles, String errorDescription) {
        final ObservableList<String> rolesList = FXCollections.observableArrayList(roles);
        final String selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem.equals("Delete role") ||
                selectedItem.equals("Change role name") ||
                selectedItem.equals("Manage role permissions") ||
                selectedItem.equals("Change resurs from role") ||
                selectedItem.equals("Associate role with role") ||
                selectedItem.equals("Delete associated role") ||
                selectedItem.equals("Delete all associated roles")) {
            selectUserComboBox.setItems(rolesList);
            requestUsersIndicator.setVisible(false);
        } else {
            getAllRolesComboBox.setItems(rolesList);
            getAllRolesIndicator.setVisible(false);
        }
    }

    @Override
    public void getRolesForUser(String[] roles, String errorDescription) {
        final ObservableList<String> rolesList = FXCollections.observableArrayList(roles);
        getAllRolesComboBox.setItems(rolesList);
        getAllRolesIndicator.setVisible(false);
    }

    @Override
    public void getResources(String[] resources, String errorDescription) {
        final ObservableList<String> resourcesList = FXCollections.observableArrayList(resources);
        getAllRolesComboBox.setItems(resourcesList);
        getAllRolesIndicator.setVisible(false);
    }

    @Override
    public void getDistinctResources(String[] roles, String errorDescription) {
        final ObservableList<String> resourcesList = FXCollections.observableArrayList(roles);
        getAllRolesComboBox.setItems(resourcesList);
        getAllRolesIndicator.setVisible(false);
    }

    @Override
    public void getRolePermissions(String resource, String perms, String errorDescription) {
        final ObservableList<String> resourcesList = FXCollections.observableArrayList(resource);
        getAllRolesComboBox.setItems(resourcesList);
        getAllRolesIndicator.setVisible(false);
        getAllRolesComboBox.setOnAction(event -> {
            writeCheckBox.setVisible(true);
            readCheckBox.setVisible(true);
            userTypeLabel.setVisible(true);
            userTypeLabel.setText("Permissions");

            if (perms.contains("READ")) readCheckBox.setSelected(true);
            if (perms.contains("WRITE")) writeCheckBox.setSelected(true);
        });
    }

    @Override
    public void distinctedResources(String[] distinctedResources, String errorDescription) {
        final ObservableList<String> resourcesList = FXCollections.observableArrayList(distinctedResources);
        getAllRolesComboBox.setItems(resourcesList);
        getAllRolesIndicator.setVisible(false);
    }

    @Override
    public void associativeRoles(String[] roles, String errorDescription) {
        final ObservableList<String> rolesList = FXCollections.observableArrayList(roles);
        getAllRolesComboBox.setItems(rolesList);
        getAllRolesIndicator.setVisible(false);
    }

    private void updateTableViewWithArray(List<User> users) {
        final ObservableList<User> tableViewData = FXCollections.observableArrayList(users);
        userColumnTableView.setCellValueFactory(
                new PropertyValueFactory<User, String>("name")
        );
        resourceColumnTableView.setCellValueFactory(
                new PropertyValueFactory<User, String>("resource")
        );
        permissionsColumnTableView.setCellValueFactory(
                new PropertyValueFactory<User, String>("permissions")
        );
        tableView.setItems(tableViewData);
    }
}
