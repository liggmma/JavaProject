package luxdine.example.luxdine.domain.table.repository;

import luxdine.example.luxdine.domain.table.entity.FloorFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FloorFeatureRepository extends JpaRepository<FloorFeature, Long> {
    List<FloorFeature> findByArea_Id(Long areaId);
}


