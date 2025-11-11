/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.shop.main;

import com.google.gson.Gson;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import model.ProductRevenue;
import model.Users;
import service.OrdersService;
import service.ProductService;
import service.ProductViewHistoryService;
import service.ShopService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "ShopDashBoard", urlPatterns = {"/shop/dash-board"})
public class ShopDashBoard extends HttpServlet {

    private OrdersService ordersService = new OrdersService();
    private ProductService productService = new ProductService();
    private ShopService shopService = new ShopService();
    private ProductViewHistoryService productViewHistoryService = new ProductViewHistoryService();
    private OrdersService orderService = new OrdersService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null || user.getShops() == null) {
            response.sendRedirect("login"); // hoặc thông báo lỗi
            return;
        }

        int shopId = user.getShops().getShopId();

        double totalRevenue = orderService.getTotalRevenueByShop(shopId);
        request.setAttribute("totalRevenue", totalRevenue);

        int totalOrders = orderService.getTotalOrderCountByShop(shopId);
        request.setAttribute("totalOrders", totalOrders);

        double avgOrderValue = orderService.getAverageOrderValueByShop(shopId);
        request.setAttribute("avgOrderValue", avgOrderValue);

        int totalProductsSold = orderService.getTotalProductsSoldByShop(shopId);
        request.setAttribute("totalProductsSold", totalProductsSold);

        Gson gson = new Gson();
        // 2. Doanh thu theo ngày gần đây (dùng cho biểu đồ đường)
        Map<String, Double> revenueByDay7 = orderService.getRevenuePerDay(shopId, 7); // 7 ngày gần đây
        String revenueJson7 = gson.toJson(revenueByDay7);
        request.setAttribute("revenueJson7", revenueJson7);

        Map<String, Double> revenueByDay30 = orderService.getRevenuePerDay(shopId, 30);
        String revenueJson30 = gson.toJson(revenueByDay30);
        request.setAttribute("revenueJson30", revenueJson30);

        Map<String, Double> revenueByDay365 = orderService.getRevenuePerDay(shopId, 365);
        String revenueJson365 = gson.toJson(revenueByDay365);
        request.setAttribute("revenueJson365", revenueJson365);

        // 3. Doanh thu từng sản phẩm 
        List<ProductRevenue> revenueByProduct = orderService.getAllProductRevenues(shopId);
        request.setAttribute("revenueByProduct", revenueByProduct);

        request.setAttribute("todayOrders", ordersService.getTodayOrderbyShop(user.getShops().getShopId()).size());
        request.setAttribute("pendingOrders", ordersService.getProcessingOrdersbyShop(user.getShops().getShopId()).size());
        request.setAttribute("processingOrders", ordersService.getProcessingOrdersbyShop(user.getShops().getShopId()).size());
        request.setAttribute("numberOfView", productViewHistoryService.getShopNumberOfView(user.getShops().getShopId()));

        request.getRequestDispatcher("/shop/shop_dashboard.jsp").forward(request, response);
    }

}
