package luxdine.example.luxdine.domain.table.repository;

import luxdine.example.luxdine.domain.table.entity.TableLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableLayoutRepository extends JpaRepository<TableLayout, Long> {
    
    List<TableLayout> findByAreaId(Long areaId);
    
    boolean existsByAreaIdAndTableNameIgnoreCase(Long areaId, String tableName);
    
    boolean existsByAreaIdAndTableNameIgnoreCaseAndIdNot(Long areaId, String tableName, Long id);
    
}

