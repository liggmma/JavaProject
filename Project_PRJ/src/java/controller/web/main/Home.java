/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.web.main;

import agentAI.RecommendAI;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Categories;
import model.Notifications;
import model.Products;
import model.Users;
import service.CategoryService;
import service.ProductService;
import service.ProductViewHistoryService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "home", urlPatterns = {"/home"})
public class Home extends HttpServlet {

    private ProductService productService;
    private ProductViewHistoryService productViewHistoryService;

    @Override
    public void init() {
        this.productService = new ProductService();
        this.productViewHistoryService = new ProductViewHistoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        Users user = (Users) session.getAttribute("user");

        List<Products> allProduct;
        List<Products> userViewProduct;
        List<Products> recommendProduct;

        allProduct = productService.getAll();
        if (user == null) {
            recommendProduct = getRandomProducts(allProduct, 24);
        } else {
            userViewProduct = productViewHistoryService.getUserProductView(user.getUserId());
            recommendProduct = RecommendAI.getRecommendations(userViewProduct, allProduct);
            if (recommendProduct.isEmpty()) {
                recommendProduct = getRandomProducts(allProduct, 24);
            } else if (recommendProduct.size() > 24) {
                recommendProduct = recommendProduct.subList(0, 24);
            }

        }

        request.setAttribute("products", recommendProduct);

        request.getRequestDispatcher("/home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    public List<Products> getRandomProducts(List<Products> allProduct, int limit) {
        // Bảo vệ: nếu danh sách đầu vào rỗng hoặc null, trả về danh sách rỗng
        if (allProduct == null || allProduct.isEmpty() || limit <= 0) {
            return new ArrayList<>();
        }

        // Nếu số lượng yêu cầu lớn hơn số sản phẩm có sẵn, chỉ lấy tối đa số hiện có
        int actualLimit = Math.min(limit, allProduct.size());

        // Tạo bản sao danh sách để không ảnh hưởng đến danh sách gốc
        List<Products> shuffled = new ArrayList<>(allProduct);

        // Trộn ngẫu nhiên danh sách sản phẩm
        Collections.shuffle(shuffled);

        // Lấy ra số lượng sản phẩm theo giới hạn
        List<Products> recommended = new ArrayList<>();
        for (int i = 0; i < actualLimit; i++) {
            recommended.add(shuffled.get(i));
        }

        // Trả về danh sách sản phẩm được gợi ý
        return recommended;
    }
}
