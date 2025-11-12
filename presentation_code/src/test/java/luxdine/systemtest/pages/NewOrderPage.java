package luxdine.systemtest.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NewOrderPage extends BasePage {

    private final By tableSelectBtn = By.id("tableSelectBtn");
    private final By tableMenu = By.id("tableMenu");
    private final By noteInput = By.id("notes");
    private final By btnCreate = By.id("btnCreate");
    private final By menuCard = By.cssSelector(".card");

    public NewOrderPage(WebDriver driver) {
        super(driver);
    }

    // 🔹 Chọn bàn trong dropdown, có kiểm tra tồn tại
    public boolean selectTable(String tableName) {
        try {
            WebElement selectBtn = wait.until(ExpectedConditions.elementToBeClickable(tableSelectBtn));
            selectBtn.click();

            // Chờ dropdown hiển thị
            WebDriverWait waitDropdown = new WebDriverWait(driver, Duration.ofSeconds(8));
            waitDropdown.until(driver -> {
                WebElement menu = driver.findElement(tableMenu);
                String hiddenAttr = menu.getAttribute("hidden");
                return hiddenAttr == null || hiddenAttr.equals("false") || menu.isDisplayed();
            });

            // Tìm bàn theo tên
            By optionLocator = By.xpath("//div[@id='tableMenu']//div[contains(text(),'" + tableName + "')]");
            var foundTables = driver.findElements(optionLocator);

            if (foundTables.isEmpty()) {
                System.out.println("⚠️ Không tìm thấy bàn '" + tableName + "' trong danh sách!");
                return false; // ❌ Không chọn được bàn
            }

            WebElement option = foundTables.get(0);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", option);
            option.click();
            return true; // ✅ Đã chọn bàn thành công

        } catch (TimeoutException e) {
            System.out.println("❌ Timeout: Không thể chọn bàn '" + tableName + "' (dropdown không hiển thị)");
            return false;
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi chọn bàn '" + tableName + "': " + e.getMessage());
            return false;
        }
    }


    // 🔹 Kiểm tra xem món ăn có trong menu không
    public boolean isMenuItemPresent(String itemName) {
        try {
            By itemLocator = By.xpath("//div[contains(@class,'card')]//*[contains(text(),'" + itemName + "')]");
            return !driver.findElements(itemLocator).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // 🔹 Thêm món ăn (nếu tồn tại)
    public void addMenuItem(String itemName) {
        try {
            By itemLocator = By.xpath("//div[contains(@class,'card')]//*[contains(text(),'" + itemName + "')]");
            WebElement item = wait.until(ExpectedConditions.elementToBeClickable(itemLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", item);
            item.click();
        } catch (TimeoutException e) {
            System.out.println("⚠️ Món '" + itemName + "' không khả dụng hoặc không tồn tại trong UI!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi thêm món '" + itemName + "': " + e.getMessage());
        }
    }

    // 🔹 Nhập ghi chú
    public void enterNotes(String notes) {
        try {
            type(noteInput, notes);
        } catch (Exception e) {
            System.out.println("⚠️ Không thể nhập ghi chú: " + e.getMessage());
        }
    }

    // 🔹 Bấm nút “Tạo” đơn hàng
    public void submitOrder() {
        try {
            WebElement btn = waitForVisibility(btnCreate);
            ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('disabled')", btn);
            btn.click();
        } catch (Exception e) {
            System.out.println("❌ Không thể nhấn nút 'Tạo': " + e.getMessage());
        }
    }

    // 🔹 Kiểm tra đã quay về danh sách đơn chưa
    public boolean isRedirectedToOrderList() {
        try {
            wait.until(ExpectedConditions.urlContains("/staff/orders"));
            return true;
        } catch (TimeoutException e) {
            System.out.println("⚠️ Không quay lại trang danh sách sau khi tạo đơn!");
            return false;
        }
    }

    // 🔹 Kiểm tra thông báo thành công (toast hoặc alert)
    private final By successToast = By.cssSelector(".toast-success, .alert-success");

    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            System.out.println("✅ Message: " + toast.getText());
            return toast.isDisplayed();
        } catch (Exception e) {
            System.out.println("⚠️ Không tìm thấy thông báo thành công!");
            return false;
        }
    }
    public boolean isFormDisplayed() {
        try {
            WebDriverWait waitShort = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement btn = waitShort.until(ExpectedConditions.visibilityOfElementLocated(btnCreate));
            return btn.isDisplayed();
        } catch (Exception e) {
            System.out.println("⚠️ Form tạo đơn không hiển thị hoặc chưa load xong!");
            return false;
        }
    }
}
