package luxdine.example.luxdine.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.service.chat.ChatService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionTimeoutScheduler {
    
    private final ChatService chatService;
    
    /**
     * Timeout WAITING sessions sau 30 phút không có staff join
     * Chạy mỗi 1 phút
     */
    @Scheduled(fixedRate = 60000) // 1 phút
    public void timeoutWaitingSessions() {
        try {
            chatService.timeoutWaitingSessions();
        } catch (Exception e) {
            log.error("Error in timeoutWaitingSessions scheduler: {}", e.getMessage(), e);
        }
    }
}

