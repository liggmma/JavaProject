package luxdine.example.luxdine.controller.page.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.service.chat.ChatService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/admin")
public class AdminChatController {

    ChatService chatService;
    UserService userService;

    @GetMapping("/chat")
    @PreAuthorize("hasRole('ADMIN')")
    public String chatPage(Principal principal, Model model) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            
            model.addAttribute("currentUser", user);
            model.addAttribute("currentPage", "chat");
            
            return "admin/chat/chat";
        } catch (Exception e) {
            log.error("Error loading admin chat page: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải trang chat. Vui lòng thử lại sau.");
            return "error/error";
        }
    }

    @GetMapping("/chat/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String chatSessionPage(@PathVariable Long sessionId, Principal principal, Model model) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            
            var session = chatService.getChatSession(sessionId, user.getId());
            
            model.addAttribute("currentUser", user);
            model.addAttribute("sessionId", sessionId);
            model.addAttribute("customerName", session.getCustomerName());
            model.addAttribute("currentPage", "chat");
            
            return "admin/chat/chat";
        } catch (Exception e) {
            log.error("Error loading admin chat session page: {}", e.getMessage(), e);
            model.addAttribute("error", "Không thể tải cuộc trò chuyện. Vui lòng thử lại sau.");
            return "error/error";
        }
    }
}

