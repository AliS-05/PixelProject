package com.test.drawingcanvas;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.application.Platform;

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

    // Navigation
    public void goToDrawing(ActionEvent e) { switchScene(e, "drawing.fxml"); }
    public void goToSettings(ActionEvent e) { switchScene(e, "settings.fxml"); }
    public void goToJoin(ActionEvent e) { switchScene(e, "join.fxml"); }
    public void goToTitle(ActionEvent e) { switchScene(e, "title.fxml"); }

    // Exit
    public void exitApp(ActionEvent e) {
        Platform.exit();
    }

    // Settings toggles (placeholder)
    public void toggleDarkMode(ActionEvent e) {
        System.out.println("Dark mode toggled");
    }

    public void toggleGrid(ActionEvent e) {
        System.out.println("Grid toggled");
    }

    // Join (placeholder)
    public void joinSession(ActionEvent e) {
        System.out.println("Join clicked");
    }
}
