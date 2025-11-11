package luxdine.example.luxdine.service.auth;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static luxdine.example.luxdine.common.util.OtpSessionKeys.*;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final JavaMailSender mailSender;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    // ====== Cấu hình OTP ======
    private static final int    OTP_DIGITS                  = 6;
    private static final int    RESEND_COOLDOWN_SECONDS     = 60;    // chờ 60s mới cho gửi lại
    private static final long   OTP_TTL_MINUTES             = 5;     // OTP sống 5 phút
    private static final int    MAX_VERIFY_ATTEMPTS         = 5;     // tối đa 5 lần thử
    private static final int    BRUTE_FORCE_LOCK_SECONDS    = 300;   // khóa 5 phút khi vượt quá

    /**
     * Gửi OTP (HTML email) tới email và lưu dấu vết vào session.
     * Lưu ý: luồng gọi nên kiểm tra username/email trùng trước khi gọi hàm này.
     */
    public Map<String, Object> sendOtp(String email, HttpSession session) {
        Instant lockUntil = (Instant) session.getAttribute(LOCK_UNTIL);
        if (lockUntil != null && Instant.now().isBefore(lockUntil)) {
            long retry = Math.max(1, Duration.between(Instant.now(), lockUntil).toSeconds());
            return Map.of(
                    "ok", false,
                    "message", "Vui lòng thử lại sau " + retry + "s.",
                    "retryAfter", retry
            );
        }

        String code = String.format("%0" + OTP_DIGITS + "d", random.nextInt((int) Math.pow(10, OTP_DIGITS)));
        session.setAttribute(HASH,    encoder.encode(code));
        session.setAttribute(EXPIRES, Instant.now().plus(Duration.ofMinutes(OTP_TTL_MINUTES)));
        session.setAttribute(EMAIL,   email);
        session.setAttribute(ATTEMPTS, 0);
        session.setAttribute(LOCK_UNTIL, Instant.now().plusSeconds(RESEND_COOLDOWN_SECONDS));
        session.removeAttribute(VERIFIED);
        session.removeAttribute(VERIFIED_EMAIL);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setTo(email);
            helper.setSubject("[LuxDine] Mã xác minh email của bạn");
            helper.setText(buildOtpHtml(code), true);
            mailSender.send(mime);
            // <-- Cho FE biết phải chờ bao lâu mới được bấm lại
            return Map.of("ok", true, "retryAfter", RESEND_COOLDOWN_SECONDS);
        } catch (Exception e) {
            clearSession(session);
            return Map.of("ok", false, "message", "Không gửi được email. Vui lòng thử lại.");
        }
    }

    /**
     * Xác minh OTP từ email. Thành công → đánh dấu VERIFIED & VERIFIED_EMAIL, xoá hash/ttl/attempts.
     */
    public Map<String, Object> verifyOtp(String email, String code, HttpSession session) {
        // Chặn khi đang bị lock do brute-force
//        Instant lockUntil = (Instant) session.getAttribute(LOCK_UNTIL);
//        if (lockUntil != null && Instant.now().isBefore(lockUntil)) {
//            long sec = Duration.between(Instant.now(), lockUntil).toSeconds();
//            return Map.of("ok", false, "message", "Bạn thử sai quá nhiều lần. Vui lòng thử lại sau " + Math.max(1, sec) + "s.");
//        }

        String expectedEmail = (String)  session.getAttribute(EMAIL);
        String hash          = (String)  session.getAttribute(HASH);
        Instant expires      = (Instant) session.getAttribute(EXPIRES);
        Integer attempts     = (Integer) session.getAttribute(ATTEMPTS);

        if (expectedEmail == null || hash == null || expires == null) {
            return Map.of("ok", false, "message", "Chưa yêu cầu mã hoặc mã đã hết hạn.");
        }
        if (!expectedEmail.equalsIgnoreCase(email)) {
            return Map.of("ok", false, "message", "Email không khớp với yêu cầu ban đầu.");
        }
        if (Instant.now().isAfter(expires)) {
            clearSession(session);
            return Map.of("ok", false, "message", "Mã OTP đã hết hạn.");
        }
        int tried = attempts == null ? 0 : attempts;
        if (tried >= MAX_VERIFY_ATTEMPTS) {
            // Khoá tạm để chống brute-force
            session.setAttribute(LOCK_UNTIL, Instant.now().plusSeconds(BRUTE_FORCE_LOCK_SECONDS));
            return Map.of("ok", false, "message", "Thử sai quá nhiều. Vui lòng thử lại sau ít phút.");
        }

        boolean ok = encoder.matches(code, hash);
        if (!ok) {
            session.setAttribute(ATTEMPTS, tried + 1);
            return Map.of("ok", false, "message", "Mã OTP không đúng.");
        }

        // Thành công: đánh dấu verified (single-use), ghi email đã xác minh, dọn dấu vết OTP
        session.setAttribute(VERIFIED, true);
        session.setAttribute(VERIFIED_EMAIL, email);
        session.removeAttribute(HASH);
        session.removeAttribute(EXPIRES);
        session.removeAttribute(EMAIL);
        session.removeAttribute(ATTEMPTS);
        // Giữ LOCK_UNTIL như cũ (không cần thay đổi)
        return Map.of("ok", true);
    }

    public Map<String, Object> sendOtpToNewEmail(String newEmail, HttpSession session) {
        Instant lockUntil = (Instant) session.getAttribute(EC_LOCK_UNTIL);
        if (lockUntil != null && Instant.now().isBefore(lockUntil)) {
            long retry = Math.max(1, Duration.between(Instant.now(), lockUntil).toSeconds());
            return Map.of("ok", false, "message", "Vui lòng thử lại sau " + retry + "s.", "retryAfter", retry);
        }

        String code = String.format("%0" + OTP_DIGITS + "d", random.nextInt((int) Math.pow(10, OTP_DIGITS)));
        session.setAttribute(EC_HASH,     encoder.encode(code));
        session.setAttribute(EC_EXPIRES,  Instant.now().plus(Duration.ofMinutes(OTP_TTL_MINUTES)));
        session.setAttribute(EC_EMAIL,    newEmail);
        session.setAttribute(EC_ATTEMPTS, 0);
        session.setAttribute(EC_LOCK_UNTIL, Instant.now().plusSeconds(RESEND_COOLDOWN_SECONDS));

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setTo(newEmail);
            helper.setSubject("[LuxDine] Xác nhận đổi địa chỉ email");
            helper.setText(buildHtml(code), true);
            mailSender.send(mime);
            return Map.of("ok", true, "retryAfter", RESEND_COOLDOWN_SECONDS);
        } catch (Exception e) {
            clear(session);
            return Map.of("ok", false, "message", "Không gửi được email xác nhận. Thử lại sau.");
        }
    }

    public Map<String, Object> verifyOtpForEmailChange(String code, HttpSession session) {
        String   email    = (String)  session.getAttribute(EC_EMAIL);
        String   hash     = (String)  session.getAttribute(EC_HASH);
        Instant  exp      = (Instant) session.getAttribute(EC_EXPIRES);
        Integer  attempts = (Integer) session.getAttribute(EC_ATTEMPTS);

        if (email == null || hash == null || exp == null) {
            return Map.of("ok", false, "message", "Chưa yêu cầu mã hoặc mã đã hết hạn.");
        }
        if (Instant.now().isAfter(exp)) {
            clear(session);
            return Map.of("ok", false, "message", "Mã OTP đã hết hạn.");
        }

        int tried = attempts == null ? 0 : attempts;
        if (tried >= MAX_VERIFY_ATTEMPTS) {
            session.setAttribute(EC_LOCK_UNTIL, Instant.now().plusSeconds(BRUTE_FORCE_LOCK_SECONDS));
            return Map.of("ok", false, "message", "Thử sai quá nhiều. Vui lòng thử lại sau ít phút.");
        }

        boolean ok = encoder.matches(code, hash);
        if (!ok) {
            session.setAttribute(EC_ATTEMPTS, tried + 1);
            return Map.of("ok", false, "message", "Mã OTP không đúng.");
        }

        // Thành công -> không tự cập nhật email tại đây; controller sẽ gọi UserService.updateEmail()
        return Map.of("ok", true, "email", email);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(EC_HASH);
        session.removeAttribute(EC_EXPIRES);
        session.removeAttribute(EC_EMAIL);
        session.removeAttribute(EC_ATTEMPTS);
        session.removeAttribute(EC_LOCK_UNTIL);
    }

    private String buildHtml(String code) {
        return """
        <!doctype html><html><body style="font-family:Inter,Arial">
          <h2>Đổi địa chỉ email LuxDine</h2>
          <p>Nhập mã OTP để xác nhận đổi email:</p>
          <div style="display:inline-block;letter-spacing:8px;font-weight:700;font-size:24px;padding:8px 12px;border:1px dashed #e5e7eb;background:#f9fafb">%s</div>
          <p style="color:#6b7280">Mã có hiệu lực trong vài phút. Nếu bạn không yêu cầu, có thể bỏ qua email này.</p>
        </body></html>
        """.formatted(code);
    }

    /** Xoá toàn bộ dấu vết OTP/verify trong session. */
    public void clearSession(HttpSession session) {
        session.removeAttribute(HASH);
        session.removeAttribute(EXPIRES);
        session.removeAttribute(EMAIL);
        session.removeAttribute(ATTEMPTS);
        session.removeAttribute(LOCK_UNTIL);
        session.removeAttribute(VERIFIED);
        session.removeAttribute(VERIFIED_EMAIL);
        session.removeAttribute(PENDING_REG);
    }

    // ====== HTML template (inline CSS + table layout, thân thiện email client) ======
    private String buildOtpHtml(String code) {
        return """
           <!doctype html>
           <html>
           <head>
             <meta name="viewport" content="width=device-width,initial-scale=1"/>
             <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
             <title>LuxDine Email Verification</title>
             <style>
               body { margin:0; padding:0; background:#f6f7fb; }
               table { border-collapse:collapse; }
               a { text-decoration:none; }
               @media only screen and (max-width:600px){
                 .container{ width:100%% !important; }
                 .content{ padding:24px !important; }
               }
             </style>
           </head>
           <body style="background:#f6f7fb; font-family: Inter,Segoe UI,Arial,sans-serif; color:#111827;">
             <span style="display:none !important; visibility:hidden; opacity:0; height:0; width:0; overflow:hidden;">
               Mã OTP của bạn: %s — mã sẽ hết hạn sau vài phút.
             </span>

             <table role="presentation" width="100%%" bgcolor="#f6f7fb">
               <tr>
                 <td align="center" style="padding:24px;">
                   <table role="presentation" width="600" class="container" style="width:600px; max-width:100%%; background:#ffffff; border-radius:12px; border:1px solid #e5e7eb;">
                     <tr>
                       <td class="content" style="padding:32px;">
                         <table role="presentation" width="100%%">
                           <tr>
                             <td align="left" style="font-weight:700; color:#0a0a23; font-size:18px;">LuxDine</td>
                             <td align="right" style="font-size:12px; color:#6b7280;">Xác minh email</td>
                           </tr>
                         </table>

                         <h1 style="margin:16px 0 8px; font-size:20px; color:#0a0a23;">Xác minh địa chỉ email của bạn</h1>
                         <p style="margin:0 0 16px; color:#374151;">
                           Sử dụng mã dưới đây để hoàn tất đăng ký tài khoản LuxDine. Mã chỉ có hiệu lực trong thời gian ngắn.
                         </p>

                         <div style="text-align:center; margin:24px 0;">
                           <div style="display:inline-block; letter-spacing:8px; font-weight:700; font-size:28px; color:#0a0a23; border:1px dashed #e5e7eb; padding:12px 16px; border-radius:8px; background:#f9fafb;">
                             %s
                           </div>
                         </div>

                         <table role="presentation" align="center" style="margin:8px auto 24px;">
                           <tr>
                             <td align="center">
                               <!--[if mso]>
                               <v:roundrect xmlns:v="urn:schemas-microsoft-com:vml" href="#" arcsize="12%%" stroke="f" fillcolor="#0a0a23" style="height:44px;v-text-anchor:middle;width:240px;">
                                 <w:anchorlock/>
                                 <center style="color:#ffffff;font-family:Arial,sans-serif;font-size:14px;font-weight:bold;">Mở trang xác thực</center>
                               </v:roundrect>
                               <![endif]-->
                               <!--[if !mso]><!-- -->
                               <a href="#" style="background:#0a0a23; color:#ffffff; display:inline-block; padding:12px 20px; border-radius:8px; font-weight:600;">
                                 Mở trang xác thực
                               </a>
                               <!--<![endif]-->
                             </td>
                           </tr>
                         </table>

                         <p style="margin:0; color:#6b7280; font-size:12px;">Nếu bạn không yêu cầu mã này, hãy bỏ qua email.</p>
                         <hr style="border:none; border-top:1px solid #e5e7eb; margin:24px 0;" />
                         <p style="margin:0; color:#9ca3af; font-size:12px;">© LuxDine. Email tự động, vui lòng không trả lời.</p>
                       </td>
                     </tr>
                   </table>
                   <div style="height:24px"></div>
                 </td>
               </tr>
             </table>
           </body>
           </html>
           """.formatted(code, code);
    }

}
