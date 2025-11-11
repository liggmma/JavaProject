package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.chat.dto.response.ChatListResponse;
import luxdine.example.luxdine.domain.chat.dto.response.ChatSessionResponse;
import luxdine.example.luxdine.domain.chat.dto.response.MessageResponse;
import luxdine.example.luxdine.service.chat.ChatService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import luxdine.example.luxdine.domain.chat.enums.ChatSessionStatus;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/chat")
public class ChatAPI {

    ChatService chatService;
    UserService userService;

    @GetMapping("/sessions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatListResponse> getChatSessions(
            Authentication authentication,
            @RequestParam(required = false) String status) {
        try {
            String username = authentication.getName();
            Long userId = userService.findByUsername(username).getId();
            
            // Xác định role để trả về danh sách phù hợp
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = authorities.stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_STAFF") 
                                  || auth.getAuthority().equals("ROLE_ADMIN"));
            
            ChatListResponse response;
            if (isAdmin) {
                // Admin có thể filter theo status
                ChatSessionStatus filterStatus = null;
                if (status != null && !status.isEmpty()) {
                    try {
                        filterStatus = ChatSessionStatus.valueOf(status.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid status filter: {}", status);
                    }
                }
                response = chatService.getChatSessionsForAdmin(userId, filterStatus);
            } else if (isStaff) {
                response = chatService.getChatSessionsForStaff(userId);
            } else {
                response = chatService.getChatSessionsForCustomer(userId);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting chat sessions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MessageResponse>> getChatMessages(
            @PathVariable Long sessionId,
            Principal principal) {
        try {
            String username = principal.getName();
            Long userId = userService.findByUsername(username).getId();
            
            List<MessageResponse> messages = chatService.getChatHistory(sessionId, userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error getting chat messages: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ChatSessionResponse> createChatSession(Principal principal) {
        try {
            String username = principal.getName();
            Long customerId = userService.findByUsername(username).getId();
            
            ChatSessionResponse session = chatService.createOrGetChatSession(customerId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error creating chat session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatSessionResponse> getChatSession(
            @PathVariable Long sessionId,
            Principal principal) {
        try {
            String username = principal.getName();
            Long userId = userService.findByUsername(username).getId();
            
            ChatSessionResponse session = chatService.getChatSession(sessionId, userId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error getting chat session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long sessionId,
            Principal principal) {
        try {
            String username = principal.getName();
            Long userId = userService.findByUsername(username).getId();
            
            chatService.markMessagesAsRead(sessionId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/join")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ChatSessionResponse> joinChatSession(
            @PathVariable Long sessionId,
            Principal principal) {
        try {
            String username = principal.getName();
            Long staffId = userService.findByUsername(username).getId();
            
            ChatSessionResponse session = chatService.joinChatSession(sessionId, staffId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error joining chat session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sessions/{sessionId}/end")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Void> endChatSession(
            @PathVariable Long sessionId,
            Principal principal) {
        try {
            String username = principal.getName();
            Long staffId = userService.findByUsername(username).getId();
            
            chatService.endChatSession(sessionId, staffId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error ending chat session: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

