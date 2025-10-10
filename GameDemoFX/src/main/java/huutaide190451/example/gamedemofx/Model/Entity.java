package huutaide190451.example.gamedemofx.Model;

import javafx.scene.image.Image;
import javafx.geometry.Rectangle2D;

public abstract class Entity {
    protected String name;
    protected double x;
    protected double y;
    protected double speed;
    protected int hp;
    protected int maxHp;
    protected Image sprite;

    public Entity(String name, double x, double y, int maxHp, double speed, Image sprite) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.speed = speed;
        this.sprite = sprite;
    }

    // getter & setter
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public Image getSprite() { return sprite; }

    public void move(double dx, double dy) {
        this.x += dx * speed;
        this.y += dy * speed;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp < 0) hp = 0;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, sprite.getWidth(), sprite.getHeight());
    }
}

