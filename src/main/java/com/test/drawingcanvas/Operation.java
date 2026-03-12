package com.test.drawingcanvas;

import javafx.scene.paint.Color;
import java.io.Serializable;

public class Operation implements Serializable {
    public Mode type;
    public int row, col;
    public double prevR, prevG, prevB, nextR, nextG, nextB;

    public Operation(int row, int col, Color previous, Color next) {
        this.type = null;
        this.row = row;
        this.col = col;
        this.prevR = previous.getRed();
        this.prevG = previous.getGreen();
        this.prevB = previous.getBlue();
        this.nextR = next.getRed();
        this.nextG = next.getGreen();
        this.nextB = next.getBlue();
    }

    public Operation(Mode type){
        this.type = type;
    }


    public Color getPrevious() { return Color.color(prevR, prevG, prevB); }
    public Color getNext() { return Color.color(nextR, nextG, nextB); }
}