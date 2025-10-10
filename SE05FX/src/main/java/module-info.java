module org.example.se05fx {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.se05fx to javafx.fxml;
    exports org.example.se05fx;
}