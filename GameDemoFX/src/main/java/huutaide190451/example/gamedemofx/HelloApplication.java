package huutaide190451.example.gamedemofx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import huutaide190451.example.gamedemofx.View.GameView;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        GameView gameView = new GameView();

        Scene scene = new Scene(gameView);
        stage.setTitle("RPG Game - JavaFX Demo");
        stage.setScene(scene);
        stage.show();

        gameView.requestFocus();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

