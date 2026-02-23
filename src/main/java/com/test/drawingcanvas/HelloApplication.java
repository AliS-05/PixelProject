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
        //Pane root = new Pane();
       // Scene scene = new Scene(root, 1020, 800, Color.WHITE);

       // Rectangle rect = new Rectangle(75, 75, 100, 100);
       // rect.setFill(Color.BLUE);

       // rect.setOnMousePressed(e -> {
       //     Color randomColor = Color.color(Math.random(), Math.random(), Math.random());
       //     rect.setFill(randomColor);
       // });

       // rect.setOnMouseDragged(e -> {
       //     rect.setX(e.getSceneX());
       //     rect.setY(e.getSceneY());
       // });

       // root.getChildren().add(rect);

        stage.setScene(scene);
        stage.show();
    }
}
