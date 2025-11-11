package luxdine.example.luxdine.domain.chat.repository;

import luxdine.example.luxdine.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId ORDER BY m.sentAt ASC")
    List<ChatMessage> findBySessionIdOrderBySentAtAsc(@Param("sessionId") Long sessionId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessagesByReceiverId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.session.id = :sessionId AND m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessagesBySessionAndReceiver(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
    
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId AND m.receiverId = :userId AND m.isRead = false")
    List<ChatMessage> findUnreadMessageListBySessionAndReceiver(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}

