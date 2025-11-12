package luxdine.systemtest.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrderPage extends BasePage {

    private final By btnCreateOrder = By.xpath("//a[contains(text(),'+ Tạo đơn mới') or contains(text(),'Tạo đơn mới')]");
    private final By emptyMessage = By.xpath("//*[contains(text(),'Chưa có đơn nào khớp bộ lọc') or contains(text(),'đơn nào khớp bộ lọc')]");

    public OrderPage(WebDriver driver) {
        super(driver);
    }

    // 🔹 Mở trang danh sách đơn gọi món
    public void openOrderListPage() {
        navigateTo("http://localhost:8080/staff/orders");

        // Đợi đến khi phần danh sách (hoặc thông báo rỗng) hiển thị
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(emptyMessage),
                    ExpectedConditions.visibilityOfElementLocated(btnCreateOrder)
            ));
        } catch (TimeoutException e) {
            System.out.println("⚠️ Trang đơn gọi món load chậm, thử tiếp tục.");
        }
    }

    // 🔹 Nhấn vào nút “+ Tạo đơn mới”
    public void clickCreateNewOrder() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(btnCreateOrder));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
        button.click();
    }
}
