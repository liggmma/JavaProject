package huutaide190451.example.gamedemofx.Model;

import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    private int level;
    private int exp;
    private List<Item> inventory;

    public Player(String name, double x, double y, Image sprite) {
        super(name, x, y, 100, 1.0, sprite);
        this.level = 1;
        this.exp = 0;
        this.inventory = new ArrayList<>();
    }

    public void gainExp(int amount) {
        exp += amount;
        if (exp >= 100) {
            exp -= 100;
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        maxHp += 20;
        hp = maxHp;
        speed += 0.2;
        System.out.println(name + " leveled up to " + level + "!");
    }

    public void addItem(Item item) {
        inventory.add(item);
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }
}
