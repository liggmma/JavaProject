package luxdine.example.luxdine.domain.catalog.repository;

import luxdine.example.luxdine.domain.catalog.entity.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    /**
     * Tìm Allergen bằng tên (name), phân biệt chữ hoa/thường.
     * Dùng để kiểm tra xem allergen đã tồn tại hay chưa.
     */
    Optional<Allergen> findByName(String name);
}