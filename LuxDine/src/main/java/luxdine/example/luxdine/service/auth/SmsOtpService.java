package luxdine.example.luxdine.service.auth;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static luxdine.example.luxdine.common.util.OtpSessionKeys.*;

@Service
@RequiredArgsConstructor
public class SmsOtpService {

    private final SmsGateway smsGateway;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    private static final int    OTP_DIGITS               = 6;
    private static final int    RESEND_COOLDOWN_SECONDS  = 60;
    private static final long   OTP_TTL_MINUTES          = 5;
    private static final int    MAX_VERIFY_ATTEMPTS      = 5;
    private static final int    BRUTE_FORCE_LOCK_SECONDS = 300;

    public Map<String, Object> sendOtp(String phoneE164, HttpSession session) {
        Instant lockUntil = (Instant) session.getAttribute(SMS_LOCK_UNTIL);
        if (lockUntil != null && Instant.now().isBefore(lockUntil)) {
            long retry = Math.max(1, Duration.between(Instant.now(), lockUntil).toSeconds());
            return Map.of("ok", false, "message", "Vui lòng thử lại sau " + retry + "s.", "retryAfter", retry);
        }

        String code = String.format("%0" + OTP_DIGITS + "d", random.nextInt((int) Math.pow(10, OTP_DIGITS)));
        session.setAttribute(SMS_HASH,    encoder.encode(code));
        session.setAttribute(SMS_EXPIRES, Instant.now().plus(Duration.ofMinutes(OTP_TTL_MINUTES)));
        session.setAttribute(SMS_PHONE,   phoneE164);
        session.setAttribute(SMS_ATTEMPTS, 0);
        session.setAttribute(SMS_LOCK_UNTIL, Instant.now().plusSeconds(RESEND_COOLDOWN_SECONDS));

        try {
            smsGateway.send(phoneE164, "[LuxDine] Ma OTP dang nhap: " + code + " (het han sau " + OTP_TTL_MINUTES + " phut)");
            return Map.of("ok", true, "retryAfter", RESEND_COOLDOWN_SECONDS);
        } catch (Exception e) {
            clearSmsSession(session);
            return Map.of("ok", false, "message", "Không gửi được SMS. Thử lại sau.");
        }
    }

    public Map<String, Object> verifyOtp(String phoneE164, String code, HttpSession session) {
        String sPhone  = (String)  session.getAttribute(SMS_PHONE);
        String hash    = (String)  session.getAttribute(SMS_HASH);
        Instant exp    = (Instant) session.getAttribute(SMS_EXPIRES);
        Integer tries  = (Integer) session.getAttribute(SMS_ATTEMPTS);
//        Instant lockUntil = (Instant) session.getAttribute(SMS_LOCK_UNTIL);
//        if (lockUntil != null && Instant.now().isBefore(lockUntil)) {
//            long sec = Duration.between(Instant.now(), lockUntil).toSeconds();
//            return Map.of("ok", false, "message", "Bạn thử sai quá nhiều lần. Thử lại sau " + Math.max(1, sec) + "s.");
//        }

        if (sPhone == null || hash == null || exp == null) {
            return Map.of("ok", false, "message", "Chưa yêu cầu mã hoặc mã đã hết hạn.");
        }
        if (!sPhone.equals(phoneE164)) {
            return Map.of("ok", false, "message", "SĐT không khớp với yêu cầu ban đầu.");
        }
        if (Instant.now().isAfter(exp)) {
            clearSmsSession(session);
            return Map.of("ok", false, "message", "Mã OTP đã hết hạn.");
        }
        int attempted = (tries == null) ? 0 : tries;
        if (attempted >= MAX_VERIFY_ATTEMPTS) {
            session.setAttribute(SMS_LOCK_UNTIL, Instant.now().plusSeconds(BRUTE_FORCE_LOCK_SECONDS));
            return Map.of("ok", false, "message", "Thử sai quá nhiều. Vui lòng thử lại sau ít phút.");
        }

        boolean ok = encoder.matches(code, hash);
        if (!ok) {
            session.setAttribute(SMS_ATTEMPTS, attempted + 1);
            return Map.of("ok", false, "message", "Mã OTP không đúng.");
        }

        clearSmsSession(session); // Single-use
        return Map.of("ok", true);
    }

    public void clearSmsSession(HttpSession session) {
        session.removeAttribute(SMS_HASH);
        session.removeAttribute(SMS_EXPIRES);
        session.removeAttribute(SMS_PHONE);
        session.removeAttribute(SMS_ATTEMPTS);
        session.removeAttribute(SMS_LOCK_UNTIL);
    }
}