package luxdine.systemtest.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

public class MenuPage extends BasePage {

    private final By btnAddMenuItem = By.id("btn-open-modal");
    private final By modalAddItem = By.id("add-item-modal");
    private final By inputItemName = By.id("item-name");
    private final By inputItemPrice = By.id("item-price");
    private final By selectItemCategory = By.id("item-category");
    private final By selectItemVisibility = By.id("item-visibility");
    private final By inputItemDescription = By.id("item-description");
    private final By inputItemAllergens = By.id("item-allergens");
    private final By fileUploadInput = By.id("item-image-file");
    private final By btnSubmitForm = By.id("btn-submit-form");

    public MenuPage(WebDriver driver) {
        super(driver);
    }
    private boolean lastCategoryValid = true;

    public boolean wasLastCategoryValid(String category) {
        return lastCategoryValid;
    }

    // 🔹 Mở trang menu
    public void openMenuPage() {
        navigateTo("http://localhost:8080/admin/menu");
        wait.until(ExpectedConditions.visibilityOfElementLocated(btnAddMenuItem));
    }

    // 🔹 Nhấn nút "Thêm món"
    public void clickAddMenuItem() {
        click(btnAddMenuItem);
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalAddItem));
        try {
            Thread.sleep(400); // chờ animation fade-in
        } catch (InterruptedException ignored) {}
    }

    // 🔹 Điền thông tin món ăn
    public void fillMenuForm(String name, String price, String category,
                             String visibility, String description,
                             String allergens, String imagePath) {

        waitForVisibility(inputItemName).clear();
        type(inputItemName, name);
        type(inputItemPrice, price);
        type(inputItemDescription, description);

        if (isElementVisible(inputItemAllergens)) {
            type(inputItemAllergens, allergens);
        }

        // ✅ Chọn danh mục
        WebElement categoryDropdown = wait.until(ExpectedConditions.elementToBeClickable(selectItemCategory));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", categoryDropdown);
        categoryDropdown.click();

// 🟡 Lấy danh sách danh mục hiện có
        Object availableCats = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('#item-category option')).map(o => o.textContent.trim());"
        );
        System.out.println("📋 Danh mục hiện có: " + availableCats);

// 🟢 Kiểm tra danh mục có tồn tại không
        boolean categoryExists = (boolean) ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('#item-category option'))" +
                        ".some(opt => opt.textContent.trim() === arguments[0]);", category
        );

        if (categoryExists) {
            lastCategoryValid = true;
            WebElement optionCat = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//select[@id='item-category']/option[normalize-space(text())='" + category + "']")
            ));
            optionCat.click();
            System.out.println("📂 Chọn danh mục: " + category);
        } else {
            lastCategoryValid = false; // cập nhật trạng thái để test biết
            System.out.println("⚠️ Danh mục '" + category + "' không tồn tại trong combo box — bỏ qua!");
        }



        // ✅ Chọn hiển thị
        try {
            WebElement visibilityDropdown = wait.until(ExpectedConditions.elementToBeClickable(selectItemVisibility));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", visibilityDropdown);
            visibilityDropdown.click();
            WebElement optionVis = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//select[@id='item-visibility']/option[contains(normalize-space(.),'" + visibility + "')]")
            ));
            optionVis.click();
        } catch (TimeoutException ignored) {}

        // ✅ Upload ảnh
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                driver.findElement(fileUploadInput).sendKeys(file.getAbsolutePath());
            }
        }
    }

    // 🔹 Submit form thêm món
    public void submitForm() {
        click(btnSubmitForm);

        // Chờ modal ẩn hoàn toàn
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(driver ->
                    ((JavascriptExecutor) driver).executeScript(
                            "let modal = document.getElementById('add-item-modal');" +
                                    "if (!modal) return true;" +
                                    "const style = window.getComputedStyle(modal);" +
                                    "return style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0';"
                    ).equals(true)
            );
        } catch (TimeoutException ignored) {}

        // Chờ danh sách món reload
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(driver1 ->
                    ((JavascriptExecutor) driver1)
                            .executeScript("return document.querySelectorAll('table tbody tr').length > 0;")
                            .equals(true)
            );
        } catch (TimeoutException ignored) {}
    }

    // 🔹 Kiểm tra món ăn đã xuất hiện trên giao diện
    public boolean isMenuItemDisplayed(String name) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//*[contains(text(),'" + name + "')]"))).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ================== 🔹 PHẦN UPDATE MÓN ĂN 🔹 ==================

    private final By cardContainer = By.cssSelector(".menu-item-card, .card");
    private final By itemNameInCard = By.cssSelector("h4, .item-name");
    private final By editButtonInCard = By.cssSelector(".btn-edit, button[class*='edit']");
    private final By toggleSwitchInCard = By.cssSelector("input[type='checkbox']");
    private final By toastMessage = By.cssSelector(".toast-message, .alert-success, .alert-danger");

    // 🔸 Mở nhóm món nếu đang đóng (accordion)
    private void expandCategoryIfCollapsed(String categoryName) {
        try {
            WebElement header = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[contains(text(),'" + categoryName + "') and contains(@class,'flex')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", header);

            WebElement section = header.findElement(By.xpath("./ancestor::section[1]"));
            boolean isCollapsed = section.findElements(cardContainer).isEmpty();

            if (isCollapsed) {
                header.click();
                Thread.sleep(600); // chờ animation
                System.out.println("📂 Đã mở danh mục: " + categoryName);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể mở danh mục: " + categoryName + " — " + e.getMessage());
        }
    }

    // 🔸 Tìm món theo tên trong danh sách
    private WebElement findItemCardByName(String name) {
        // Mở tất cả nhóm chính
        expandCategoryIfCollapsed("Món chính");
        expandCategoryIfCollapsed("Đồ uống");
        expandCategoryIfCollapsed("Tráng miệng");

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(cardContainer));
        for (WebElement card : driver.findElements(cardContainer)) {
            try {
                String title = card.findElement(itemNameInCard).getText().trim();
                if (title.equalsIgnoreCase(name)) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", card);
                    System.out.println("✅ Tìm thấy món: " + name);
                    return card;
                }
            } catch (NoSuchElementException ignored) {}
        }
        throw new NoSuchElementException("❌ Không tìm thấy món: " + name);
    }

    // 🔸 Mở modal chỉnh sửa món
    public UpdateMenuPage openEditItemByName(String name) {
        WebElement card = findItemCardByName(name);
        WebElement editBtn = card.findElement(editButtonInCard);
        wait.until(ExpectedConditions.elementToBeClickable(editBtn)).click();

        UpdateMenuPage updatePage = new UpdateMenuPage(driver);
        updatePage.waitForModalVisible();
        System.out.println("✏️ Mở modal chỉnh sửa: " + name);
        return updatePage;
    }

    // 🔸 Bật/tắt hiển thị món
    public void toggleItemAvailability(String name) {
        WebElement card = findItemCardByName(name);
        WebElement toggle = card.findElement(toggleSwitchInCard);
        wait.until(ExpectedConditions.elementToBeClickable(toggle)).click();
        System.out.println("🔁 Đã đổi trạng thái hiển thị cho món: " + name);
    }

    // 🔸 Lấy thông báo Toast (thành công / lỗi)
    public String getToastMessage() {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
            String msg = toast.getText().trim();
            System.out.println("🔹 Toast: " + msg);
            return msg;
        } catch (TimeoutException e) {
            System.out.println("⚠️ Không thấy thông báo Toast!");
            return "";
        }
    }

}
