package com.test.drawingcanvas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PixelApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/test/drawingcanvas/title.fxml")
        );

        Scene scene = new Scene(loader.load(), 800, 600);

        scene.getStylesheets().add(
                getClass().getResource("/com/test/drawingcanvas/theme.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(300);
        stage.show();
    }
}