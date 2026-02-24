package com.test.drawingcanvas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class HelloApplication extends Application {

    private double offsetX;
    private double offsetY;

    @Override
    public void start(Stage stage) throws Exception{

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/test/drawingcanvas/sceneTest.fxml")
        );
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }
}
