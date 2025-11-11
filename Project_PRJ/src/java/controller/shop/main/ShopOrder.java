package controller.shop.main;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Orders;
import model.Users;
import model.Shops; // Import Shops model
import service.OrdersService; // Assuming this service exists and has necessary methods
import com.google.gson.Gson; // For sending JSON responses

@WebServlet(name = "ShopOrder", urlPatterns = {"/shop/manageOrders"})
public class ShopOrder extends HttpServlet {

    private OrdersService orderService;
    private Gson gson = new Gson(); // Initialize Gson for JSON conversion

    @Override
    public void init() throws ServletException {
        orderService = new OrdersService();
    }

    // Helper class for JSON responses
    private static class ServerResponse {

        boolean success;
        String message;

        public ServerResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (loggedUser.getShops() == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Bạn không liên kết với cửa hàng nào.");
            return;
        }

        String status = request.getParameter("status");
        String keyword = request.getParameter("keyword");
        String pageParam = request.getParameter("page");

        int currentPage = 1;
        int pageSize = 10;

        if (pageParam != null) {
            try {
                currentPage = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                System.err.println("Invalid page parameter: " + pageParam);
                currentPage = 1;
            }
        }

        List<Orders> orders = orderService.searchOrdersWithPaging(loggedUser, status, keyword, currentPage, pageSize);
        int totalOrders = orderService.countOrders(loggedUser, status, keyword);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        request.setAttribute("orders", orders);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("status", status != null ? status : "");
        request.setAttribute("keyword", keyword != null ? keyword : "");

        request.getRequestDispatcher("/shop/shop_order.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");
        if (loggedUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(gson.toJson(new ServerResponse(false, "Người dùng chưa đăng nhập.")));
            return;
        }

        // Check if the logged-in user is associated with a shop
        if (loggedUser.getShops() == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(new ServerResponse(false, "Bạn không có quyền truy cập vì không liên kết với shop nào.")));
            return;
        }

        String action = request.getParameter("action");
        String orderIdParam = request.getParameter("orderId");
        int orderId;

        // Validate orderId parameter
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ServerResponse(false, "Mã đơn hàng không hợp lệ.")));
            return;
        }

        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ServerResponse(false, "Mã đơn hàng phải là số nguyên.")));
            return;
        }

        boolean success = false;
        String message = "";
        int statusCode = HttpServletResponse.SC_OK; // Default status code

        // Get the order from the database to validate current status and ownership
        Orders order = orderService.getOrderById(orderId);

        if (order == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(new ServerResponse(false, "Không tìm thấy đơn hàng.")));
            return;
        }

        // IMPORTANT: Verify that the order belongs to the logged-in shop
        // This relies on orderService.getShopByOrderId(orderId) to fetch the shop associated with the order.
        // This way, we don't need to modify the 'Orders' model directly if it doesn't already have getShop().
        Shops orderShop = orderService.getShopByOrderId(orderId);
        if (orderShop == null || !orderShop.getShopId().equals(loggedUser.getShops().getShopId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(new ServerResponse(false, "Bạn không có quyền thao tác với đơn hàng này (đơn hàng không thuộc shop của bạn).")));
            return;
        }

        String currentStatus = order.getStatus(); // Get current status for validation

        switch (action) {
            case "confirmProcessing":
                // Logic: Pending -> Processing
                if ("Pending".equals(currentStatus)) {
                    success = orderService.updateOrderStatus(orderId, "Processing");
                    message = success ? "Đã xác nhận đơn hàng, chuyển sang trạng thái đang xử lý." : "Không thể xác nhận đơn hàng.";
                } else {
                    message = "Không thể xác nhận. Đơn hàng không ở trạng thái 'Pending'.";
                    statusCode = HttpServletResponse.SC_CONFLICT; // 409 Conflict
                }
                break;
            case "confirmShipped":
                // Logic: Processing -> Shipped
                if ("Processing".equals(currentStatus)) {
                    success = orderService.updateOrderStatus(orderId, "Shipped");
                    message = success ? "Đã cập nhật trạng thái đơn hàng thành đang giao." : "Không thể cập nhật trạng thái đơn hàng.";
                } else {
                    message = "Không thể cập nhật. Đơn hàng không ở trạng thái 'Processing'.";
                    statusCode = HttpServletResponse.SC_CONFLICT;
                }
                break;
            case "confirmDelivered":
                // Logic: Shipped -> Delivered
                if ("Shipped".equals(currentStatus)) {
                    success = orderService.updateOrderStatus(orderId, "Delivered");
                    message = success ? "Đã xác nhận đơn hàng đã được giao thành công." : "Không thể xác nhận giao hàng.";
                } else {
                    message = "Không thể xác nhận. Đơn hàng không ở trạng thái 'Shipped'.";
                    statusCode = HttpServletResponse.SC_CONFLICT;
                }
                break;
            case "cancelOrder":
                // Logic: Pending or Processing -> Cancelled
                if ("Pending".equals(currentStatus) || "Processing".equals(currentStatus)) {
                    success = orderService.updateOrderStatus(orderId, "Cancelled");
                    message = success ? "Đơn hàng đã được hủy." : "Không thể hủy đơn hàng.";
                } else {
                    message = "Không thể hủy. Đơn hàng không ở trạng thái 'Pending' hoặc 'Processing'.";
                    statusCode = HttpServletResponse.SC_CONFLICT;
                }
                break;
            default:
                message = "Hành động không hợp lệ.";
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
                break; // No need to return here, will be handled by final response block
        }

        // Set the appropriate HTTP status code based on the outcome
        response.setStatus(statusCode);
        response.getWriter().write(gson.toJson(new ServerResponse(success, message)));
    }

    @Override
    public String getServletInfo() {
        return "Shop Order Management Servlet";
    }
}
