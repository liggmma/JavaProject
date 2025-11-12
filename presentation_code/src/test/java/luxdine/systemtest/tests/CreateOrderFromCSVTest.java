package luxdine.systemtest.tests;

import org.junit.jupiter.api.*;
import pages.LoginPage;
import pages.NewOrderPage;
import pages.OrderPage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateOrderFromCSVTest extends BaseTest {

    private static LoginPage loginPage;
    private static OrderPage orderPage;
    private static NewOrderPage newOrderPage;

    private static int totalOrders = 0;
    private static int successCount = 0;
    private static int skippedCount = 0;
    private static int failedCount = 0;

    @BeforeAll
    public static void setup() {
        BaseTest.setUpBase();
        loginPage = new LoginPage(driver);
        orderPage = new OrderPage(driver);
        newOrderPage = new NewOrderPage(driver);

        loginPage.loginAsStaff();
        Assertions.assertTrue(loginPage.isLoggedIn(), "❌ Đăng nhập Staff thất bại!");
        System.out.println("✅ Đăng nhập Staff thành công - bắt đầu System Test CSV!");
    }

    @Test
    @Order(1)
    @DisplayName("System Test: Tạo đơn hàng từ CSV (bỏ qua đơn lỗi, có assert)")
    public void testCreateOrdersFromCSV() throws IOException {
        String csvPath = "src/test/resources/orders.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean first = true;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (first) {
                    first = false;
                    continue; // bỏ qua header
                }

                String[] data = line.split(",");
                if (data.length < 3) {
                    System.out.println("⚠️ Bỏ qua dòng " + lineNumber + ": dữ liệu không hợp lệ!");
                    skippedCount++;
                    continue;
                }

                String tableName = data[0].trim();
                String[] items = data[1].trim().split("\\|");
                String notes = data[2].trim();

                totalOrders++;
                System.out.println("\n🧾 ===== Dòng " + lineNumber + ": Đang xử lý đơn của " + tableName + " =====");

                try {
                    orderPage.openOrderListPage();
                    orderPage.clickCreateNewOrder();

                    Assertions.assertTrue(newOrderPage.isFormDisplayed(),
                            "❌ Form tạo đơn không hiển thị!");


                    boolean tableSelected = newOrderPage.selectTable(tableName);
                    if (!tableSelected) {
                        System.out.println("⚠️ Không chọn được bàn '" + tableName + "', bỏ qua!");
                        skippedCount++;
                        continue;
                    }

                    boolean allItemsExist = true;
                    for (String item : items) {
                        String trimmed = item.trim();
                        if (newOrderPage.isMenuItemPresent(trimmed)) {
                            newOrderPage.addMenuItem(trimmed);
                        } else {
                            System.out.println("⚠️ Món '" + trimmed + "' không tồn tại!");
                            allItemsExist = false;
                        }
                    }

                    if (!allItemsExist) {
                        failedCount++;
                        continue;
                    }

                    newOrderPage.enterNotes(notes);
                    newOrderPage.submitOrder();

                    boolean redirected = newOrderPage.isRedirectedToOrderList();
                    if (!redirected) {
                        System.out.println("⚠️ Không quay lại danh sách sau khi tạo đơn '" + tableName + "'");
                        failedCount++;
                        continue;
                    }
                    boolean successMsg = newOrderPage.isSuccessMessageDisplayed();
                    if (successMsg) {
                        successCount++;
                        System.out.println("✅ Đơn của " + tableName + " tạo thành công!");
                    } else {
                        failedCount++;
                        System.out.println("⚠️ Không hiển thị thông báo thành công!");
                    }

                } catch (AssertionError ae) {
                    System.out.println("❌ Lỗi xác minh ở dòng " + lineNumber + ": " + ae.getMessage());
                    failedCount++;
                    continue;
                } catch (Exception e) {
                    System.out.println("❌ Lỗi xử lý đơn '" + tableName + "': " + e.getMessage());
                    failedCount++;
                    continue;
                }
            }
        }

        System.out.println("\n📊 ===== KẾT QUẢ TỔNG =====");
        System.out.println("📘 Tổng số dòng xử lý: " + totalOrders);
        System.out.println("🟢 Thành công: " + successCount);
        System.out.println("🔴 Thất bại: " + failedCount);
        System.out.println("🟡 Bỏ qua: " + skippedCount);


        Assertions.assertTrue(totalOrders > 0, "Không có dòng nào trong CSV!");
        Assertions.assertTrue(successCount > 0, " Không có đơn nào được tạo thành công!");
    }

    @AfterAll
    public static void tearDown() {
        BaseTest.tearDownBase();
        System.out.println("\n🧩 Hoàn tất System Test CSV ✅");
    }
}
