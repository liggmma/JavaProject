package luxdine.example.luxdine.controller.websocket;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.chat.dto.request.MessageRequest;
import luxdine.example.luxdine.domain.chat.dto.response.MessageResponse;
import luxdine.example.luxdine.service.chat.ChatService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatWebSocketController {

    ChatService chatService;
    UserService userService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload MessageRequest request, Principal principal) {
        try {
            // Validate request
            if (request == null || request.getSessionId() == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                log.warn("Invalid message request from user: {}", principal != null ? principal.getName() : "unknown");
                sendErrorToUser(principal, "Tin nhắn không hợp lệ. Vui lòng thử lại.");
                return;
            }

            // Lấy user ID từ username
            String username = principal.getName();
            Long senderId = userService.findByUsername(username).getId();

            if (senderId == null) {
                log.error("Cannot get user ID from principal: {}", username);
                sendErrorToUser(principal, "Không thể xác định người dùng. Vui lòng đăng nhập lại.");
                return;
            }

            // Gửi tin nhắn
            MessageResponse response = chatService.sendMessage(request, senderId);
            log.debug("Message sent via WebSocket: {}", response.getId());
        } catch (Exception e) {
            log.error("Error sending message via WebSocket: {}", e.getMessage(), e);
            sendErrorToUser(principal, "Lỗi khi gửi tin nhắn: " + e.getMessage());
        }
    }

    private void sendErrorToUser(Principal principal, String errorMessage) {
        try {
            if (principal != null) {
                String username = principal.getName();
                Long userId = userService.findByUsername(username).getId();
                if (userId != null) {
                    String userTopic = "/topic/chat." + userId;
                    messagingTemplate.convertAndSend(userTopic, 
                        "{\"type\":\"ERROR\",\"message\":\"" + errorMessage + "\"}");
                }
            }
        } catch (Exception e) {
            log.error("Error sending error notification to user: {}", e.getMessage(), e);
        }
    }
}

