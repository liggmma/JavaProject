package huutaide190451.example.gamedemofx.Model;

import javafx.scene.image.Image;

public class Enemy extends Entity {
    private int attackPower;
    private int expReward;

    public Enemy(String name, double x, double y, Image sprite, int hp, int attackPower, int expReward) {
        super(name, x, y, hp, 2.0, sprite);
        this.attackPower = attackPower;
        this.expReward = expReward;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public int getExpReward() {
        return expReward;
    }

    public void attack(Player player) {
        player.takeDamage(attackPower);
    }
}

