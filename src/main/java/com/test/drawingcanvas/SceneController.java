package com.test.drawingcanvas;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.util.Optional;

public class SceneController {

    private void switchScene(ActionEvent event, String file) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(file));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToDrawing(ActionEvent e) { switchScene(e, "drawing.fxml"); }
    public void goToSettings(ActionEvent e) { switchScene(e, "settings.fxml"); }
    public void goToJoin(ActionEvent e) { switchScene(e, "join.fxml"); }
    public void goToTitle(ActionEvent e) { switchScene(e, "title.fxml"); }

    public void exitApp(ActionEvent e) {
        Platform.exit();
    }

    public void joinSession(ActionEvent e) {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("Join Server");
        dialog.setHeaderText("Enter Server IP");
        dialog.setContentText("IP:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String ip = result.get().trim();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("drawing.fxml"));
            Parent root = loader.load();

            PixelController controller = loader.getController();
            controller.connectToServer(ip);

            Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void hostSession(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("drawing.fxml"));
            Parent root = loader.load();

            PixelController controller = loader.getController();
            controller.startHosting(); // no IP needed or you pass it if you want

            Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}