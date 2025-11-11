package luxdine.example.luxdine.domain.table.repository;

import luxdine.example.luxdine.domain.table.entity.Areas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AreasRepository extends JpaRepository<Areas, Long> {
    List<Areas> findByFloor(Integer floor);
    
    @Query("SELECT DISTINCT a.floor FROM Areas a ORDER BY a.floor")
    List<Integer> findDistinctFloors();
}
