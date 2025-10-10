module org.example.demofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.desktop;

    opens org.example.demofx to javafx.fxml;
    opens org.example.demofx.model to jakarta.persistence,org.hibernate.orm.core,javafx.base;
    exports org.example.demofx;
}