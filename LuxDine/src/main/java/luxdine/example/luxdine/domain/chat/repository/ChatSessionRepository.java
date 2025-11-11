package luxdine.example.luxdine.domain.chat.repository;

import jakarta.persistence.LockModeType;
import luxdine.example.luxdine.domain.chat.entity.ChatSession;
import luxdine.example.luxdine.domain.chat.enums.ChatSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatSession> findByCustomerIdAndStaffId(Long customerId, Long staffId);
    
    @Query("SELECT s FROM ChatSession s WHERE s.customerId = :customerId ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findByCustomerIdOrderByLastMessageAtDesc(@Param("customerId") Long customerId);
    
    @Query("SELECT s FROM ChatSession s WHERE s.customerId = :customerId AND (s.status = :status OR s.status IS NULL) ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findByCustomerIdAndStatusOrderByLastMessageAtDesc(@Param("customerId") Long customerId, @Param("status") ChatSessionStatus status);
    
    @Query("SELECT s FROM ChatSession s WHERE s.staffId = :staffId ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findByStaffIdOrderByLastMessageAtDesc(@Param("staffId") Long staffId);
    
    @Query("SELECT s FROM ChatSession s WHERE s.customerId = :customerId OR s.staffId = :staffId ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findByCustomerIdOrStaffIdOrderByLastMessageAtDesc(@Param("customerId") Long customerId, @Param("staffId") Long staffId);
    
    @Query("SELECT s FROM ChatSession s ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findAllOrderByLastMessageAtDesc();
    
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.staffId = :staffId AND (s.status = :status OR s.status IS NULL)")
    Long countActiveSessionsByStaffId(@Param("staffId") Long staffId, @Param("status") ChatSessionStatus status);
    
    @Query("SELECT s FROM ChatSession s WHERE s.status = :status ORDER BY s.createdAt ASC")
    List<ChatSession> findByStatusOrderByCreatedAtAsc(@Param("status") ChatSessionStatus status);
    
    @Query("SELECT s FROM ChatSession s WHERE s.staffId = :staffId AND s.status IN :statuses ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findByStaffIdAndStatusIn(@Param("staffId") Long staffId, @Param("statuses") List<ChatSessionStatus> statuses);
    
    @Query("SELECT s FROM ChatSession s WHERE s.status = :status AND s.createdAt < :before")
    List<ChatSession> findByStatusAndCreatedAtBefore(@Param("status") ChatSessionStatus status, @Param("before") Instant before);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ChatSession s WHERE s.id = :sessionId")
    Optional<ChatSession> findByIdWithLock(@Param("sessionId") Long sessionId);
}

