package com.test.drawingcanvas;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private Socket s = null;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    //client object establishes connection to server addr and port
    public Client(String addr, int port) {
        try {
            System.out.println("Attempting to open socket");
            s = new Socket(addr, port);
            System.out.println("Opening streams...");
            this.out = new ObjectOutputStream(s.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(s.getInputStream());

            System.out.println("Connected to " + addr + " at port: " + port);
        } catch (UnknownHostException u) {
            System.out.println("Unknown Host Exception, Couldn't resolve hostname?" + u);
            return;
        } catch (IOException i) {
            System.out.println("Connection Failed: " + i);
            return;
        }
    }

    public void closeConnection(){
        try {
            out.close();
            s.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    public void sendOperation(Operation op){
        try{
            out.writeObject(op);
            out.flush();
        } catch(IOException i){
            System.out.println(i);
        }
    }

    //spawns a background thread to continuously listen for new operations being sent out by the server (see Server.broadcastToClients)
    //a Consumer interface is used because the controller needs to pass a Platform.RunLater to update the UI.
    //listenForOperations UI behavior is defined in PixelController:227 with a lambda
    public void listenForOperation(java.util.function.Consumer<Operation> receivedOperation){
        new Thread(() -> {
            try{
                while(true){
                    Operation op = (Operation) in.readObject();
                    receivedOperation.accept(op);
               }
           } catch(Exception e){
               System.out.println("ERROR IN CLIENT.JAVA");
           }
        }).start();
    }

    //bit masking stuff because javafx Color object is not serializable we can just store info as ints
    public Color[][] loadServerCanvas(){
        try{
            int[][] data = (int[][]) in.readObject();

            int rows = data.length;
            int cols = data[0].length;

            Color[][] canvas = new Color[rows][cols];

            for(int r = 0; r < rows; r++){
                for(int c = 0; c < cols; c++){
                    int argb = data[r][c];

                    int red = (argb >> 16) & 0xff;
                    int g = (argb >> 8) & 0xff;
                    int b = argb & 0xff;

                    canvas[r][c] = Color.rgb(red, g, b);
                }
            }

            return canvas;

        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}
