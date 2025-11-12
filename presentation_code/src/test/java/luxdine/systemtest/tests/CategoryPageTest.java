package luxdine.systemtest.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import pages.CategoryPage;
import pages.LoginPage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryPageTest {

    private static WebDriver driver;
    private static CategoryPage categoryPage;
    private static LoginPage loginPage;

    @BeforeAll
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        loginPage = new LoginPage(driver);
        categoryPage = new CategoryPage(driver);

        //  Đăng nhập admin trước khi test
        loginPage.loginAsAdmin();

        // Kiểm tra đăng nhập thành công
        Assertions.assertTrue(loginPage.isLoggedIn(), "Đăng nhập thất bại — không thể tiếp tục test!");
        System.out.println(" Đăng nhập admin thành công!");

        // Sau khi login → vào trang quản lý menu
        driver.get("http://localhost:8080/admin/menu");
    }

    @Test
    @Order(1)
    public void testAddCategoriesFromCSV() {
        categoryPage.openCategoryPopup();

        String csvPath = "src/test/resources/categories.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // bỏ dòng header
            while ((line = br.readLine()) != null) {
                String categoryName = line.trim();
                if (!categoryName.isEmpty()) {
                    System.out.println("🟢 Thêm danh mục: " + categoryName);
                    categoryPage.addCategory(categoryName);
                    Assertions.assertTrue(categoryPage.isCategoryPresent(categoryName),
                            " Không thấy danh mục '" + categoryName + "' sau khi thêm!");
                }
            }
        } catch (IOException e) {
            Assertions.fail("Lỗi khi đọc file CSV: " + e.getMessage());
        }

        System.out.println(" Hoàn tất thêm danh mục từ CSV!");
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
