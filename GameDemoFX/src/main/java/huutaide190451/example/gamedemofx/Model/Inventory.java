package huutaide190451.example.gamedemofx.Model;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Item> items;

    public Inventory() {
        this.items = new ArrayList<>();
    }

    public void add(Item item) {
        items.add(item);
    }

    public void remove(Item item) {
        items.remove(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public void listItems() {
        items.forEach(i -> System.out.println(i.getName() + " - " + i.getType()));
    }
}

