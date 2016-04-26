package sample.ui;

import connection.Connection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.admin.AdminController;

import java.net.URL;

public class UILoader {
    private static final String LOGIN_SCREEN_FILE_NAME = "loginScreen.fxml";
    private static final String MAIN_SCREEN_FILE_NAME = "mainScreen.fxml";

    private static UILoader ourInstance = new UILoader();
    private Stage primaryStage;

    public static UILoader getInstance() {
        return ourInstance;
    }

    private UILoader() {
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Admin UI");
    }

    public void loadLoginScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(LOGIN_SCREEN_FILE_NAME));
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadMainScreen(Connection connection) {
        try {
            final FXMLLoader loader = new FXMLLoader(getLocation(MAIN_SCREEN_FILE_NAME));
            final Parent root = loader.load();

            primaryStage.setScene(new Scene(root, 600, 400));

            AdminController c = loader.<AdminController>getController();
            c.setConnection(connection);

            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private URL getLocation(String file) {
        return getClass().getResource(file);
    }
}
