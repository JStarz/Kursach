package sample.login;

import connection.Connection;
import connection.ServerAuthResponseDelegate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import sample.ui.UILoader;

import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginController implements ServerAuthResponseDelegate {

    private Connection adminConnection;

    @FXML private TextField nameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator indicator;

    @FXML private void handleLogin(ActionEvent event) {

        if (adminConnection == null) {
            try {
                adminConnection = new Connection(new Socket("localhost", 9001));
                adminConnection.authResponse = this;
                auth();
            } catch (Exception e) {
                indicator.setVisible(false);
                errorLabel.setText(e.getMessage());
                closeConnection();
            }
        } else {
            auth();
        }
    }

    @FXML private void onRegister(ActionEvent event) {
        // TODO: add register functionality
    }

    private void auth() {
        final String name = nameTextField.getText();
        final String password = passwordTextField.getText();
        if (name != null && !name.isEmpty() && password != null && !password.isEmpty()) {
            errorLabel.setText("");
            indicator.setVisible(true);
            adminConnection.auth(name, md5(password));
        } else {
            errorLabel.setText("Name or password field are incorrect!");
        }
    }

    private String md5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte byteData[] = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte aByteData : byteData) {
                sb.append(Integer.toString((aByteData & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void closeConnection() {
        try {
            adminConnection.close();
        } catch (IOException e1) {
            adminConnection = null;
        } finally {
            adminConnection = null;
        }
    }

    @Override
    public void authFailed(String description) {
        indicator.setVisible(false);

        errorLabel.setText("Auth Failed: " + description);
    }

    @Override
    public void connectionClose(String reason) {
        indicator.setVisible(false);

        errorLabel.setText("Connection close: " + reason);
        closeConnection();
    }

    @Override
    public void authSuccesful() {
        indicator.setVisible(false);

        UILoader.getInstance().loadMainScreen(adminConnection);
    }
}
