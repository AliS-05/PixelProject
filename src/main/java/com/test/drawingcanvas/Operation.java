package com.test.drawingcanvas;

import javafx.scene.paint.Color;

public class Operation implements java.io.Serializable{
    public final int row, col;
    public final Color previous, next;

    public Operation(int row, int col, Color previous, Color next){
        this.row = row;
        this.col = col;
        this.previous = previous;
        this.next = next;
    }
}
