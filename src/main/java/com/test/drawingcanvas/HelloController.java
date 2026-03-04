package com.test.drawingcanvas;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.ArrayDeque;
import java.util.Deque;

public class HelloController {

    private static final int ROWS = 16;
    private static final int COLS = 16;

    // source of truth grid
    private Color[][] canvasData = new Color[ROWS][COLS];
    private Deque<Operation> undoStack = new ArrayDeque<>();
    private Deque<Operation> redoStack = new ArrayDeque<>();

    private Color curColor = Color.BLACK;
    private Mode curMode = Mode.Pencil;

    @FXML
    private GridPane grid;

    @FXML
    private ColorPicker colorPicker;

    private Rectangle[][] pixels;

    @FXML
    public void initialize() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                canvasData[r][c] = Color.WHITE;

        pixels = new Rectangle[ROWS][COLS];

        // CRITICAL: allow GridPane to expand
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.setSnapToPixel(true);
        // clear any constraints
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        // make columns expand evenly
        for (int i = 0; i < COLS; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / COLS);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        // make rows expand evenly
        for (int i = 0; i < ROWS; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / ROWS);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }

        grid.setOnMouseDragged(e -> {
            int col = (int)(e.getX() / (grid.getWidth() / COLS));
            int row = (int)(e.getY() / (grid.getHeight() / ROWS));

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS){
                applyTool(row, col);
            }
        });

        colorPicker.setValue(curColor);
        colorPicker.setOnAction(e -> curColor = colorPicker.getValue());

        // create cells
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                StackPane cell = new StackPane();

                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Rectangle rect = new Rectangle();
                rect.setFill(Color.WHITE);
                rect.setStroke(Color.LIGHTGRAY);

                rect.setStrokeType(StrokeType.INSIDE);
                rect.setStrokeWidth(1);
                // rectangle fills the cell
                rect.widthProperty().bind(cell.widthProperty());
                rect.heightProperty().bind(cell.heightProperty());

                cell.getChildren().add(rect);

                int r = row;
                int c = col;

                cell.setOnMousePressed(e -> applyTool(r, c));
                cell.setOnMouseDragged(e -> applyTool(r, c));

                pixels[row][col] = rect;

                grid.add(cell, col, row);
            }
        }
    }

    private void applyTool(int row, int col) {
        Color previous = canvasData[row][col];
        Color next = curMode == Mode.Eraser ? Color.WHITE : curColor;

        if (previous.equals(next)) return;

        Operation op = new Operation(row, col, previous, next);
        undoStack.push(op);
        redoStack.clear();
        applyOperation(op);
    }

    private void applyOperation(Operation op) {
        setPixel(op.row, op.col, op.next);
    }

    private void setPixel(int row, int col, Color color){
        canvasData[row][col] = color;
        pixels[row][col].setFill(color);
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
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                canvasData[r][c] = Color.WHITE;
                pixels[r][c].setFill(Color.WHITE);
            }
    }

    @FXML
    public void selectUndo(){
        if(undoStack.isEmpty()) return;
        Operation op = undoStack.pop();
        redoStack.push(op);
        setPixel(op.row,op.col,op.previous);
    }
    @FXML
    public void selectRedo(){
        if (redoStack.isEmpty()) return;

        Operation op = redoStack.pop();
        undoStack.push(op);

        applyOperation(op);
    }
}