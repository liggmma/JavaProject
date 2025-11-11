package HuuTaiDE190451.example;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class AccountService {

    private static final Set<String> registeredUsers = new HashSet<>();

    private static final String EMAIL_REGEX = "^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{7,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    private String lastErrorMessage = "";

    /**
     * Đăng ký tài khoản mới.
     * @return true nếu hợp lệ, false nếu có lỗi.
     */
    public boolean registerAccount(String username, String password, String email) {
        lastErrorMessage = "";

        if (username == null || password == null || email == null) {
            lastErrorMessage = "Thiếu thông tin bắt buộc (username, password hoặc email).";
            return false;
        }

        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            lastErrorMessage = "Không được để trống username, password hoặc email.";
            return false;
        }

        if (username.length() <= 3) {
            lastErrorMessage = "Username phải có nhiều hơn 3 ký tự.";
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            lastErrorMessage = "Password phải dài hơn 6 ký tự, chứa ít nhất 1 chữ hoa, 1 số và 1 ký tự đặc biệt.";
            return false;
        }

        if (!isValidEmail(email)) {
            lastErrorMessage = "Email không hợp lệ.";
            return false;
        }

        if (registeredUsers.contains(username)) {
            lastErrorMessage = "Username đã tồn tại.";
            return false;
        }

        registeredUsers.add(username);
        lastErrorMessage = "Đăng ký thành công!";
        return true;
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}
