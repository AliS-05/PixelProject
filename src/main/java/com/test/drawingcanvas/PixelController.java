package com.test.drawingcanvas;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import java.io.IOException;
import java.net.BindException;

public class PixelController {
    private static final int ROWS = 16;
    private static final int COLS = 16;
    private Server server;
    private Client client;
    // source of truth grid
    private static Color[][] canvasData = new Color[ROWS][COLS];
    private Color curColor = Color.BLACK;
    private Mode curMode = Mode.Pencil;
    private final Object stackMutex = new Object();
    @FXML
    private GridPane grid;
    @FXML
    private ColorPicker colorPicker;
    private Rectangle[][] pixels;
    private boolean showGrid = true;
    private Rectangle[][] gridLines;


    @FXML
    public void initialize() { //fills in blank canvas and intializes JavaFX UI
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                canvasData[r][c] = Color.WHITE;

        pixels = new Rectangle[ROWS][COLS];
        gridLines = new Rectangle[ROWS][COLS];
        //making rectangles proportional to window size
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.setSnapToPixel(true);
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
            int col = (int) (e.getX() / (grid.getWidth() / COLS));
            int row = (int) (e.getY() / (grid.getHeight() / ROWS));

            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
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
                gridLines[row][col] = rect;
                cell.setOnMousePressed(e -> applyTool(r, c));
                cell.setOnMouseDragged(e -> applyTool(r, c));

                pixels[row][col] = rect;

                grid.add(cell, col, row);
            }
        }
    }
    //this is how the canvas gets updated
    private void applyTool(int row, int col) {
        Color previous = canvasData[row][col];
        Color next = curMode == Mode.Eraser ? Color.WHITE : curColor;

        if (previous.equals(next)) return;

        Operation op = new Operation(row, col, previous, next);
        applyOperation(op, false);
    }

    private void applyOperation(Operation op, boolean fromNetwork) {
        if(!fromNetwork) {
            if (this.client != null) { // client forwards operations to be processed by server
                this.client.sendOperation(op);
            }
            if (this.server != null) { //server processors all operations, even its own
                this.server.processOperation(op, null);
            }
        }

        if(op.type == null){ //optimistically apply every operation no matter what to the UI
            Platform.runLater(() -> setPixel(op.row, op.col, op.getNext()));
        }
    }

    private void setPixel(int row, int col, Color color) {
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
                Operation op = new Operation(r, c, canvasData[r][c], Color.WHITE);
                applyOperation(op, false);
            }
    }

    //stacks handled with server mutex
    @FXML
    public void selectUndo() {
        if(client != null) {
            client.sendOperation(new Operation(Mode.Undo));
        } else if(server != null){
            server.processOperation(new Operation(Mode.Undo), null);
        }
    }

    @FXML
    public void selectRedo() {
        if(client != null) {
            client.sendOperation(new Operation(Mode.Redo));
        } else if(server != null){
            server.processOperation(new Operation(Mode.Redo), null);
        }
    }

    public static int getRows() {
        return ROWS;
    }

    public static int getCols() {
        return COLS;
    }

    public void loadNewCanvas(Color[][] newData) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Operation op = new Operation(r, c, canvasData[r][c], newData[r][c]);
                applyOperation(op, false);
            }
        }
    }

    @FXML
    public void saveFile() throws IOException {
        WriteFile wf = new WriteFile();
        wf.writeFile(ROWS, COLS, canvasData, "src/Data/test.pxbmp");
    }

    @FXML
    public void loadFile() throws IOException{
        ReadFile rf = new ReadFile();
        Color[][] pixels = rf.readFile("src/Data/test.pxbmp");
        loadNewCanvas(pixels);
    }

    @FXML
    public void hostServer() throws BindException, IOException {
        System.out.println("Hosting Server...");
        this.server = new Server(8080);
        this.server.setUiUpdateCallback(op -> {
            Platform.runLater(() -> setPixel(op.row, op.col, op.getNext()));
        });
        this.server.start();
        this.server.initServerCanvas(canvasData, ROWS, COLS);
    }

    @FXML
    public void joinServer() {
        new Thread(() -> {
            try {
                System.out.println("Joining Server...");
                client = new Client("127.0.0.1", 8080);

                // sync states with current server canvas
                Color[][] initial = client.loadServerCanvas();
                if(initial != null){
                    Platform.runLater(() -> loadNewCanvas(initial));
                }
                //listens for incoming operations on background thread
                this.client.listenForOperation((op) -> {
                    Platform.runLater(() -> applyOperation(op, true));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    public void toggleGridVisibility() {
        showGrid = !showGrid;
        Color gridColor = showGrid ? Color.LIGHTGRAY : Color.TRANSPARENT;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                gridLines[r][c].setStroke(gridColor);
            }
        }
    }
}


