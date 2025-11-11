package luxdine.example.luxdine.domain.catalog.repository;

import luxdine.example.luxdine.domain.catalog.entity.BundleItems;
import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BundleItemsRepository extends JpaRepository<BundleItems, Long> {
    @Query("SELECT bi FROM BundleItems bi " +
           "JOIN FETCH bi.item " +
           "WHERE bi.bundle = :bundle " +
           "ORDER BY bi.sortOrder")
    List<BundleItems> findByBundleOrderBySortOrder(@Param("bundle") Bundles bundle);
}

