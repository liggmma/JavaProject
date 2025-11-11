package luxdine.example.luxdine.controller.page.customer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatController {

    UserService userService;

    @GetMapping("/chat")
    @PreAuthorize("isAuthenticated()")
    public String chatPage(Principal principal, Model model) {
        try {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            
            // Kiểm tra role CUSTOMER
            if (user.getRole() != luxdine.example.luxdine.domain.user.enums.Role.CUSTOMER) {
                log.warn("User {} with role {} tried to access customer chat", username, user.getRole());
                model.addAttribute("error", "Bạn không có quyền truy cập trang này.");
                return "error/error";
            }
            
            // Không tự động tạo session, để frontend tự gọi API khi customer bấm nút
            // Chỉ truyền thông tin user
            model.addAttribute("currentUser", user);
            model.addAttribute("currentPage", "chat");
            
            return "customer/chat/chat";
        } catch (Exception e) {
            log.error("Unexpected error loading chat page: {}", e.getMessage(), e);
            model.addAttribute("error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
            return "error/error";
        }
    }
}

