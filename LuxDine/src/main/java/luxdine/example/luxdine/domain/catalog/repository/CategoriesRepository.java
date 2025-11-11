package luxdine.example.luxdine.domain.catalog.repository;

import luxdine.example.luxdine.domain.catalog.entity.Categories;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, Long> {
    Optional<Categories> findBySlug(String slug);
    @EntityGraph(attributePaths = "items")
    List<Categories> findAllByOrderByNameAsc();
}


