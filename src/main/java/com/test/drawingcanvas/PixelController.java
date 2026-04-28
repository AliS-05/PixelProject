package com.test.drawingcanvas;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.Optional;

public class PixelController {
    private static int ROWS = 16;
    private static int COLS = 16;
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
    private File currentFile = null;


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

    public void resizeGrid() {
        TextInputDialog dialog = new TextInputDialog(ROWS + "x" + COLS);
        dialog.setTitle("Resize Grid");
        dialog.setHeaderText("Enter new grid size (e.g., 32x32)");
        dialog.setContentText("Size:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String input = result.get().toLowerCase().replace(" ", "");
        if (!input.contains("x")) return;

        String[] parts = input.split("x");
        int newRows = Integer.parseInt(parts[0]);
        int newCols = Integer.parseInt(parts[1]);

        applyGridResize(newRows, newCols);
    }
    private void applyGridResize(int newRows, int newCols) {

        Color[][] newCanvas = new Color[newRows][newCols];

        // fill with white
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                newCanvas[r][c] = Color.WHITE;
            }
        }

        // copy old pixels into new grid
        for (int r = 0; r < Math.min(ROWS, newRows); r++) {
            for (int c = 0; c < Math.min(COLS, newCols); c++) {
                newCanvas[r][c] = canvasData[r][c];
            }
        }

        // update controller state
        ROWS = newRows;
        COLS = newCols;
        canvasData = newCanvas;

        rebuildGridUI();
    }

    private void rebuildGridUI() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        createGrid();  // this is the method we will add next
    }

    private void createGrid() {
        pixels = new Rectangle[ROWS][COLS];
        gridLines = new Rectangle[ROWS][COLS];

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

        // recreate cells
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                StackPane cell = new StackPane();
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Rectangle rect = new Rectangle();
                rect.setFill(canvasData[row][col]);  // restore pixel color
                rect.setStroke(Color.LIGHTGRAY);
                rect.setStrokeType(StrokeType.INSIDE);
                rect.setStrokeWidth(1);

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

    public static int getRows() {
        return ROWS;
    }

    public static int getCols() {
        return COLS;
    }

    public void loadNewCanvas(Color[][] newData) {

        // Clear canvas to transparent
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                applyOperation(new Operation(r, c, canvasData[r][c], new Color(1,1,1,1)), false);
            }
        }

        // Paste pixels exactly where they belong
        for (int r = 0; r < newData.length; r++) {
            for (int c = 0; c < newData[0].length; c++) {

                Color color = newData[r][c];

                if (color.getOpacity() > 0) {   // only draw visible pixels
                    if (r < ROWS && c < COLS) {
                        applyOperation(new Operation(
                            r, c,
                            canvasData[r][c],
                            color
                        ), false);
                    }
                }
            }
        }
    }



    // File Chooser

    private FileChooser createFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pixel Bitmap (*.pxbmp)", "*.pxbmp")
        );
        return chooser;
    }

    // Bounding Box + Cropping

    private Rectangle findBoundingBox(Color[][] grid) {
        int minR = ROWS, maxR = -1;
        int minC = COLS, maxC = -1;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (!grid[r][c].equals(Color.WHITE)) {
                    if (r < minR) minR = r;
                    if (r > maxR) maxR = r;   // ⭐ FIXED
                    if (c < minC) minC = c;
                    if (c > maxC) maxC = c;
                    System.out.println("minR=" + minR + " maxR=" + maxR + " minC=" + minC + " maxC=" + maxC);
                }
            }
        }

        if (maxR == -1) return null; // no pixels

        return new Rectangle(minC, minR, (maxC - minC + 1), (maxR - minR + 1));
    }

    private Color[][] cropGrid(Color[][] grid, javafx.scene.shape.Rectangle box) {
        int w = (int) box.getWidth();
        int h = (int) box.getHeight();
        int startC = (int) box.getX();
        int startR = (int) box.getY();

        Color[][] cropped = new Color[h][w];

        for (int r = 0; r < h; r++)
            for (int c = 0; c < w; c++)
                cropped[r][c] = grid[startR + r][startC + c];

        return cropped;
    }

    // Save / Save As / Load

    @FXML
    public void saveFile() throws IOException {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        saveToFile(currentFile);
    }

    @FXML
    public void saveFileAs() throws IOException {
        FileChooser chooser = createFileChooser("Save Pixel Art");
        File file = chooser.showSaveDialog(grid.getScene().getWindow());

        if (file != null) {
            currentFile = file;
            saveToFile(file);
        }
    }

    private void saveToFile(File file) throws IOException {
        javafx.scene.shape.Rectangle box = findBoundingBox(canvasData);

        Color[][] dataToSave;

        if (box == null) {
            dataToSave = new Color[][]{{Color.WHITE}};
        } else {
            dataToSave = cropGrid(canvasData, box);
        }

        WriteFile wf = new WriteFile();
        wf.writeFile(dataToSave.length, dataToSave[0].length, dataToSave, file.getAbsolutePath());
    }

    @FXML
    public void loadFile() throws IOException {
        FileChooser chooser = createFileChooser("Load Pixel Art");
        File file = chooser.showOpenDialog(grid.getScene().getWindow());

        if (file != null) {
            ReadFile rf = new ReadFile();
            Color[][] pixels = rf.readFile(file.getAbsolutePath());
            loadNewCanvas(pixels);
            currentFile = file;
        }
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


