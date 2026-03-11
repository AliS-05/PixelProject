package com.test.drawingcanvas;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private Socket s = null;
    private ObjectOutputStream out;
    // com.test.drawingcanvas.Launcher.Client c = new com.test.drawingcanvas.Launcher.Client("127.0.0.1", 8080);
    public Client(String addr, int port) {
        try {
            s = new Socket(addr, port);
            this.out = new ObjectOutputStream(s.getOutputStream());
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
}
