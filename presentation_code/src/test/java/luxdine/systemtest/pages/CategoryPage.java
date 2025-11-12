package luxdine.systemtest.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class CategoryPage extends BasePage {

    private By btnManageCategories = By.id("btn-manage-categories");
    private By btnAddNewCategory = By.id("btn-add-new-category");
    private By inputCategoryName = By.id("category-name");
    private By btnSubmitCategory = By.id("btn-submit-category-form");

    public CategoryPage(WebDriver driver) {
        super(driver);
    }

    // Mở popup quản lý danh mục
    public void openCategoryPopup() {
        click(btnManageCategories);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("category-popup")));
    }

    // Thêm danh mục mới
    public void addCategory(String name) {
        click(btnAddNewCategory);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inputCategoryName));

        type(inputCategoryName, name);
        click(btnSubmitCategory);

        // Chờ danh mục xuất hiện trong danh sách
        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("category-grid"), name));
        } catch (TimeoutException e) {
            System.out.println("⚠️ Không thấy '" + name + "' sau 3s — có thể đã tồn tại hoặc load chậm.");
        }
    }

    // Kiểm tra danh mục có tồn tại không
    public boolean isCategoryPresent(String name) {
        try {
            driver.findElement(By.xpath("//div[@id='category-grid']//*[contains(text(),'" + name + "')]"));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
