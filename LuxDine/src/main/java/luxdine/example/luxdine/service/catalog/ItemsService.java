package luxdine.example.luxdine.service.catalog;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemsService {

    private ItemsRepository itemsRepository;

    public List<Items> getAllItemsByVisibility(){
        return itemsRepository.findByVisibility(ItemVisibility.PUBLIC);
    }

    public List<Items> getAllPublicItems(){
        List<Items> result = new ArrayList<>(itemsRepository.findAll().stream().toList());
        result.removeIf(items -> !ItemVisibility.PUBLIC.equals(items.getVisibility()));
        return result;
    }

    public List<Items> getAllBestSellerItems(){
        List<Items> result = new java.util.ArrayList<>(itemsRepository.findAll().stream().toList());
        result.removeIf(items -> !ItemVisibility.PUBLIC.equals(items.getVisibility()));
        result.sort((o1, o2) -> Integer.compare(o2.getSoldCount(), o1.getSoldCount()));
        if(result.size() > 8){
            return result.subList(0,8);
        }
        return result;
    }

    /**
     * Filter items by price range
     * Note: Allergen/dietary info currently mocked - needs database schema enhancement
     */
    public List<Items> findByPriceRange(Double minPrice, Double maxPrice) {
        return getAllPublicItems().stream()
                .filter(item -> item.getPrice() >= minPrice && item.getPrice() <= maxPrice)
                .toList();
    }

    /**
     * Find items by category
     */
    public List<Items> findByCategory(String categoryName) {
        return getAllPublicItems().stream()
                .filter(item -> item.getCategory() != null &&
                        item.getCategory().getName().equalsIgnoreCase(categoryName))
                .toList();
    }

    /**
     * Filter items by allergen (mocked - requires allergen field in Items entity)
     * TODO: Add allergen tracking to Items entity/database
     */
    public List<Items> findItemsWithoutAllergen(String allergen) {
        // Currently returns all items as allergen info is not in database
        // This should be implemented when allergen field is added to Items entity
        return getAllPublicItems();
    }

    /**
     * Find vegetarian items (mocked - requires dietary info in Items entity)
     * TODO: Add dietary flags to Items entity/database
     */
    public List<Items> findVegetarianItems() {
        // Mock implementation - check item name/description for vegetarian keywords
        return getAllPublicItems().stream()
                .filter(item -> {
                    String name = item.getName().toLowerCase();
                    String desc = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains("salad") || name.contains("vegetable") ||
                           name.contains("tofu") || desc.contains("vegetarian") ||
                           name.contains("rau") || name.contains("chay");
                })
                .toList();
    }

    /**
     * Find vegan items (mocked - requires dietary info in Items entity)
     * TODO: Add dietary flags to Items entity/database
     */
    public List<Items> findVeganItems() {
        // Mock implementation
        return getAllPublicItems().stream()
                .filter(item -> {
                    String name = item.getName().toLowerCase();
                    String desc = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains("salad") || name.contains("vegetable") ||
                           name.contains("fruit") || desc.contains("vegan") ||
                           name.contains("rau") || name.contains("trái cây");
                })
                .toList();
    }

    /**
     * Find gluten-free items (mocked - requires dietary info in Items entity)
     * TODO: Add dietary flags to Items entity/database
     */
    public List<Items> findGlutenFreeItems() {
        // Mock implementation - exclude items likely to contain gluten
        return getAllPublicItems().stream()
                .filter(item -> {
                    String name = item.getName().toLowerCase();
                    return !name.contains("bread") && !name.contains("pasta") &&
                           !name.contains("noodle") && !name.contains("pizza") &&
                           !name.contains("bánh mì") && !name.contains("mì");
                })
                .toList();
    }

    /**
     * Find spicy items (mocked - requires spice level in Items entity)
     * TODO: Add spice level field to Items entity/database
     */
    public List<Items> findSpicyItems() {
        // Mock implementation
        return getAllPublicItems().stream()
                .filter(item -> {
                    String name = item.getName().toLowerCase();
                    String desc = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains("spicy") || name.contains("hot") ||
                           name.contains("chili") || desc.contains("spicy") ||
                           name.contains("cay") || name.contains("ớt");
                })
                .toList();
    }

    /**
     * Search items by keyword in name or description
     */
    public List<Items> searchItems(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return getAllPublicItems().stream()
                .filter(item -> {
                    String name = item.getName().toLowerCase();
                    String desc = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
                    return name.contains(lowerKeyword) || desc.contains(lowerKeyword);
                })
                .toList();
    }

}
