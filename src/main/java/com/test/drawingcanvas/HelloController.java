package com.test.drawingcanvas;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class HelloController {

    private static final int ROWS = 32;
    private static final int COLS = 32;
    private static final int CELL_SIZE = 20;

    private Color curColor = Color.BLACK;
    private Mode curMode = Mode.Pencil;

    @FXML
    private GridPane grid;

    @FXML
    private ColorPicker colorPicker;

    private Rectangle[][] pixels;

    @FXML
    public void initialize() {

        pixels = new Rectangle[ROWS][COLS];

        colorPicker.setValue(curColor);
        colorPicker.setOnAction(e -> curColor = colorPicker.getValue());

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.WHITE);
                cell.setStroke(Color.LIGHTGRAY);

                int r = row;
                int c = col;

                cell.setOnMousePressed(e -> applyTool(r, c));
                cell.setOnMouseDragged(e -> applyTool(r, c));

                pixels[row][col] = cell;
                grid.add(cell, col, row);
            }
        }
    }

    private void applyTool(int row, int col) {

        switch (curMode) {

            case Pencil -> pixels[row][col].setFill(curColor);

            case Eraser -> pixels[row][col].setFill(Color.WHITE);

            case Fill -> {
                // flood fill logic can go here later
            }
        }
    }

    @FXML
    public void selectPencil() {
        curMode = Mode.Pencil;
    }

    @FXML
    public void selectEraser() {
        curMode = Mode.Eraser;
    }

    @FXML
    public void selectFill() {
        curMode = Mode.Fill;
    }

    @FXML
    public void selectClear() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                pixels[row][col].setFill(Color.WHITE);
            }
        }
    }
}