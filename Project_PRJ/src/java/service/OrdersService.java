package service;

import dao.OrderDAO;
import java.util.List;
import java.util.Map;
import model.Orders;
import model.ProductRevenue;
import model.Users;
import model.Shops; // Import Shops model

/**
 *
 * @author ADMIN
 */
public class OrdersService {

    OrderDAO orderDAO = new OrderDAO();

    public List<Orders> getAll() {
        return orderDAO.getAll();
    }

    public Orders findById(int id) {
        return orderDAO.findById(id);
    }

    // New function: getOrderById (renamed from findById to avoid confusion if needed)
    public Orders getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }

    // New function: getShop by Order ID
    public Shops getShopByOrderId(int orderId) {
        return orderDAO.getShopByOrderId(orderId);
    }

    // New function: updateOrderStatus
    public boolean updateOrderStatus(int orderId, String newStatus) {
        return orderDAO.updateOrderStatus(orderId, newStatus);
    }

    public void add(Orders o) {
        orderDAO.add(o);
    }

    public void update(Orders o) {
        orderDAO.update(o);
    }

    public List<Orders> getTodayOrderbyShop(int shopId) {
        return orderDAO.getTodayOrderbyShop(shopId);
    }

    public List<Orders> getProcessingOrdersbyShop(int shopId) {
        return orderDAO.getProcessingOrdersbyShop(shopId);
    }

    public List<Orders> getPendingOrdersbyShop(int shopId) {
        return orderDAO.getPendingOrdersbyShop(shopId); // Changed to call getPendingOrdersbyShop
    }

    public double getMonthlyRevenueByShop(int shopId) {
        return orderDAO.getMonthlyRevenue(shopId);
    }

    public double getTotalRevenueByShop(int shopId) {
        return orderDAO.getTotalRevenue(shopId);
    }

    public Map<String, Double> getRevenuePerDay(int shopId, int days) {
        return orderDAO.getRevenuePerDay(shopId, days);
    }

    public List<ProductRevenue> getAllProductRevenues(int shopId) {
        return orderDAO.getAllProductRevenues(shopId);
    }

    public int getTotalOrderCountByShop(int shopId) {
        return orderDAO.getTotalOrderCountByShop(shopId);
    }

    public double getAverageOrderValueByShop(int shopId) {
        return orderDAO.getAverageOrderValueByShop(shopId);
    }

    public int getTotalProductsSoldByShop(int shopId) {
        return orderDAO.getTotalProductsSoldByShop(shopId);
    }

    public List<Orders> searchOrdersWithPaging(Users seller, String status, String keyword, int page, int pageSize) {
        return orderDAO.searchOrdersWithPaging(seller, status, keyword, page, pageSize);
    }

    public int countOrders(Users seller, String status, String keyword) {
        return orderDAO.countOrders(seller, status, keyword);
    }

    public Orders findByOrderNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }

    public List<Orders> findOrdersByUserId(int userId) {
        return orderDAO.findOrdersByUserId(userId);
    }

}
