module com.test.drawingcanvas {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.test.drawingcanvas to javafx.fxml;
    exports com.test.drawingcanvas;
}