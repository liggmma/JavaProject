package luxdine.example.luxdine.service.user;

import jakarta.transaction.Transactional;
import luxdine.example.luxdine.domain.user.dto.request.ChangePasswordRequest;
import luxdine.example.luxdine.domain.user.dto.request.UserCreationRequest;
import luxdine.example.luxdine.domain.user.dto.response.UserResponse;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.mapper.UserMapper;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    private static final int MIN_LEN = 3;
    private static final int MAX_LEN = 30;
    private static final Pattern NON_ASCII_MARKS = Pattern.compile("\\p{M}+");

    UserMapper userMapper;
    UserRepository userRepository;
    final PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) return false;
        return userRepository.existsByEmailIgnoreCase(email.trim());
    }

    public String getUsernameByEmailOrThrow(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) throw new UsernameNotFoundException("Email chưa đăng ký: " + email);
        return user.getUsername();
    }

    public String normalizePhoneVN(String raw) {
        if (!StringUtils.hasText(raw)) return null;
        String digits = raw.replaceAll("[^0-9+]", "");

        // +84... -> giữ nguyên
        if (digits.startsWith("+84")) {
            String rest = digits.substring(3);
            if (rest.matches("\\d{9,10}")) return "+84" + rest;
            return null;
        }

        // 84........ -> thêm +
        if (digits.startsWith("84")) {
            String rest = digits.substring(2);
            if (rest.matches("\\d{9,10}")) return "+84" + rest;
            return null;
        }

        // 0......... -> thay 0 bằng +84
        if (digits.startsWith("0")) {
            String rest = digits.substring(1);
            if (rest.matches("\\d{9,10}")) return "+84" + rest;
            return null;
        }

        // Trường hợp nhập thẳng 9-10 số (không 0 đầu): coi là sai
        return null;
    }

    public String getUsernameByPhoneOrThrow(String raw) {
        String e164 = normalizePhoneVN(raw);
        if (e164 == null) throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        return userRepository.findByPhoneNumber(e164)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với SĐT này"))
                .getUsername();
    }

    public boolean existsByPhone(String raw) {
        String e164 = normalizePhoneVN(raw);
        if  (e164 == null) return false;
        return userRepository.existsByPhoneNumber(e164);
    }

    public boolean existsByUsername(String username) {
        if (!StringUtils.hasText(username)) return false;
        return userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    @Transactional
    public String registerVerified(UserCreationRequest req) {
        // 0) Nếu đã có theo email -> trả về username cũ
        var existing = userRepository.findByEmailIgnoreCase(req.getEmail());
        if (existing.isPresent()) {
            return existing.get().getUsername();
        }

        // 1) Chốt username
        String username = req.getUsername();
        if (!StringUtils.hasText(username)) {
            // Google hoặc case không nhập username -> sinh từ email
            username = usernameFromEmailOnly(req.getEmail());
        } else {
            // đảm bảo unique
            String base = username.trim();
            String candidate = base;
            int suffix = 0;
            while (userRepository.existsByUsernameIgnoreCase(candidate)) {
                suffix++;
                candidate = base + suffix;
            }
            username = candidate;
        }

        // 2) Lưu user
        User u = new User();
        u.setUsername(username);
        u.setEmail(req.getEmail());

        if (req.getFirstName() != null && req.getLastName() != null) {
            u.setFirstName(req.getFirstName());
            u.setLastName(req.getLastName());
        }

        if (StringUtils.hasText(req.getPassword())) {
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        u.setRole(req.getRole() == null ? Role.CUSTOMER : req.getRole());
        u.setActive(true);
        u.setEmailVerifiedAt(Instant.now());

        userRepository.save(u);
        return username;
    }

    public List<UserResponse> getAllCustomer() {
        return userRepository.findAllByRole(Role.CUSTOMER).stream().map(userMapper::toUserResponse).toList();
    }

    public String changePassword(String username, ChangePasswordRequest request) {
        try {
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return "Xác nhận mật khẩu không khớp";
            }
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return "Mật khẩu hiện tại không đúng";
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private List<String> parseAllergens(String allergensCsv) {
        if (allergensCsv == null || allergensCsv.isBlank()) return new ArrayList<>();
        return Arrays.stream(allergensCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }


    public String usernameFromEmailOnly(String email) {
        // 1) Lấy local-part
        String local = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        int at = local.indexOf('@');
        if (at >= 0) local = local.substring(0, at);

        // Nếu muốn bỏ "+tag" kiểu Gmail, mở dòng này:
        // int plus = local.indexOf('+');
        // if (plus >= 0) local = local.substring(0, plus);

        // 2) Bỏ dấu + lọc ký tự (chỉ giữ a-z, 0-9, _)
        local = stripAccents(local);
        local = local.replaceAll("[^a-z0-9_]", "_");
        local = local.replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");

        // 3) Ràng buộc định dạng
        if (local.isBlank()) local = "user";
        if (Character.isDigit(local.charAt(0))) local = "u_" + local;
        if (local.length() > MAX_LEN) local = local.substring(0, MAX_LEN);
        if (local.length() < MIN_LEN) local = padRight(local, MIN_LEN, 'x');

        // 4) Đảm bảo unique bằng hậu tố số
        String base = local;
        String candidate = base;
        int suffix = 0;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            suffix++;
            int maxBase = MAX_LEN - String.valueOf(suffix).length();
            String trimmed = base.length() > maxBase ? base.substring(0, maxBase) : base;
            candidate = trimmed + suffix;
        }
        return candidate;
    }

    @Transactional
    public String updateFullName(String username, String fullName) {
        User u = findByUsername(username);

        String norm = normalizeSpaces(fullName);
        if (!StringUtils.hasText(norm)) {
            throw new IllegalArgumentException("Họ và tên không được để trống.");
        }

        NameParts parts = splitVietnameseFullName(norm);

        boolean changed = false;
        if (!parts.firstName().equals(u.getFirstName())) { u.setFirstName(parts.firstName()); changed = true; }
        if (!parts.lastName().equals(u.getLastName()))   { u.setLastName(parts.lastName());   changed = true; }

        if (changed) userRepository.save(u);

        // FE hiển thị "LastName FirstName" (đã dùng ở controller)
        return (parts.lastName().isBlank() ? parts.firstName() : parts.lastName() + " " + parts.firstName());
    }

    @Transactional
    public String updateEmail(String username, String email) {
        User u = findByUsername(username);

        String em = (email == null) ? null : email.trim().toLowerCase(Locale.ROOT);
        if (em == null || !EMAIL_RE.matcher(em).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }

        String current = Objects.toString(u.getEmail(), "");
        if (!em.equalsIgnoreCase(current)) {
            if (userRepository.existsByEmailIgnoreCase(em)) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
            }
            u.setEmail(em);
            userRepository.save(u);
        }
        return em;
    }

    @Transactional
    public String updatePhone(String username, String phoneNumber) {
        User u = findByUsername(username);

        // Dùng đúng normalizePhoneVN() đã có trong class (trả về dạng +84xxxxxxxxx)
        String e164 = normalizePhoneVN(phoneNumber);
        if (e164 == null) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }

        String current = Objects.toString(u.getPhoneNumber(), "");
        if (!e164.equals(current)) {
            if (userRepository.existsByPhoneNumber(e164)) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác.");
            }
            u.setPhoneNumber(e164);
            userRepository.save(u);
        }
        return e164;
    }

    @Transactional
    public String updateAllergensCsv(String username, String allergensCsv) {
        User u = findByUsername(username);

        // Dùng đúng parseAllergens() bạn đã có (trim, distinct)
        List<String> parsed = parseAllergens(allergensCsv);

        if (!Objects.equals(u.getAllergens(), parsed)) {
            u.setAllergens(parsed);
            userRepository.save(u);
        }
        // FE muốn hiển thị dạng "a, b, c"
        return parsed.isEmpty() ? "" : String.join(", ", parsed);
    }

    public boolean verifyPassword(String username, String rawPassword){
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    private static String stripAccents(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return NON_ASCII_MARKS.matcher(n).replaceAll("");
    }
    private static String padRight(String s, int len, char ch) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(ch);
        return sb.toString();
    }


    // Chuẩn hoá khoảng trắng một lần cho gọn
    private String normalizeSpaces(String s) {
        if (s == null) return null;
        String t = s.trim().replaceAll("\\s+", " ");
        return t.isEmpty() ? null : t;
    }

    // Cắt tên kiểu VN: firstName = token cuối (tên gọi), lastName = phần trước (họ + đệm)
    private NameParts splitVietnameseFullName(String fullName) {
        String fn = normalizeSpaces(fullName);
        if (!StringUtils.hasText(fn)) throw new IllegalArgumentException("Họ và tên không hợp lệ.");

        int idx = fn.lastIndexOf(' ');
        if (idx < 0) {
            // Không có khoảng trắng → tất cả là firstName
            return new NameParts(fn, "");
        }
        String last  = fn.substring(0, idx).trim();
        String first = fn.substring(idx + 1).trim();
        if (!StringUtils.hasText(first)) throw new IllegalArgumentException("Tên riêng không hợp lệ.");
        return new NameParts(first, last);
    }

    // Regex email đơn giản, không quá “gắt”
    private static final Pattern EMAIL_RE = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    // Để tiện return cặp (first,last)
    private record NameParts(String firstName, String lastName) {}
}
