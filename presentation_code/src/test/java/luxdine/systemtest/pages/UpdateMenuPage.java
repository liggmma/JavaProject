package luxdine.systemtest.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * UpdateMenuPage — Trang xử lý chỉnh sửa món ăn (LuxDine)
 * Hỗ trợ các thao tác: nhập tên, giá, hiển thị, lưu, hủy, lấy toast message
 */
public class UpdateMenuPage extends BasePage {

    private final WebDriverWait wait;

    // ====== Locators trong modal edit ======
    private final By modalEditItem = By.cssSelector("#edit-item-modal, #add-item-modal"); // fallback 2 ID
    private final By inputItemName = By.id("item-name");
    private final By inputItemPrice = By.id("item-price");
    private final By selectItemVisibility = By.id("item-visibility");
    private final By btnSubmitForm = By.id("btn-submit-form");
    private final By btnCancelForm = By.id("btn-cancel-form");
    private final By toastMessage = By.cssSelector(".toast-message, .alert-success, .alert-danger, .toast, .notification");

    public UpdateMenuPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // 🔹 Chờ modal hiển thị hoàn toàn
    public void waitForModalVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalEditItem));
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> ((JavascriptExecutor) d)
                            .executeScript("""
                                const modal = document.querySelector('#edit-item-modal') || document.querySelector('#add-item-modal');
                                return modal && getComputedStyle(modal).opacity === '1' && getComputedStyle(modal).visibility !== 'hidden';
                            """)
                            .equals(true));
            System.out.println("✅ Modal edit hiển thị hoàn toàn.");
        } catch (TimeoutException e) {
            System.err.println("⚠️ Modal edit không hiển thị trong thời gian chờ!");
        }
    }

    // 🔹 Kiểm tra modal đang mở
    public boolean isModalVisible() {
        try {
            WebElement modal = driver.findElement(modalEditItem);
            return modal.isDisplayed() && modal.getCssValue("opacity").equals("1");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // 🔹 Nhập tên món
    public void setItemName(String name) {
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputItemName));
            input.clear();
            input.sendKeys(name);
            System.out.println("📝 Nhập tên: " + name);
        } catch (Exception e) {
            System.err.println("❌ Lỗi nhập tên: " + e.getMessage());
        }
    }

    // 🔹 Nhập giá món
    public void setItemPrice(String price) {
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputItemPrice));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", input);
            input.clear();
            input.sendKeys(price);
            System.out.println("💰 Nhập giá: " + price);
        } catch (Exception e) {
            System.err.println("❌ Lỗi nhập giá: " + e.getMessage());
        }
    }

    // 🔹 Chọn hiển thị (PUBLIC / PRIVATE / COMBO_ONLY)
    public void setVisibility(String visibility) {
        try {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(selectItemVisibility));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", dropdown);
            dropdown.click();

            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//select[@id='item-visibility']/option[contains(translate(text(),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ'),'" +
                            visibility.toUpperCase() + "')]")));
            option.click();
            System.out.println("👁️ Chọn hiển thị: " + visibility);
        } catch (Exception e) {
            System.err.println("❌ Lỗi chọn hiển thị: " + e.getMessage());
        }
    }

    // 🔹 Lưu thay đổi
    public void submit() {
        try {
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(btnSubmitForm));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", submitBtn);
            submitBtn.click();

            // Chờ modal đóng hoàn toàn
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(d ->
                    ((JavascriptExecutor) d).executeScript("""
                        const modal = document.querySelector('#edit-item-modal') || document.querySelector('#add-item-modal');
                        return !modal || getComputedStyle(modal).opacity === '0' || getComputedStyle(modal).display === 'none';
                    """).equals(true));
            System.out.println("✅ Đã lưu thay đổi & đóng modal.");
        } catch (Exception e) {
            System.err.println("⚠️ Không thể submit form: " + e.getMessage());
        }
    }

    // 🔹 Hủy chỉnh sửa
    public void cancel() {
        try {
            WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(btnCancelForm));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", cancelBtn);
            cancelBtn.click();

            // Chờ modal đóng
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.invisibilityOfElementLocated(modalEditItem));
            System.out.println("↩️ Đã hủy chỉnh sửa.");
        } catch (Exception e) {
            System.err.println("⚠️ Không thể hủy form: " + e.getMessage());
        }
    }

    // 🔹 Lấy thông báo Toast (thành công / lỗi)
    public String getToastMessage() {
        try {
            WebElement toast = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
            String message = toast.getText().trim();
            System.out.println("🔹 Toast: " + message);
            return message;
        } catch (TimeoutException e) {
            System.out.println("⚠️ Không thấy thông báo Toast!");
            return "";
        }
    }
}
