package com.test.drawingcanvas;

import javafx.fxml.FXML;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.canvas.Canvas;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;


public class HelloController {
    private Color curColor = Color.BLACK; // default to black pencil on white canvas
    private Mode curMode = Mode.Pencil; //Default to pencil mode
    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Canvas canvas;
    GraphicsContext gc;

    @FXML
    public void initialize() {
        gc = canvas.getGraphicsContext2D();
        gc.setStroke(curColor);

        colorPicker.setOnAction(e -> {
            curColor = colorPicker.getValue();
            gc.setStroke(curColor);
            System.out.println("Current Color: " + curColor);
        });

        canvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
        });

        canvas.setOnMouseDragged(e -> {
            switch (curMode) {
                case Pencil -> {
                    gc.lineTo(e.getX(), e.getY());
                    gc.stroke();
                }
                case Eraser -> {
                    gc.clearRect(e.getX() - 5, e.getY() - 5, 20, 20);
                }
                case Fill -> {
                    // fill logic later
                }
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

    @FXML
    public void selectFill(){
        curMode = Mode.Fill;
        System.out.println("Current Mode: Fill");
    }

    @FXML
    public void selectClear(){
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0, canvas.getWidth(), canvas.getHeight());
        System.out.println("Current Mode: Clear");
    }
}