module huutaide190451.example.gamedemofx {
    requires javafx.controls;
    requires javafx.fxml;

    opens huutaide190451.example.gamedemofx to javafx.fxml;
    opens huutaide190451.example.gamedemofx.View to javafx.fxml;

    exports huutaide190451.example.gamedemofx;
    exports huutaide190451.example.gamedemofx.View;
}
