/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agentAI;

import service.ProductService;
import model.*;

public class GeminiTest {

    public static void main(String[] args) {
        // Tạo đối tượng ProductService
        ProductService productService = new ProductService();

        // Tóm tắt đầu vào từ người dùng (mô phỏng đoạn được tạo bởi Gemini)
        String summary = "tôi muốn mua đồ chơi lego xếp hình thông minh";

        // Gọi hàm xử lý và hiển thị danh sách sản phẩm tương đồng
        String formattedList = GeminiChat.getFormattedProductListFromSummary(summary, productService);

        System.out.println("Kết quả tìm kiếm sản phẩm tương đồng:\n" + formattedList);

    }
}
