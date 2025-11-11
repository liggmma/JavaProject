import HuuTaiDE190451.example.InsecureLogin;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class InsecureLoginTest {

    @Test
    void testLoginSuccess() {
        InsecureLogin("admin", "123456");

    }

    @Test
    void testLoginFail() {
        InsecureLogin.login("user", "wrongpassword");
    }

    @Test
    void testPrintUserInfo() {
        InsecureLogin insecureLogin = new InsecureLogin();
        insecureLogin.printUserInfo("John Doe");
    }
}
