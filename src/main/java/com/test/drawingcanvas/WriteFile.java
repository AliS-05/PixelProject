package com.test.drawingcanvas;

import javafx.scene.paint.Color;

import java.io.*;

public class WriteFile {
    // Magic Number = PXBMP (50 58 42 4D 50)
    private final byte[] magicNumber= {0x50, 0x58, 0x42, 0x4D, 0x50};

    public void writeFile(int rows, int cols, Color[][] canvasData, String fileName) throws IOException {
        // DataOutputStream allows for writeInt, writeString etc. FileOutputStream is for straight bytes one by one
        try(DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName))){
            //NOTE need to take filename from user, im sure scenebuilder has some kind of popup menu
            //write magic numbers
            for(int i = 0; i < 4; i++){
                output.writeByte(magicNumber[i]);
            }
            System.out.println("Wrote Magic Number Successfully");

            //write canvas data
            // need to write dimensions first ? probably a good idea:w
            // could also add date of creation, last edit time etc.
            output.writeInt(rows);
            output.writeInt(cols);

            for(int r = 0; r < rows; r++){
                for(int c = 0; c < cols; c++){
                    //Javafx Stores the RGB values as floats between 0.0 - 1.0
                    // by casting to an int and multiplying by 255 we can get a close enough RGB value
                    // so each pixel will be stored as 3 bytes
                    // 16 x 16 grid = 768 bytes
                    output.writeByte((int)(canvasData[r][c].getRed() * 255));
                    output.writeByte((int)(canvasData[r][c].getGreen() * 255));
                    output.writeByte((int)(canvasData[r][c].getBlue() * 255));
                }
            }

        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }
}
