/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.web.main;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import model.ProductViewHistory;
import model.Products;
import model.Users;
import service.CategoryService;
import service.ProductService;
import service.ProductViewHistoryService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "product", urlPatterns = {"/product"})
public class ProductDetail extends HttpServlet {

    private CategoryService categoryService;
    private ProductService productService;
    private ProductViewHistoryService productViewHistoryService;

    @Override
    public void init() {
        this.categoryService = new CategoryService();
        this.productService = new ProductService();
        this.productViewHistoryService = new ProductViewHistoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy productId từ query string
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.sendRedirect("home");
            return;
        }

        HttpSession session = request.getSession();

        Users user = (Users) session.getAttribute("user");

        if (user != null) {
            ProductViewHistory pvh = new ProductViewHistory(new Date(), productService.findById(Integer.parseInt(idParam)), user);
            productViewHistoryService.add(pvh);
        }

        try {
            int productId = Integer.parseInt(idParam);
            Products product = productService.findById(productId);

            request.setAttribute("product", product);
            request.getRequestDispatcher("product_detail.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect("home");
        }
    }

}
