package huutaide190451.example.gamedemofx.Model;

public class Item {
    private String name;
    private String description;
    private ItemType type;
    private int value;

    public enum ItemType {
        CONSUMABLE, WEAPON, ARMOR, QUEST
    }

    public Item(String name, String description, ItemType type, int value) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.value = value;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public int getValue() { return value; }

    public void use(Player player) {
        switch (type) {
            case CONSUMABLE:
                player.takeDamage(-value); // hồi máu
                break;
            default:
                System.out.println(name + " can’t be used directly.");
        }
    }
}

