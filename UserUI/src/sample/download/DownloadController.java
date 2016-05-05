package sample.download;

import connection.Connection;
import connection.DownloadFileDelegate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DownloadController implements DownloadFileDelegate {

    @FXML private ImageView okImageView;
    @FXML private Label titleLabel;
    @FXML private ProgressIndicator indicator;
    @FXML private Button openButton;
    @FXML private Button startButton;

    private Connection connection;
    private Stage stage;
    private String resourceKey;
    private String resourceName;
    private String downloadPath;
    private boolean isReadable;
    private boolean isWriteable;

    public void setConnection(Connection connection) {
        this.connection = connection;
        this.connection.downloadResponse = this;
    }

    public void setPermissions(boolean isReadable, boolean isWriteable) {
        this.isReadable = isReadable;
        this.isWriteable = isWriteable;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
        titleLabel.setText(titleLabel.getText() + resourceName);
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void onStart(ActionEvent actionEvent) {
        if (startButton.getText().equals("Start")) {
            if (downloadPath == null || downloadPath.isEmpty()) {
                DirectoryChooser dir = new DirectoryChooser();
                File selectedPath = dir.showDialog(stage);

                if (selectedPath != null) downloadPath = selectedPath.getAbsolutePath();
            }

            startDownload();
        } else {
            stopDownload();
        }
    }

    private void stopDownload() {
        startButton.setText("Start");
        indicator.setVisible(false);

        connection.stopDownload(resourceKey);
    }

    private void startDownload() {
        startButton.setText("Stop");
        indicator.setVisible(true);

        connection.startDownload(resourceKey);
    }

    public void onOpen(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(downloadPath + File.separator + resourceName));
        } catch (IOException e) {

        }
    }

    @Override
    public void receiveFile(String resourceKey, String file) {
        final String fullPath = downloadPath + File.separator + resourceName;
        try {
            Files.createFile(Paths.get(fullPath));
            Files.write(Paths.get(fullPath), file.getBytes());

            final File newFile = new File(fullPath);
            newFile.setReadable(isReadable);
            newFile.setWritable(isWriteable);
        } catch (IOException e) {

        }

        indicator.setVisible(false);
        okImageView.setVisible(true);

        openButton.setDisable(false);
        startButton.setDisable(true);
    }
}
