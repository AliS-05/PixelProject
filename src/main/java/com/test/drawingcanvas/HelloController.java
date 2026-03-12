package com.test.drawingcanvas;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Deque;

public class HelloController {

    private static final int ROWS = 16;
    private static final int COLS = 16;

    private Server server;
    private Client client;
    // source of truth grid
    private static Color[][] canvasData = new Color[ROWS][COLS];
    // these probably need to be static as well for all users to receive same undo's and redo's

    private Color curColor = Color.BLACK;
    private Mode curMode = Mode.Pencil;

    private final Object stackMutex = new Object();

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
        applyOperation(op, false);
    }

    private void applyOperation(Operation op, boolean fromNetwork) {
        if(!fromNetwork) {
            if (this.client != null) { // if we are the client then send the operation to the server
                this.client.sendOperation(op);
            }
            if (this.server != null) { // otherwise if we are the host AND we have a server, update server state
                this.server.processOperation(op, null);
            }
        }

        if(op.type == null){
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
                //canvasData[r][c] = newData[r][c];
                //pixels[r][c].setFill(newData[r][c]);
            }
        }
    }



    @FXML
    public void saveFile() throws IOException {
        WriteFile wf = new WriteFile();
        // NOTE need some element to get fileName
        wf.writeFile(ROWS, COLS, canvasData, "src/Data/test.pxbmp");
    }

    @FXML
    public void loadFile() throws IOException{
        ReadFile rf = new ReadFile();
        //NOTE once again need some way to get fileName
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
    public void joinServer() throws UnknownHostException, IOException{
        System.out.println("Joining Server...");
        client = new Client("127.0.0.1", 8080); // keep it localhost for now

        Color[][] initial = client.loadServerCanvas(); //getting server canvas state upon joining (ie your friend has already started drawing)
        if(initial != null){
            loadNewCanvas(initial);
        }

        this.client.listenForOperation((op) -> {
            Platform.runLater(() -> {
                applyOperation(op, true);
            });
        });

    }
}