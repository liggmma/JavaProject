package luxdine.example.luxdine.domain.catalog.repository;

import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.entity.Categories;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Long>, JpaSpecificationExecutor<Items> {
    interface ItemHit {
        Long getId();
        String getName();
        String getSlug();
    }

    List<Items> findByIsAvailableTrue();
    List<Items> findByVisibility(ItemVisibility visibility);
    List<Items> findByCategoryAndIsAvailableTrue(Categories category);

    List<ItemHit> findByIsAvailableTrueAndVisibilityAndNameContainingIgnoreCase(
            ItemVisibility visibility, String q, Pageable pageable
    );

    List<Items> findByCategoryId(Long categoryId);


    Optional<Items> findBySlug(String slug);

    @Query("SELECT i FROM Items i WHERE i.isAvailable = true AND i.visibility = 'PUBLIC' ORDER BY i.soldCount DESC")
    List<Items> findBestSellingItems();

    @Query("SELECT i FROM Items i WHERE i.isAvailable = true AND i.visibility = 'PUBLIC' AND i.category = :category ORDER BY i.soldCount DESC")
    List<Items> findBestSellingItemsByCategory(@Param("category") Categories category);

    @Query("SELECT i FROM Items i " +
            "WHERE i.isAvailable = true AND i.visibility = :visibility " +
            "ORDER BY i.soldCount DESC")
    List<Items> findTop10BestSellingItems(@Param("visibility") ItemVisibility visibility, Pageable pageable);

    long countByCategoryId(Long categoryId);

}

