package sample.main;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sample.ui.UILoader;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final UILoader loader = UILoader.getInstance();
        primaryStage.getIcons().add(new Image(new File("src/sample/ui/appIcon.png").toURI().toString()));

        loader.setStage(primaryStage);
        loader.loadLoginScreen();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
