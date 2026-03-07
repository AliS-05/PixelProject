package com.test.drawingcanvas;

import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;

public class ReadFile {
    //magic number = pxbmp
    private static final byte[] magicNumber = {0x50, 0x58, 0x42, 0x4D, 0x50};
    ArrayList<Byte> byteArray = new ArrayList<>();
    public Color[][] readFile(String filename){
        try(DataInputStream inputStream = new DataInputStream(new FileInputStream(filename))){
             if(!verifyMagicNumber(inputStream)){
                 throw new IOException("Wrong file format! Only .pxbmp files are supported currently");
             }
            return populateCanvas(inputStream);
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private boolean verifyMagicNumber(DataInputStream inputStream) throws IOException{
        for(int i = 0; i < 4; i++){
            byteArray.add(inputStream.readByte());
        }
        for(int i = 0; i < 4; i++){
            if(magicNumber[i] != byteArray.get(i))
                return false;
        }
        return true;
    }

    private Color[][] populateCanvas(DataInputStream in) throws IOException{
        int rows = in.readInt();
        int cols = in.readInt();
        Color[][] grid = new Color[rows][cols];
        for(int r = 0; r < rows; r++){
            for(int c = 0; c < cols; c++){
                byte red   = in.readByte();
                byte green = in.readByte();
                byte blue  = in.readByte();

                double rr = Byte.toUnsignedInt(red) / 255.0;
                double g  = Byte.toUnsignedInt(green) / 255.0;
                double b  = Byte.toUnsignedInt(blue) / 255.0;
                grid[r][c] = Color.color(rr, g ,b);
            }
        }

        return grid;
    }

}
