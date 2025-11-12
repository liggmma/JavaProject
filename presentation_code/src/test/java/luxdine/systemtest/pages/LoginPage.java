package luxdine.systemtest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector("button.btn--primary[type='submit']");
    private final By alertError = By.cssSelector(".alert--error");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // 🔹 Mở trang login
    public void openLoginPage() {
        navigateTo("http://localhost:8080/login");
    }

    // 🔹 Đăng nhập với user + password
    public void loginAs(String username, String password) {
        openLoginPage();
        type(usernameInput, username);
        type(passwordInput, password);
        click(loginButton);
    }

    // 🔹 Đăng nhập với quyền admin (dành cho trang quản lý)
    public void loginAsAdmin() {
        loginAs("admin", "admin");
    }

    // 🔹 Đăng nhập với quyền staff (dành cho trang gọi món)
    public void loginAsStaff() {
        loginAs("staff", "staff");
    }

    //  Kiểm tra login thất bại
    public boolean isLoginFailed() {
        return isElementVisible(alertError);
    }

    // Kiểm tra đăng nhập thành công
    public boolean isLoggedIn() {
        String url = driver.getCurrentUrl();
        return url.contains("/admin") || url.contains("/staff") || url.contains("/home");
    }
}
