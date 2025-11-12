package luxdine.systemtest.tests;

import org.junit.jupiter.api.*;
import pages.LoginPage;
import pages.MenuPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuPageTest extends BaseTest {

    private static LoginPage loginPage;
    private static MenuPage menuPage;

    @BeforeAll
    public static void setUp() {
        BaseTest.setUpBase();
        loginPage = new LoginPage(driver);
        menuPage = new MenuPage(driver);

        // ✅ Đăng nhập admin
        loginPage.loginAsAdmin();
        Assertions.assertTrue(loginPage.isLoggedIn(), "❌ Đăng nhập thất bại!");
        System.out.println("✅ Đăng nhập admin thành công!");
    }

    @Test
    @Order(1)
    @DisplayName("System Test: Thêm món mới (có ảnh) từ file CSV")
    public void testAddMenuItemsFromCSV() throws IOException, InterruptedException {
        menuPage.openMenuPage();

        // ✅ Đảm bảo file CSV tồn tại
        Path csvPath = Paths.get("src/test/resources/test_menu.csv");
        if (!Files.exists(csvPath)) {
            Assertions.fail("❌ Không tìm thấy file CSV: " + csvPath.toAbsolutePath());
        }

        Path imagePath = Paths.get("src/test/resources/img/anh1.jpg");
        if (!Files.exists(imagePath)) {
            Assertions.fail("❌ Không tìm thấy ảnh: " + imagePath.toAbsolutePath());
        }

        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line;
            boolean first = true;

            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }

                String[] d = line.split(",");
                if (d.length < 5) continue;

                String name = d[0].trim();
                String price = d[1].trim();
                String category = d[2].trim();
                String visibility = d[3].trim();
                String description = d[4].trim();
                String allergens = d.length > 5 ? d[5].trim() : "";

                System.out.println("🟢 Thêm món: " + name);

                menuPage.clickAddMenuItem();
                Thread.sleep(400); // cho modal render ổn định
                menuPage.fillMenuForm(
                        name, price, category, visibility, description, allergens,
                        imagePath.toString()
                );
                menuPage.submitForm();

        // 🟡 Nếu danh mục không tồn tại thì bỏ qua món này
                if (!menuPage.wasLastCategoryValid(category)) {
                    System.out.println("⚠️ Bỏ qua assert cho món '" + name + "' do danh mục không hợp lệ.");
                    continue; // 👉 chuyển sang dòng kế tiếp trong CSV
                }

        // 🟢 Nếu danh mục hợp lệ, kiểm tra như bình thường
                Assertions.assertTrue(menuPage.isMenuItemDisplayed(name),
                        "❌ Không thấy món '" + name + "' sau khi thêm!");

            }
        }

        System.out.println("✅ Hoàn tất thêm món ăn từ CSV!");
    }

    @AfterAll
    public static void tearDown() {
        BaseTest.tearDownBase();
    }
}
