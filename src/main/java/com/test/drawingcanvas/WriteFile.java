package com.test.drawingcanvas;

import javafx.scene.paint.Color;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFile {

    private static final byte[] MAGIC = {0x50, 0x58, 0x42, 0x4D}; // PXBM

    public void writeFile(int rows, int cols, Color[][] canvasData, String fileName) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {

            // Magic
            for (byte b : MAGIC) out.writeByte(b);

            // Grid size
            out.writeInt(rows);
            out.writeInt(cols);

            // Count non-transparent pixels
            int pixelCount = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (canvasData[r][c].getOpacity() > 0.0) {
                        pixelCount++;
                    }
                }
            }

            out.writeInt(pixelCount);

            // Write each pixel
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Color color = canvasData[r][c];
                    if (color.getOpacity() > 0.0) {
                        //Javafx Stores the RGB values as floats between 0.0 - 1.0
                        // by casting to an int and multiplying by 255 we can get a close enough RGB value
                        // so each pixel will be stored as 3 bytes
                        // 16 x 16 grid = 768 bytes
                        out.writeInt(r);
                        out.writeInt(c);
                        out.writeByte((int)(color.getRed()   * 255));
                        out.writeByte((int)(color.getGreen() * 255));
                        out.writeByte((int)(color.getBlue()  * 255));
                        out.writeByte((int)(color.getOpacity() * 255)); // alpha
                    }
                }
            }
        }
    }
}

