package com.test.drawingcanvas;

import javafx.scene.paint.Color;

import java.io.BufferedInputStream;
import java.net.ServerSocket;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Server {
    private Socket s = null;
    private ServerSocket ss = null;
    private DataInputStream in = null;
    //usage com.test.drawingcanvas.Launcher.Server s = new com.test.drawingcanvas.Launcher.Server(8080);
    private Color serverCanvasData[][];
    public Server(int port) throws IOException{
        try {
            ss = new ServerSocket(port);
            System.out.println("com.test.drawingcanvas.Launcher.Server started");

            System.out.println("Waiting for client..");

            s = ss.accept();
            System.out.println("com.test.drawingcanvas.Launcher.Client Accepted");

            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));

            String m = "";
            while (!m.equals("Over")) {
                try {
                    m = in.readUTF();
                    System.out.println(m);
                } catch (IOException i) {
                    System.out.println(i);
                }
            }
            System.out.println("Closing Connection");
            s.close();
            in.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateServerOperation(Operation op){
        serverCanvasData[op.row][op.col] = op.next;
    }

    public void initServerCanvas(Color[][] clientCanvas, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                serverCanvasData[r][c] = clientCanvas[r][c];
            }
        }
    }
}

