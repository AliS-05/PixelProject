package com.test.drawingcanvas;

import javafx.fxml.FXML;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;


public class HelloController {
    public Mode curMode = Mode.Pencil; //Default to pencil mode
    @FXML
    private Canvas canvas;

    @FXML
    public void initialize() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvas.setOnMouseDragged(e -> {
            if (curMode == Mode.Pencil) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (curMode == Mode.Eraser) {
                gc.clearRect(e.getX() - 5, e.getY() - 5, 10, 10);
            }
        });
    }

    @FXML
    public void selectPencil(){
        curMode = Mode.Pencil;
        System.out.println("Current Mode: Pencil");
    }

    @FXML
    public void selectEraser(){
        curMode = Mode.Eraser;
        System.out.println("Current Mode: Eraser");
    }
}
