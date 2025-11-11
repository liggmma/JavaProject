package luxdine.example.luxdine.domain.table.repository;

import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<Tables, Long> {
    
    List<Tables> findByStatus(TableStatus status);
    List<Tables> findByStatusIn(Collection<TableStatus> status);
    
    // Link Tables <-> TableLayout
    java.util.Optional<Tables> findByTableLayoutId(Long tableLayoutId);
    
    @Query("SELECT t FROM Tables t LEFT JOIN FETCH t.area WHERE t.status = :status")
    List<Tables> findByStatusWithArea(@Param("status") TableStatus status);
    
    @Query("SELECT t FROM Tables t LEFT JOIN FETCH t.area WHERE t.status IN :statuses")
    List<Tables> findByStatusInWithArea(@Param("statuses") Collection<TableStatus> statuses);
    
    @Query("SELECT t FROM Tables t LEFT JOIN FETCH t.area WHERE t.id = :id")
    java.util.Optional<Tables> findByIdWithArea(@Param("id") Long id);
    
    @Query("SELECT t FROM Tables t WHERE (:areaName IS NULL OR t.area.name = :areaName)")
    List<Tables> findByAreaFilter(@Param("areaName") String areaName);
    
    @Query("SELECT COUNT(t) FROM Tables t WHERE t.status = :status")
    Long countByStatus(@Param("status") TableStatus status);

}
