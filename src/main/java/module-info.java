module com.test.drawingcanvas {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.java_websocket;

    opens com.test.drawingcanvas to javafx.fxml;
    exports com.test.drawingcanvas;
}