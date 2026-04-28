package com.test.drawingcanvas;

import javafx.scene.paint.Color;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadFile {

    private static final byte[] MAGIC = {0x50, 0x58, 0x42, 0x4D};

    public Color[][] readFile(String filename) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {

            if (!verifyMagicNumber(in)) throw new IOException("Bad file format");

            int rows = in.readInt();
            int cols = in.readInt();
            int pixelCount = in.readInt();

            Color[][] data = new Color[rows][cols];

            // Default to transparent
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    data[r][c] = new Color(1, 1, 1, 1);

            // Read pixels
            for (int i = 0; i < pixelCount; i++) {
                int r = in.readInt();
                int c = in.readInt();
                int red   = in.readUnsignedByte();
                int green = in.readUnsignedByte();
                int blue  = in.readUnsignedByte();
                int alpha = in.readUnsignedByte();

                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    data[r][c] = Color.rgb(red, green, blue, alpha / 255.0);
                }
            }

            return data;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean verifyMagicNumber(DataInputStream in) throws IOException {
        for (byte expected : MAGIC) {
            if (in.readByte() != expected) return false;
        }
        return true;
    }
}


