package luxdine.example.luxdine.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.util.OtpSessionKeys;
import luxdine.example.luxdine.service.auth.AuthenticationService;
import luxdine.example.luxdine.service.auth.EmailOtpService;
import luxdine.example.luxdine.service.auth.SmsOtpService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/auth/otp-login")
public class OtpLogin {

    EmailOtpService emailOtpService;
    UserService userService;
    AuthenticationService authenticationService;
    SmsOtpService smsOtpService;

    @PostMapping("/email/send")
    public ResponseEntity<?> sendEmailOtp(@RequestParam String email, HttpSession session) {
        String em = (email == null) ? "" : email.trim().toLowerCase();
        if (!StringUtils.hasText(em)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Vui lòng nhập email."));
        }
        if (!userService.existsByEmail(em)) {
            return ResponseEntity.ok(Map.of("ok", false, "message", "Email chưa đăng ký."));
        }
        emailOtpService.sendOtp(em, session);
        session.setAttribute(OtpSessionKeys.EMAIL, em);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Đã gửi mã OTP. Vui lòng kiểm tra email."));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmailOtp(@RequestParam String email,
                                    @RequestParam String code,
                                    HttpSession session,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        String em = (email == null) ? "" : email.trim().toLowerCase();
        String cd = (code  == null) ? "" : code.trim();

        if (!StringUtils.hasText(em) || !StringUtils.hasText(cd)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Thiếu email hoặc mã OTP."));
        }

        String sessionEmail = (String) session.getAttribute(OtpSessionKeys.EMAIL);
        if (sessionEmail == null || !sessionEmail.equalsIgnoreCase(em)) {
            return ResponseEntity.ok(Map.of("ok", false, "message", "Email không khớp phiên OTP hiện tại."));
        }

        // <-- SỬA Ở ĐÂY: luôn xử lý Map
        Map<String, Object> res = emailOtpService.verifyOtp(em, cd, session);
        boolean ok = Boolean.TRUE.equals(res.get("ok"));
        String msg = String.valueOf(res.getOrDefault("message", ok ? "OK" : "Mã OTP không hợp lệ."));

        if (!ok) {
            return ResponseEntity.ok(Map.of("ok", false, "message", msg));
        }

        String username = userService.getUsernameByEmailOrThrow(em);
        authenticationService.loginAfterRegisterWithoutPassword(username, request, response);

        // Dọn session OTP login
        session.removeAttribute(OtpSessionKeys.HASH);
        session.removeAttribute(OtpSessionKeys.EXPIRES);
        session.removeAttribute(OtpSessionKeys.ATTEMPTS);
        session.removeAttribute(OtpSessionKeys.LOCK_UNTIL);
        session.removeAttribute(OtpSessionKeys.EMAIL);
        session.removeAttribute(OtpSessionKeys.VERIFIED);
        session.removeAttribute(OtpSessionKeys.VERIFIED_EMAIL);

        return ResponseEntity.ok(Map.of("ok", true));
    }


    @PostMapping("/sms/send")
    public ResponseEntity<?> sendSmsOtp(@RequestParam String phone, HttpSession session) {
        if (!StringUtils.hasText(phone)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Vui lòng nhập số điện thoại."));
        }
        String e164 = userService.normalizePhoneVN(phone);
        if (e164 == null) {
            return ResponseEntity.ok(Map.of("ok", false, "message", "Số điện thoại không hợp lệ."));
        }
        if (!userService.existsByPhone(phone)) {
            return ResponseEntity.ok(Map.of("ok", false, "message", "Số điện thoại chưa đăng ký."));
        }

        var res = smsOtpService.sendOtp(e164, session);
        if (Boolean.TRUE.equals(res.get("ok"))) {
            session.setAttribute(OtpSessionKeys.SMS_PHONE, e164); // khóa phiên theo 1 số
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<?> verifySmsOtp(@RequestParam String phone,
                                    @RequestParam String code,
                                    HttpSession session,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String e164 = userService.normalizePhoneVN(phone);
        if (e164 == null || !StringUtils.hasText(code)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Thiếu SĐT hoặc mã OTP."));
        }

        String sessionPhone = (String) session.getAttribute(OtpSessionKeys.SMS_PHONE);
        if (sessionPhone == null || !sessionPhone.equals(e164)) {
            return ResponseEntity.ok(Map.of("ok", false, "message", "SĐT không khớp phiên OTP hiện tại."));
        }

        Map<String, Object> res = smsOtpService.verifyOtp(e164, code.trim(), session);
        boolean ok = Boolean.TRUE.equals(res.get("ok"));
        String msg = String.valueOf(res.getOrDefault("message", ok ? "OK" : "Mã OTP không hợp lệ."));

        if (!ok) return ResponseEntity.ok(Map.of("ok", false, "message", msg));

        String username = userService.getUsernameByPhoneOrThrow(e164);
        authenticationService.loginAfterRegisterWithoutPassword(username, request, response);

        // Dọn session dấu vết SMS OTP
        smsOtpService.clearSmsSession(session);

        return ResponseEntity.ok(Map.of("ok", true));
    }

}