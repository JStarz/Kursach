package sample.main;

import javafx.application.Application;
import javafx.stage.Stage;
import sample.ui.UILoader;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        final UILoader loader = UILoader.getInstance();

        loader.setStage(primaryStage);
        loader.loadLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
