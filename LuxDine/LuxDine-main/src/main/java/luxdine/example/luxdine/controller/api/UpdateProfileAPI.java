package luxdine.example.luxdine.controller.api;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.user.dto.request.ChangePasswordRequest;
import luxdine.example.luxdine.service.auth.EmailOtpService;
import luxdine.example.luxdine.service.auth.SmsOtpService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import static luxdine.example.luxdine.common.util.OtpSessionKeys.SMS_PHONE;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileAPI {
    final UserService userService;
    final EmailOtpService emailOtpService;
    final SmsOtpService smsOtpService;

    private static final String REAUTH_AT = "REAUTHS_AT";
    private static final long   REAUTH_TTL_MS = 5 * 60 * 1000L; // 5 phút

    @PostMapping("/profile/name")
    public Map<String,Object> updateName(@RequestParam String fullName, Principal me){
        userService.updateFullName(me.getName(), fullName); // bạn parse fullName -> first/last bên service
        return Map.of("ok", true, "value", fullName);
    }

    @PostMapping("/profile/email")
    public Map<String, Object> updateEmail(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String currentPassword,  // NEW
            Principal me,
            HttpSession session
    ) {
        // CONFIRM step (giữ nguyên)
        if (code != null) {
            var res = emailOtpService.verifyOtpForEmailChange(code.trim(), session);
            if (!Boolean.TRUE.equals(res.get("ok"))) return res;
            String newEmail = String.valueOf(res.get("email"));
            String saved = userService.updateEmail(me.getName(), newEmail);
            emailOtpService.clear(session);
            return Map.of("ok", true, "value", saved);
        }

        // START step (NEW: reauth required)
        if (email == null || email.isBlank()) {
            return Map.of("ok", false, "message", "Vui lòng nhập email mới.");
        }
        var reauthErr = requireReauth(session, me.getName(), currentPassword); // NEW
        if (reauthErr != null) return reauthErr;                                // NEW

        try {
            String normalized = email.trim().toLowerCase();
            String current = String.valueOf(userService.findByUsername(me.getName()).getEmail());
            if (normalized.equalsIgnoreCase(current)) {
                return Map.of("ok", false, "message", "Email mới trùng với email hiện tại.");
            }
            if (userService.existsByEmail(normalized)) {
                return Map.of("ok", false, "message", "Email đã được sử dụng bởi tài khoản khác.");
            }
        } catch (Exception e) {
            return Map.of("ok", false, "message", e.getMessage());
        }

        // Gửi OTP & trả VERIFY như cũ
        var send = emailOtpService.sendOtpToNewEmail(email.trim(), session);
        if (!Boolean.TRUE.equals(send.get("ok"))) return send;
        long retryAfter = Long.parseLong(String.valueOf(send.getOrDefault("retryAfter", 60)));
        return Map.of("ok", true, "step", "VERIFY", "retryAfter", retryAfter);
    }

    @PostMapping("/profile/email/cancel")
    public Map<String,Object> cancelEmailChange(HttpSession session){
        emailOtpService.clear(session);
        return Map.of("ok", true);
    }

    @PostMapping("/profile/phone")
    public Map<String, Object> updatePhone(
            @RequestParam(required = false, name = "phoneNumber") String phoneNumber,
            @RequestParam(required = false, name = "code") String code,
            @RequestParam(required = false) String currentPassword,            // NEW
            Principal me,
            HttpSession session
    ) {
        // VERIFY step (giữ nguyên)
        if (code != null) {
            String pendingE164 = String.valueOf(session.getAttribute(SMS_PHONE));
            if (pendingE164 == null || pendingE164.equals("null")) {
                return Map.of("ok", false, "message", "Chưa yêu cầu mã hoặc mã đã hết hạn.");
            }
            var res = smsOtpService.verifyOtp(pendingE164, code.trim(), session);
            if (!Boolean.TRUE.equals(res.get("ok"))) return res;

            String saved = userService.updatePhone(me.getName(), pendingE164);
            smsOtpService.clearSmsSession(session);
            return Map.of("ok", true, "value", saved);
        }

        // START step (NEW: reauth required)
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Map.of("ok", false, "message", "Vui lòng nhập số điện thoại.");
        }
        var reauthErr = requireReauth(session, me.getName(), currentPassword); // NEW
        if (reauthErr != null) return reauthErr;                               // NEW

        String e164 = userService.normalizePhoneVN(phoneNumber);
        if (e164 == null) {
            return Map.of("ok", false, "message", "Số điện thoại không hợp lệ.");
        }
        String current = String.valueOf(userService.findByUsername(me.getName()).getPhoneNumber());
        if (e164.equals(current)) {
            return Map.of("ok", false, "message", "Số điện thoại mới trùng với số hiện tại.");
        }
        if (userService.existsByPhone(e164)) {
            return Map.of("ok", false, "message", "Số điện thoại đã được sử dụng bởi tài khoản khác.");
        }
        var send = smsOtpService.sendOtp(e164, session);
        if (!Boolean.TRUE.equals(send.get("ok"))) return send;

        long retryAfter = Long.parseLong(String.valueOf(send.getOrDefault("retryAfter", 60)));
        return Map.of("ok", true, "step", "VERIFY", "retryAfter", retryAfter);
    }

    @PostMapping("/profile/phone/cancel")
    public Map<String, Object> cancelPhoneChange(HttpSession session){
        smsOtpService.clearSmsSession(session);
        return Map.of("ok", true);
    }

    @PostMapping("/profile/allergens")
    public Map<String,Object> updateAllergens(@RequestParam String allergensCsv, Principal me){
        userService.updateAllergensCsv(me.getName(), allergensCsv);
        return Map.of("ok", true, "value", allergensCsv);
    }

    @PostMapping(value = "/profile/password", consumes = "application/x-www-form-urlencoded", produces = "application/json")
    public Map<String, Object> changePasswordApi(@Valid ChangePasswordRequest req,
                                                 BindingResult br,
                                                 Principal principal) {
        // Lỗi validate DTO (minlength, required, …)
        if (br.hasErrors()) {
            Map<String, String> fieldErrors = new LinkedHashMap<>();
            for (FieldError fe : br.getFieldErrors()) {
                fieldErrors.put(fe.getField(), fe.getDefaultMessage());
            }
            // Nếu có global errors
            var globals = br.getGlobalErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return Map.of(
                    "ok", false,
                    "errors", fieldErrors,
                    "messages", globals
            );
        }

        // Business check (currentPassword sai, confirm không khớp, …)
        String result = userService.changePassword(principal.getName(), req);
        if (result != null) {
            return Map.of("ok", false, "message", result);
        }
        return Map.of("ok", true, "message", "Đổi mật khẩu thành công");
    }

    private boolean isReauthed(HttpSession session) {
        Object t = session.getAttribute(REAUTH_AT);
        if (t instanceof Long at) {
            return (System.currentTimeMillis() - at) < REAUTH_TTL_MS;
        }
        return false;
    }

    private Map<String, Object> requireReauth(HttpSession session, String username, String rawPw) {
        if (isReauthed(session)) return null; // còn hiệu lực
        if (rawPw == null || rawPw.isBlank()) {
            return Map.of("ok", false, "message", "Vui lòng nhập mật khẩu hiện tại.");
        }
        boolean ok = userService.verifyPassword(username, rawPw); // <-- thêm bên dưới (UserService)
        if (!ok) return Map.of("ok", false, "message", "Mật khẩu hiện tại không đúng.");
        session.setAttribute(REAUTH_AT, System.currentTimeMillis());
        return null;
    }
}

