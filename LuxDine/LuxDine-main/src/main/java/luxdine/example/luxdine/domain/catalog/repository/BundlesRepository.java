package luxdine.example.luxdine.domain.catalog.repository;

import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface BundlesRepository extends JpaRepository<Bundles, Long> {
    interface BundleHit {
        Long getId();
        String getName();
        String getSlug();
    }
    List<BundleHit> findByIsActiveTrueAndNameContainingIgnoreCase(String q, Pageable pageable);
    List<Bundles> findByIsActiveTrue();
    Optional<Bundles> findBySlug(String slug);
    Optional<Bundles> findByName(String name);


    List<Bundles> findByBundleTypeAndIsActiveTrue(BundleType type);
    
    //  return all by type (both active/inactive)
    List<Bundles> findByBundleType(BundleType type);
}

