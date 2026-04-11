package com.test.drawingcanvas;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class Server {
    private Socket s = null;
    private ServerSocket ss = null;
    private ObjectInputStream in = null;
    private Color[][] serverCanvasData; //authoritative canvas data
    private final List<ObjectOutputStream> clientOutputs = new ArrayList<>();
    private Consumer<Operation> uiUpdateCallback;
    private final Object mutex = new Object();
    private final Object stackMutex = new Object();
    private final Deque<Operation> undoStack = new ArrayDeque<>();
    private final Deque<Operation> redoStack = new ArrayDeque<>();

    public Server(int port) throws IOException {
        ss = new ServerSocket(port);
        ss.setReuseAddress(true); //needed for localhost testing
    }

    public void setUiUpdateCallback(Consumer<Operation> callback) {
        this.uiUpdateCallback = callback;
    }

    public void start() {
        new Thread(() -> { //this thread is spawned solely to accept new connections. once a connection is accepted another thread is spawned, see below
            while (true) {
                try {
                    Socket socket = ss.accept();

                    //to support multiple clients simultaneously we keep an array list of Object Output Streams
                    // and loop the array list to send the data to each client individually
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); //each client is its own object
                    synchronized (clientOutputs) {
                        clientOutputs.add(out);
                    }
                    //mutex sending serialized canvas data (JavaFX Color object is not serializable) to prevent race conditions
                    // or otherwise unexpected results from concurrency
                    synchronized (mutex){
                        out.writeObject(serializeCanvas()); //getting client up-to-date with current canvas data
                        out.flush();
                    }
                    new Thread(() -> {
                        try {
                            //connection has been accepted, so we spawn a new thread with its OWN while loop
                            //this thread constantly reads in new operations from the client and processes the incoming operation with concurrency in mind
                            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                            while (true) {
                                Operation op = (Operation) in.readObject();
                                processOperation(op, out);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void processOperation(Operation op, ObjectOutputStream sender) {
        if (op.type == Mode.Undo) {
            selectUndo();
        } else if (op.type == Mode.Redo) {
            selectRedo();
        } else { //else a 'draw' operation or erase
            synchronized (stackMutex) { //mutex'ed stack to prevent race conditions for multiple clients writing at the same time
                undoStack.push(op);
                redoStack.clear();
            }
            updateServerOperation(op);
            broadcastToClients(op, sender);
            if (uiUpdateCallback != null) Platform.runLater(() -> uiUpdateCallback.accept(op)); //updates the UI with the new operation
        }
    }

    public void updateServerOperation(Operation op) {
        synchronized (mutex) {
            serverCanvasData[op.row][op.col] = op.getNext();
        }
    }

    public void initServerCanvas(Color[][] clientCanvas, int rows, int cols) {
        this.serverCanvasData = new Color[rows][cols];
        for (int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++){
                serverCanvasData[r][c] = clientCanvas[r][c];
            }
        }
    }

    //as mentioned above we keep track of all current connections in an ArrayList and loop over it writing to each connection / client sending data
    public void broadcastToClients(Operation op, ObjectOutputStream sender) {
        synchronized (clientOutputs) {
            for (ObjectOutputStream out : clientOutputs) {
                if (out != sender) {
                    try {
                        out.writeObject(op);
                        out.flush();
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }
        }
    }

    public void selectUndo() {
        synchronized (stackMutex) {
            if (undoStack.isEmpty()) return;
            Operation prev = undoStack.pop();
            redoStack.push(prev);

            // undo restores the previous value by reversing the operation
            Operation undoOp = new Operation(prev.row, prev.col, prev.getNext(), prev.getPrevious());
            updateServerOperation(undoOp);
            broadcastToClients(undoOp, null);
            if (uiUpdateCallback != null) Platform.runLater(() -> uiUpdateCallback.accept(undoOp));
        }
    }

    public void selectRedo() {
        synchronized (stackMutex) {
            if (redoStack.isEmpty()) return;
            Operation op = redoStack.pop();
            undoStack.push(op);
            updateServerOperation(op);
            broadcastToClients(op, null);
            if (uiUpdateCallback != null) Platform.runLater(() -> uiUpdateCallback.accept(op));
        }
    }

    private int[][] serializeCanvas() {
        int rows = serverCanvasData.length;
        int cols = serverCanvasData[0].length;
        int[][] data = new int[rows][cols]; //recall javafx Color cannot be serialized

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Color col = serverCanvasData[r][c];
                int red = (int)(col.getRed() * 255);
                int g = (int)(col.getGreen() * 255);
                int b = (int)(col.getBlue() * 255);
                data[r][c] = (red << 16) | (g << 8) | b; //bit packing to fit 8 bit integers in 32 bit value
            }
        }
        return data;
    }
}