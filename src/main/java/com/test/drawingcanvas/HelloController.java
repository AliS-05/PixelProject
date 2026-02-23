package com.test.drawingcanvas;

import javafx.fxml.FXML;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Canvas canvas;
    private GraphicsContext gc;

    @FXML
    public void initialize(){
        gc = canvas.getGraphicsContext2D();
    }

    @FXML
    public void selectPencil(){
        System.out.println("Pencil Selected");
    }
}
