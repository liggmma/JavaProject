package luxdine.systemtest.tests;

import org.junit.jupiter.api.*;
import pages.LoginPage;
import pages.MenuPage;
import pages.UpdateMenuPage;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuUpdateTest extends BaseTest {

    private static LoginPage loginPage;
    private static MenuPage menuPage;
    private static UpdateMenuPage updatePage;

    @BeforeAll
    public static void setup() {
        BaseTest.setUpBase();
        loginPage = new LoginPage(driver);
        menuPage = new MenuPage(driver);

        // Đăng nhập admin
        loginPage.loginAsAdmin();
        Assertions.assertTrue(loginPage.isLoggedIn(), "❌ Đăng nhập thất bại!");
        System.out.println("✅ Đăng nhập thành công (Admin).");
    }

    // ======================================
    // TC_ITEM_009 – Update item's Price and Visibility
    // ======================================
    @Test
    @Order(1)
    @DisplayName("TC_ITEM_009 – Update item's Price and Visibility")
    void updateItemPriceAndVisibility() {
        menuPage.openMenuPage();
        updatePage = menuPage.openEditItemByName("Chả giò");

        updatePage.setItemPrice("40000");
        updatePage.setVisibility("PRIVATE");
        updatePage.submit();

        String message = updatePage.getToastMessage();
        System.out.println("🔹 Toast: " + message);
        Assertions.assertTrue(message.toLowerCase().contains("success") 
                || message.toLowerCase().contains("thành công"),
                "❌ Không thấy thông báo thành công khi cập nhật.");
    }

    // ======================================
    // TC_ITEM_010 – Toggle item availability
    // ======================================
    @Test
    @Order(2)
    @DisplayName("TC_ITEM_010 – Toggle item availability")
    void toggleItemAvailability() {
        menuPage.openMenuPage();
        menuPage.toggleItemAvailability("Chả giò");

        String message = menuPage.getToastMessage();
        System.out.println("🔹 Toast: " + message);
        Assertions.assertTrue(message.toLowerCase().contains("success") 
                || message.toLowerCase().contains("thành công"),
                "❌ Không thấy thông báo thành công khi đổi trạng thái hiển thị.");
    }

    // ======================================
    // TC_ITEM_011 – Cannot update with empty name
    // ======================================
    @Test
    @Order(3)
    @DisplayName("TC_ITEM_011 – Cannot update with empty name")
    void updateEmptyName() {
        menuPage.openMenuPage();
        updatePage = menuPage.openEditItemByName("Chả giò");

        updatePage.setItemName("");
        updatePage.submit();

        String message = updatePage.getToastMessage();
        System.out.println("🔹 Toast: " + message);
        Assertions.assertTrue(message.toLowerCase().contains("required")
                || message.toLowerCase().contains("trống")
                || message.toLowerCase().contains("không được bỏ trống"),
                "❌ Không có thông báo lỗi khi tên trống!");
    }

    // ======================================
    // TC_ITEM_012 – Cannot update with price = 0
    // ======================================
    @Test
    @Order(4)
    @DisplayName("TC_ITEM_012 – Cannot update with price = 0")
    void updatePriceZero() {
        menuPage.openMenuPage();
        updatePage = menuPage.openEditItemByName("Chả giò");

        updatePage.setItemPrice("0");
        updatePage.submit();

        String message = updatePage.getToastMessage();
        System.out.println("🔹 Toast: " + message);
        Assertions.assertTrue(message.toLowerCase().contains("greater")
                || message.toLowerCase().contains("phải lớn hơn")
                || message.toLowerCase().contains("invalid"),
                "❌ Không thấy thông báo lỗi khi giá = 0!");
    }

    // ======================================
    // TC_ITEM_013 – Cancel button discards changes
    // ======================================
    @Test
    @Order(5)
    @DisplayName("TC_ITEM_013 – Cancel button discards changes")
    void cancelEdit() {
        menuPage.openMenuPage();
        updatePage = menuPage.openEditItemByName("Chả giò");

        updatePage.setItemPrice("99999");
        updatePage.cancel();

        String message = updatePage.getToastMessage();
        System.out.println("🔹 Toast sau khi hủy: " + message);
        Assertions.assertTrue(message.isEmpty(),
                "❌ Hủy chỉnh sửa không hoạt động đúng!");
    }

    @AfterAll
    public static void tearDown() {
        BaseTest.tearDownBase();
    }
}
