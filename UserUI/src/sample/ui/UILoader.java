package sample.ui;

import connection.Connection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.download.DownloadController;
import sample.user.UserController;

import java.net.URL;

public class UILoader {
    private static final String LOGIN_SCREEN_FILE_NAME = "loginScreen.fxml";
    private static final String MAIN_SCREEN_FILE_NAME = "mainScreen.fxml";
    private static final String DOWNLOAD_MANAGER_FILE_NAME = "downloadScreen.fxml";

    private static UILoader ourInstance = new UILoader();
    private Stage primaryStage;

    public static UILoader getInstance() {
        return ourInstance;
    }

    private UILoader() {
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("User UI");
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
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_SCREEN_FILE_NAME));
            final Parent root = loader.load();

            primaryStage.setScene(new Scene(root, 600, 400));

            UserController c = loader.<UserController>getController();
            c.setConnection(connection);

            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadDownloadManager(Connection connection, String resourceName, String resourceKey, boolean isReadable, boolean isWriteable) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(DOWNLOAD_MANAGER_FILE_NAME));
            final Parent root = loader.load();
            final Stage downloadStage = new Stage();

            downloadStage.setScene(new Scene(root, 488, 129));
            downloadStage.setTitle("Download Manager");

            DownloadController c = loader.<DownloadController>getController();
            c.setConnection(connection);
            c.setResourceKey(resourceKey);
            c.setResourceName(resourceName);
            c.setStage(downloadStage);
            c.setPermissions(isReadable, isWriteable);

            downloadStage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Image getFolderImage() {
        return new Image(getClass().getResourceAsStream("Folder.png"), 18, 18, false, false);
    }
}

