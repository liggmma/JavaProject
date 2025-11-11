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
import java.util.List;
import model.Categories;
import model.Products;
import service.CategoryService;
import service.ProductService;

@WebServlet("/category")
public class CategoryProduct extends HttpServlet {

    private CategoryService categorySerivce;
    private ProductService productService;

    @Override
    public void init() throws ServletException {
        categorySerivce = new CategoryService();
        productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy ID từ URL
        String idRaw = request.getParameter("id");

        try {
            int categoryId = Integer.parseInt(idRaw);

            // Lấy thông tin danh mục và danh sách sản phẩm
            Categories category = categorySerivce.findById(categoryId);
            List<Products> products = productService.getProductByCategory(category);
            // Gửi dữ liệu sang JSP
            request.setAttribute("category", category);
            request.setAttribute("products", products);

            request.getRequestDispatcher("/category.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // Nếu không đúng định dạng id => chuyển sang trang 404
            response.sendRedirect("/sub/404.jsp");
        }
    }
}
