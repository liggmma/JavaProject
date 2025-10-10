package huutaide190451.example.gamedemofx.View;

import huutaide190451.example.gamedemofx.Model.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Set;

public class GameView extends Pane {
    private static final int TILE_SIZE = 32;
    private static final int MAP_WIDTH = 20;
    private static final int MAP_HEIGHT = 15;

    private Canvas canvas;
    private GraphicsContext gc;

    private Image grassTile;
    private Image wallTile;

    private Player player;
    private Set<KeyCode> keys = new HashSet<>();

    // Map mẫu (0 = cỏ, 1 = tường)
    private int[][] map = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    public GameView() {
        canvas = new Canvas(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        // Load hình
        grassTile = new Image(getClass().getResourceAsStream("/tiles/grass.jpg"));
        wallTile = new Image(getClass().getResourceAsStream("/tiles/wall.jpg"));

        player = new Player("Hero", 64, 64, new Image(getClass().getResourceAsStream("/sprite/Player.png")));
        System.out.println("Path test: " + getClass().getResource("/sprite/Player.png"));


        // Lắng nghe phím bấm
        setOnKeyPressed(e -> keys.add(e.getCode()));
        setOnKeyReleased(e -> keys.remove(e.getCode()));
        setFocusTraversable(true);

        startGameLoop();
    }

    private void startGameLoop() {
        AnimationTimer loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        loop.start();
    }

    private void update() {
        double dx = 0, dy = 0;
        if (keys.contains(KeyCode.UP)) dy -= 1;
        if (keys.contains(KeyCode.DOWN)) dy += 1;
        if (keys.contains(KeyCode.LEFT)) dx -= 1;
        if (keys.contains(KeyCode.RIGHT)) dx += 1;

        player.move(dx, dy);
    }

    private void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Vẽ map
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                Image tile = (map[y][x] == 1) ? wallTile : grassTile;
                gc.drawImage(tile, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Vẽ player
        gc.drawImage(player.getSprite(), player.getX(), player.getY(), TILE_SIZE, TILE_SIZE);
    }
}
