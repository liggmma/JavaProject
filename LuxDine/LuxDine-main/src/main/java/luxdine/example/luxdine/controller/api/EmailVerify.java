package luxdine.example.luxdine.controller.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import luxdine.example.luxdine.common.util.OtpSessionKeys;
import luxdine.example.luxdine.domain.user.dto.request.UserCreationRequest;
import luxdine.example.luxdine.service.auth.EmailOtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class EmailVerify {
    private final EmailOtpService emailOtpService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(HttpSession session) {
        UserCreationRequest pending = (UserCreationRequest) session.getAttribute(OtpSessionKeys.PENDING_REG);
        if (pending == null) return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Phiên đăng ký đã hết hạn."));
        return ResponseEntity.ok(emailOtpService.sendOtp(pending.getEmail(), session));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String code, HttpSession session) {
        UserCreationRequest pending = (UserCreationRequest) session.getAttribute(OtpSessionKeys.PENDING_REG);
        if (pending == null) return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Phiên đăng ký đã hết hạn."));
        return ResponseEntity.ok(emailOtpService.verifyOtp(pending.getEmail(), code, session));
    }
}
