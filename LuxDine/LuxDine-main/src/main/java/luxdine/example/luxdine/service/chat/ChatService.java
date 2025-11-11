package luxdine.example.luxdine.service.chat;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.chat.dto.request.MessageRequest;
import luxdine.example.luxdine.domain.chat.dto.response.ChatListResponse;
import luxdine.example.luxdine.domain.chat.dto.response.ChatSessionResponse;
import luxdine.example.luxdine.domain.chat.dto.response.MessageResponse;
import luxdine.example.luxdine.domain.chat.dto.response.SessionEndedNotification;
import luxdine.example.luxdine.domain.chat.dto.response.StaffJoinedNotification;
import luxdine.example.luxdine.domain.chat.entity.ChatMessage;
import luxdine.example.luxdine.domain.chat.entity.ChatSession;
import luxdine.example.luxdine.domain.chat.enums.ChatSessionStatus;
import luxdine.example.luxdine.domain.chat.repository.ChatMessageRepository;
import luxdine.example.luxdine.domain.chat.repository.ChatSessionRepository;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatService {

    ChatSessionRepository chatSessionRepository;
    ChatMessageRepository chatMessageRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, Long senderId) {
        // Lấy session
        ChatSession session = chatSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        // Kiểm tra session phải ACTIVE (hoặc NULL - coi như ACTIVE)
        // Customer không thể gửi vào session ENDED
        // Staff/Admin có thể gửi vào session ENDED để tiếp tục hỗ trợ (nếu cần)
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
        boolean isCustomer = session.getCustomerId().equals(senderId);
        
        // Kiểm tra session status
        if (session.getStatus() == ChatSessionStatus.WAITING) {
            throw new RuntimeException("Chưa có staff tham gia phòng. Vui lòng chờ staff tham gia.");
        }
        
        if (session.getStatus() == ChatSessionStatus.ENDED) {
            // Customer không thể gửi vào session ENDED
            if (isCustomer) {
                throw new RuntimeException("Cuộc trò chuyện đã kết thúc");
            }
            // Staff/Admin có thể gửi vào session ENDED -> tự động reopen session
            session.setStatus(ChatSessionStatus.ACTIVE);
            chatSessionRepository.save(session);
            log.info("Session {} reopened by staff/admin {}", session.getId(), senderId);
        }
        
        // Set status ACTIVE nếu NULL (migration cho session cũ)
        if (session.getStatus() == null) {
            session.setStatus(ChatSessionStatus.ACTIVE);
            chatSessionRepository.save(session);
        }
        
        // Kiểm tra staffId không null khi customer gửi
        if (isCustomer && session.getStaffId() == null) {
            throw new RuntimeException("Chưa có staff tham gia phòng. Vui lòng chờ staff tham gia.");
        }

        // Xác định receiver
        // Customer chỉ có thể gửi cho staff được assign
        // Staff nào cũng có thể reply cho customer
        Long receiverId;
        
        if (session.getCustomerId().equals(senderId)) {
            // Customer gửi tin nhắn -> receiver là staff được assign
            if (session.getStaffId() == null) {
                throw new RuntimeException("Chưa có staff tham gia phòng. Vui lòng chờ staff tham gia.");
            }
            receiverId = session.getStaffId();
        } else if (sender.getRole() == Role.STAFF || sender.getRole() == Role.ADMIN) {
            // Staff hoặc Admin có thể reply bất kỳ customer nào
            receiverId = session.getCustomerId();
        } else {
            throw new RuntimeException("Bạn không có quyền gửi tin nhắn trong cuộc trò chuyện này");
        }

        // Tạo và lưu tin nhắn
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .session(session)
                .message(request.getMessage())
                .isRead(false)
                .sentAt(Instant.now())
                .build();

        message = chatMessageRepository.save(message);

        // Cập nhật lastMessageAt của session
        session.setLastMessageAt(Instant.now());
        chatSessionRepository.save(session);

        // Lấy thông tin receiver để map sang response
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

        MessageResponse response = MessageResponse.builder()
                .id(message.getId())
                .senderId(senderId)
                .senderName(getUserDisplayName(sender))
                .senderRole(sender.getRole().name()) // Thêm role của người gửi
                .receiverId(receiverId)
                .receiverName(getUserDisplayName(receiver))
                .sessionId(session.getId())
                .message(message.getMessage())
                .isRead(false)
                .sentAt(message.getSentAt())
                .build();

        // Broadcast tin nhắn đến receiver qua WebSocket
        String receiverTopic = "/topic/chat." + receiverId;
        String senderTopic = "/topic/chat." + senderId;
        
        messagingTemplate.convertAndSend(receiverTopic, response);
        // Cũng gửi lại cho sender để confirm
        messagingTemplate.convertAndSend(senderTopic, response);

        log.info("Message sent from {} to {} in session {}. Broadcasted to topics: {}, {}", 
                senderId, receiverId, session.getId(), receiverTopic, senderTopic);
        return response;
    }

    public List<MessageResponse> getChatHistory(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        // Kiểm tra user có quyền xem session này không
        // Customer chỉ có thể xem session của chính họ
        // Staff có thể xem tất cả sessions
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        boolean isCustomer = session.getCustomerId().equals(userId);
        boolean isStaff = user.getRole() == Role.STAFF || user.getRole() == Role.ADMIN;
        
        if (!isCustomer && !isStaff && (session.getStaffId() == null || !session.getStaffId().equals(userId))) {
            throw new RuntimeException("Bạn không có quyền xem cuộc trò chuyện này");
        }

        // Customer chỉ có thể xem history của session ACTIVE hoặc WAITING
        if (isCustomer && session.getStatus() == ChatSessionStatus.ENDED) {
            throw new RuntimeException("Cuộc trò chuyện đã kết thúc");
        }
        // Set status ACTIVE nếu NULL (migration cho session cũ)
        if (session.getStatus() == null) {
            session.setStatus(ChatSessionStatus.ACTIVE);
            chatSessionRepository.save(session);
        }

        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderBySentAtAsc(sessionId);

        // Nếu là customer và session đã được reopen (có reopenedAt), chỉ hiển thị tin nhắn từ lúc reopen
        // Staff/admin luôn thấy toàn bộ lịch sử
        if (isCustomer && session.getReopenedAt() != null) {
            final Instant reopenedAt = session.getReopenedAt();
            messages = messages.stream()
                    .filter(msg -> msg.getSentAt().isAfter(reopenedAt) || msg.getSentAt().equals(reopenedAt))
                    .collect(Collectors.toList());
            log.debug("Filtered messages for customer: showing {} messages after reopen at {}", 
                    messages.size(), reopenedAt);
        }

        return messages.stream().map(msg -> {
            User sender = userRepository.findById(msg.getSenderId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người gửi"));
            User receiver = userRepository.findById(msg.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận"));

            return MessageResponse.builder()
                    .id(msg.getId())
                    .senderId(msg.getSenderId())
                    .senderName(getUserDisplayName(sender))
                    .senderRole(sender.getRole().name()) // Thêm role của người gửi
                    .receiverId(msg.getReceiverId())
                    .receiverName(getUserDisplayName(receiver))
                    .sessionId(sessionId)
                    .message(msg.getMessage())
                    .isRead(msg.getIsRead())
                    .sentAt(msg.getSentAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public ChatListResponse getChatSessionsForCustomer(Long customerId) {
        // Customer thấy sessions WAITING và ACTIVE
        List<ChatSession> activeSessions = chatSessionRepository.findByCustomerIdAndStatusOrderByLastMessageAtDesc(customerId, ChatSessionStatus.ACTIVE);
        List<ChatSession> waitingSessions = chatSessionRepository.findByCustomerIdAndStatusOrderByLastMessageAtDesc(customerId, ChatSessionStatus.WAITING);
        
        // Merge và sort
        List<ChatSession> sessions = new java.util.ArrayList<>();
        sessions.addAll(activeSessions);
        sessions.addAll(waitingSessions);
        sessions.sort((s1, s2) -> {
            Instant time1 = s1.getLastMessageAt() != null ? s1.getLastMessageAt() : s1.getCreatedAt();
            Instant time2 = s2.getLastMessageAt() != null ? s2.getLastMessageAt() : s2.getCreatedAt();
            return time2.compareTo(time1);
        });
        
        return buildChatListResponse(sessions, customerId);
    }

    public ChatListResponse getChatSessionsForStaff(Long staffId) {
        // Staff thấy:
        // 1. Tất cả WAITING sessions (chưa có staff join)
        // 2. ACTIVE sessions của chính staff đó
        List<ChatSession> waitingSessions = chatSessionRepository.findByStatusOrderByCreatedAtAsc(ChatSessionStatus.WAITING);
        List<ChatSession> activeSessions = chatSessionRepository.findByStaffIdAndStatusIn(
                staffId, List.of(ChatSessionStatus.ACTIVE));
        
        // Merge và sort
        List<ChatSession> sessions = new java.util.ArrayList<>();
        sessions.addAll(waitingSessions);
        sessions.addAll(activeSessions);
        sessions.sort((s1, s2) -> {
            Instant time1 = s1.getLastMessageAt() != null ? s1.getLastMessageAt() : s1.getCreatedAt();
            Instant time2 = s2.getLastMessageAt() != null ? s2.getLastMessageAt() : s2.getCreatedAt();
            return time2.compareTo(time1);
        });
        
        log.info("Found {} chat sessions for staff {} ({} waiting, {} active)", 
                sessions.size(), staffId, waitingSessions.size(), activeSessions.size());
        return buildChatListResponse(sessions, staffId);
    }
    
    public ChatListResponse getChatSessionsForAdmin(Long adminId, ChatSessionStatus filterStatus) {
        List<ChatSession> sessions;
        
        if (filterStatus == null) {
            // Tất cả sessions
            sessions = chatSessionRepository.findAllOrderByLastMessageAtDesc();
        } else {
            // Filter theo status
            sessions = chatSessionRepository.findByStatusOrderByCreatedAtAsc(filterStatus);
            // Sort lại theo lastMessageAt DESC
            sessions.sort((s1, s2) -> {
                Instant time1 = s1.getLastMessageAt() != null ? s1.getLastMessageAt() : s1.getCreatedAt();
                Instant time2 = s2.getLastMessageAt() != null ? s2.getLastMessageAt() : s2.getCreatedAt();
                return time2.compareTo(time1);
            });
        }
        
        log.info("Found {} chat sessions for admin {} (filter: {})", sessions.size(), adminId, filterStatus);
        return buildChatListResponse(sessions, adminId);
    }

    @Transactional
    public ChatSessionResponse createOrGetChatSession(Long customerId) {
        // Tìm session ACTIVE hoặc WAITING đã tồn tại (bỏ qua ENDED)
        List<ChatSession> activeSessions = chatSessionRepository.findByCustomerIdAndStatusOrderByLastMessageAtDesc(customerId, ChatSessionStatus.ACTIVE);
        List<ChatSession> waitingSessions = chatSessionRepository.findByCustomerIdAndStatusOrderByLastMessageAtDesc(customerId, ChatSessionStatus.WAITING);
        
        if (!activeSessions.isEmpty()) {
            return mapToChatSessionResponse(activeSessions.get(0), customerId);
        }
        
        if (!waitingSessions.isEmpty()) {
            return mapToChatSessionResponse(waitingSessions.get(0), customerId);
        }

        // Nếu chưa có session, tạo session mới với status WAITING (không assign staff)
        ChatSession session = ChatSession.builder()
                .customerId(customerId)
                .staffId(null) // Chưa có staff join
                .status(ChatSessionStatus.WAITING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        session = chatSessionRepository.save(session);
        
        log.info("Created new WAITING chat session {} for customer {}", session.getId(), customerId);
        
        return mapToChatSessionResponse(session, customerId);
    }
    
    @Transactional
    public ChatSessionResponse joinChatSession(Long sessionId, Long staffId) {
        // Dùng pessimistic lock để tránh race condition
        ChatSession session = chatSessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));
        
        // Kiểm tra session có status WAITING không
        if (session.getStatus() != ChatSessionStatus.WAITING) {
            throw new RuntimeException("Phòng này không còn chờ staff tham gia");
        }
        
        // Kiểm tra session chưa có staffId
        if (session.getStaffId() != null) {
            throw new RuntimeException("Đã có staff khác tham gia phòng này");
        }
        
        // Lấy thông tin staff
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        
        // Kiểm tra staff có role STAFF không (không cho ADMIN join)
        if (staff.getRole() != Role.STAFF) {
            throw new RuntimeException("Chỉ nhân viên mới có thể tham gia phòng hỗ trợ");
        }
        
        // Set staffId và status
        session.setStaffId(staffId);
        session.setStatus(ChatSessionStatus.ACTIVE);
        session.setJoinedAt(Instant.now());
        session.setUpdatedAt(Instant.now());
        session = chatSessionRepository.save(session);
        
        log.info("Staff {} joined chat session {}", staffId, sessionId);
        
        // Broadcast notification đến customer
        String customerTopic = "/topic/chat." + session.getCustomerId();
        String staffName = getUserDisplayName(staff);
        StaffJoinedNotification notification = StaffJoinedNotification.of(sessionId, staffId, staffName);
        messagingTemplate.convertAndSend(customerTopic, notification);
        
        log.debug("Sent STAFF_JOINED notification to customer {} for session {}", session.getCustomerId(), sessionId);
        
        return mapToChatSessionResponse(session, session.getCustomerId());
    }
    
    @Transactional
    public void timeoutWaitingSessions() {
        Instant thirtyMinutesAgo = Instant.now().minusSeconds(30 * 60); // 30 phút
        List<ChatSession> timeoutSessions = chatSessionRepository.findByStatusAndCreatedAtBefore(
                ChatSessionStatus.WAITING, thirtyMinutesAgo);
        
        if (timeoutSessions.isEmpty()) {
            return;
        }
        
        log.info("Found {} WAITING sessions to timeout", timeoutSessions.size());
        
        for (ChatSession session : timeoutSessions) {
            session.setStatus(ChatSessionStatus.ENDED);
            session.setUpdatedAt(Instant.now());
            chatSessionRepository.save(session);
            
            // Broadcast notification đến customer
            String customerTopic = "/topic/chat." + session.getCustomerId();
            SessionEndedNotification notification = SessionEndedNotification.of(session.getId());
            messagingTemplate.convertAndSend(customerTopic, notification);
            
            log.info("Timeout WAITING session {} for customer {}", session.getId(), session.getCustomerId());
        }
    }

    @Transactional
    public void markMessagesAsRead(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        // Kiểm tra quyền: Customer chỉ có thể đánh dấu session của mình, Staff có thể đánh dấu tất cả
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        boolean isCustomer = session.getCustomerId().equals(userId);
        boolean isStaff = user.getRole() == Role.STAFF || user.getRole() == Role.ADMIN;
        
        if (!isCustomer && !isStaff && !session.getStaffId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền đánh dấu tin nhắn trong cuộc trò chuyện này");
        }

        List<ChatMessage> unreadMessages = chatMessageRepository
                .findUnreadMessageListBySessionAndReceiver(sessionId, userId);

        unreadMessages.forEach(msg -> msg.setIsRead(true));
        chatMessageRepository.saveAll(unreadMessages);
    }

    private ChatListResponse buildChatListResponse(List<ChatSession> sessions, Long currentUserId) {
        List<ChatSessionResponse> sessionResponses = sessions.stream()
                .map(session -> mapToChatSessionResponse(session, currentUserId))
                .collect(Collectors.toList());

        Long totalUnread = chatMessageRepository.countUnreadMessagesByReceiverId(currentUserId);
        
        log.debug("Built chat list response with {} sessions, {} total unread for user {}", 
                sessionResponses.size(), totalUnread, currentUserId);

        return ChatListResponse.builder()
                .sessions(sessionResponses)
                .totalUnreadCount(totalUnread)
                .build();
    }

    private ChatSessionResponse mapToChatSessionResponse(ChatSession session, Long currentUserId) {
        User customer = userRepository.findById(session.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        
        // Staff có thể null khi session WAITING
        String staffName = null;
        if (session.getStaffId() != null) {
            try {
                User staff = userRepository.findById(session.getStaffId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
                staffName = getUserDisplayName(staff);
            } catch (Exception e) {
                log.warn("Could not find staff {} for session {}", session.getStaffId(), session.getId());
            }
        }

        // Lấy tin nhắn cuối cùng
        List<ChatMessage> messages = chatMessageRepository
                .findBySessionIdOrderBySentAtAsc(session.getId());
        String lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessage();

        // Đếm số tin nhắn chưa đọc trong session này
        Long unreadCount = chatMessageRepository.countUnreadMessagesBySessionAndReceiver(
                session.getId(), currentUserId);

        return ChatSessionResponse.builder()
                .id(session.getId())
                .customerId(session.getCustomerId())
                .customerName(getUserDisplayName(customer))
                .staffId(session.getStaffId())
                .staffName(staffName)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .lastMessageAt(session.getLastMessageAt())
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .build();
    }

    private String getUserDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else {
            return user.getUsername();
        }
    }

    public ChatSessionResponse getChatSession(Long sessionId, Long userId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        // Kiểm tra quyền: Customer chỉ có thể xem session của mình, Staff có thể xem tất cả
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        boolean isCustomer = session.getCustomerId().equals(userId);
        boolean isStaff = user.getRole() == Role.STAFF || user.getRole() == Role.ADMIN;
        
        if (!isCustomer && !isStaff && !session.getStaffId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem cuộc trò chuyện này");
        }

        return mapToChatSessionResponse(session, userId);
    }

    @Transactional
    public void endChatSession(Long sessionId, Long staffId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        // Chỉ staff được assign hoặc admin mới có quyền kết thúc chat
        User user = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isAssignedStaff = session.getStaffId() != null && session.getStaffId().equals(staffId);
        
        // Chỉ admin hoặc staff được assign mới có quyền kết thúc
        if (!isAdmin && !isAssignedStaff) {
            throw new RuntimeException("Bạn không có quyền kết thúc cuộc trò chuyện này");
        }

        // Đánh dấu session là ENDED
        session.setStatus(ChatSessionStatus.ENDED);
        session.setUpdatedAt(Instant.now());
        session.setReopenedAt(null); // Clear reopenedAt khi end session
        chatSessionRepository.save(session);

        log.info("Chat session {} ended by staff {}", sessionId, staffId);

        // Broadcast message đến customer để đóng chat UI
        String customerTopic = "/topic/chat." + session.getCustomerId();
        SessionEndedNotification notification = SessionEndedNotification.of(sessionId);
        messagingTemplate.convertAndSend(customerTopic, notification);
        
        log.debug("Sent SESSION_ENDED notification to customer {} for session {}", session.getCustomerId(), sessionId);
    }
}

