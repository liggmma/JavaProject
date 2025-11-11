package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.NoResultException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Orders;
import model.ProductRevenue;
import model.Products;
import model.Users;
import model.Shops; // Import Shops model

/**
 *
 * @author ADMIN
 */
public class OrderDAO extends GenericDAO<Orders> {

    // Giữ lại cách khởi tạo EntityManagerFactory từ EntityManagerFactoryInit
    private final EntityManagerFactory emf;

    public OrderDAO() {
        super(Orders.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory(); // Sử dụng EntityManagerFactoryInit của bạn
    }

    public Orders getOrderById(int orderId) {
        EntityManager em = emf.createEntityManager(); // Tạo EntityManager từ emf
        try {
            return em.find(Orders.class, orderId);
        } finally {
            if (em != null && em.isOpen()) { // Đảm bảo đóng EntityManager
                em.close();
            }
        }
    }

    public Shops getShopByOrderId(int orderId) {
        EntityManager em = emf.createEntityManager(); // Tạo EntityManager từ emf
        try {
            String jpql = """
                SELECT DISTINCT p.shopId FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE o.orderId = :orderId
            """;
            TypedQuery<Shops> query = em.createQuery(jpql, Shops.class);
            query.setParameter("orderId", orderId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; // No shop found for this order
        } finally {
            if (em != null && em.isOpen()) { // Đảm bảo đóng EntityManager
                em.close();
            }
        }
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        EntityManager em = emf.createEntityManager(); // Tạo EntityManager từ emf
        try {
            em.getTransaction().begin();
            Orders order = em.find(Orders.class, orderId);
            if (order != null) {
                order.setStatus(newStatus);
                em.merge(order);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback(); // Rollback if order not found
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (em != null && em.isOpen()) { // Đảm bảo đóng EntityManager
                em.close();
            }
        }
    }

    public List<Orders> getTodayOrderbyShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        List<Orders> orders;

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        try {
            String jpql = """
                SELECT DISTINCT o FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
                  AND o.createdAt BETWEEN :start AND :end
            """;

            orders = em.createQuery(jpql, Orders.class)
                    .setParameter("shopId", shopId)
                    .setParameter("start", java.sql.Timestamp.valueOf(startOfDay))
                    .setParameter("end", java.sql.Timestamp.valueOf(endOfDay))
                    .getResultList();

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return orders;
    }

    public List<Orders> getProcessingOrdersbyShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        List<Orders> orders;

        try {

            String jpql = """
                SELECT DISTINCT o FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
                  AND o.status = 'Processing'
            """;

            orders = em.createQuery(jpql, Orders.class)
                    .setParameter("shopId", shopId)
                    .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return orders;
    }

    public List<Orders> getPendingOrdersbyShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        List<Orders> orders;

        try {

            String jpql = """
                SELECT DISTINCT o FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
                  AND o.status = 'Pending'
            """;

            orders = em.createQuery(jpql, Orders.class)
                    .setParameter("shopId", shopId)
                    .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return orders;
    }

    public double getMonthlyRevenue(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = """
                SELECT SUM(oi.unitPrice * oi.quantity) FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
                  AND FUNCTION('MONTH', o.createdAt) = :month
                  AND FUNCTION('YEAR', o.createdAt) = :year
                  AND o.status = 'Delivered'
            """;

            Object result = em.createQuery(jpql)
                    .setParameter("shopId", shopId)
                    .setParameter("month", LocalDate.now().getMonthValue())
                    .setParameter("year", LocalDate.now().getYear())
                    .getSingleResult();

            if (result instanceof Number numberResult) {
                return numberResult.doubleValue();
            } else {
                return 0.0;
            }

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public double getTotalRevenue(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = """
            SELECT SUM(oi.unitPrice * oi.quantity) FROM Orders o
            JOIN o.orderItemsList oi
            JOIN oi.productId p
            WHERE p.shopId.shopId = :shopId
              AND o.status = 'Delivered'
        """;

            Object result = em.createQuery(jpql)
                    .setParameter("shopId", shopId)
                    .getSingleResult();

            if (result instanceof Number numberResult) {
                return numberResult.doubleValue();
            } else {
                return 0.0;
            }

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public Map<String, Double> getRevenuePerDay(int shopId, int days) {
        EntityManager em = emf.createEntityManager();
        Map<String, Double> revenueMap = new LinkedHashMap<>();

        try {
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                Timestamp startTimestamp = Timestamp.valueOf(startOfDay);
                Timestamp endTimestamp = Timestamp.valueOf(endOfDay);

                String jpql = """
                SELECT SUM(oi.unitPrice * oi.quantity) FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
                  AND o.status = 'Delivered'
                  AND o.createdAt BETWEEN :start AND :end
            """;

                Object result = em.createQuery(jpql)
                        .setParameter("shopId", shopId)
                        .setParameter("start", startTimestamp)
                        .setParameter("end", endTimestamp)
                        .getSingleResult();

                double revenue = (result instanceof Number number) ? number.doubleValue() : 0.0;
                revenueMap.put(date.toString(), revenue);
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return revenueMap;
    }

    public List<ProductRevenue> getAllProductRevenues(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = """
            SELECT
                oi.productId,
                SUM(oi.unitPrice * oi.quantity),
                SUM(oi.quantity)
            FROM Orders o
            JOIN o.orderItemsList oi
            JOIN oi.productId p
            WHERE p.shopId.shopId = :shopId
              AND o.status = 'Delivered'
            GROUP BY oi.productId
            ORDER BY SUM(oi.unitPrice * oi.quantity) DESC
        """;

            List<Object[]> resultList = em.createQuery(jpql, Object[].class)
                    .setParameter("shopId", shopId)
                    .getResultList();

            List<ProductRevenue> productRevenues = new ArrayList<>();
            for (Object[] row : resultList) {
                Products product = (Products) row[0];
                Object revenueObject = row[1];
                Object quantityObject = row[2];

                double revenue = 0.0;
                if (revenueObject instanceof BigDecimal) {
                    revenue = ((BigDecimal) revenueObject).doubleValue();
                } else if (revenueObject instanceof Double) {
                    revenue = (Double) revenueObject;
                } else if (revenueObject instanceof Long) {
                    revenue = ((Long) revenueObject).doubleValue();
                }

                int quantitySold = 0;
                if (quantityObject instanceof Long) {
                    quantitySold = ((Long) quantityObject).intValue();
                } else if (quantityObject instanceof Number) {
                    quantitySold = ((Number) quantityObject).intValue();
                }

                String productName = product.getName();
                String imageUrl = product.getProductImagesList().isEmpty() ? null : product.getProductImagesList().get(0).getImageUrl();

                productRevenues.add(new ProductRevenue(productName, revenue, quantitySold, imageUrl));
            }

            return productRevenues;

        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public int getTotalOrderCountByShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = """
                SELECT COUNT(DISTINCT o)
                FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
            """;

            Long totalOrders = em.createQuery(jpql, Long.class)
                    .setParameter("shopId", shopId)
                    .getSingleResult();

            return totalOrders != null ? totalOrders.intValue() : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public double getAverageOrderValueByShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = """
                SELECT AVG(o.totalAmount)
                FROM Orders o
                WHERE o.status = 'Delivered' AND o.orderId IN (
                    SELECT DISTINCT o2.orderId
                    FROM Orders o2
                    JOIN o2.orderItemsList oi
                    JOIN oi.productId p
                    WHERE p.shopId.shopId = :shopId
                )
            """;

            Double average = em.createQuery(jpql, Double.class)
                    .setParameter("shopId", shopId)
                    .getSingleResult();

            return (average != null) ? average : 0.0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public int getTotalProductsSoldByShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = """
                SELECT SUM(oi.quantity)
                FROM Orders o
                JOIN o.orderItemsList oi
                JOIN oi.productId p
                WHERE p.shopId.shopId = :shopId
                  AND o.status = 'Delivered'
            """;

            Long totalQuantity = em.createQuery(jpql, Long.class)
                    .setParameter("shopId", shopId)
                    .getSingleResult();

            return (totalQuantity != null) ? totalQuantity.intValue() : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Orders> searchOrdersWithPaging(Users seller, String status, String keyword, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT DISTINCT o FROM Orders o "
                    + "JOIN o.orderItemsList oi "
                    + "JOIN oi.productId p "
                    + "WHERE p.shopId.shopId = :shopId ";

            if (status != null && !status.isEmpty()) {
                jpql += "AND o.status = :status ";
            }

            if (keyword != null && !keyword.isEmpty()) {
                jpql += "AND (CAST(o.orderId AS string) LIKE :keyword "
                        + "OR o.customerId.username LIKE :keyword "
                        + "OR p.productName LIKE :keyword) ";
            }
            jpql += "ORDER BY o.createdAt DESC";

            TypedQuery<Orders> query = em.createQuery(jpql, Orders.class);
            query.setParameter("shopId", seller.getShops().getShopId());

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            if (keyword != null && !keyword.isEmpty()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }

            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public int countOrders(Users seller, String status, String keyword) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(DISTINCT o) FROM Orders o "
                    + "JOIN o.orderItemsList oi "
                    + "JOIN oi.productId p "
                    + "WHERE p.shopId.shopId = :shopId ";

            if (status != null && !status.isEmpty()) {
                jpql += "AND o.status = :status ";
            }

            if (keyword != null && !keyword.isEmpty()) {
                jpql += "AND (CAST(o.orderId AS string) LIKE :keyword "
                        + "OR o.customerId.username LIKE :keyword "
                        + "OR p.productName LIKE :keyword) ";
            }

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("shopId", seller.getShops().getShopId());

            if (status != null && !status.isEmpty()) {
                query.setParameter("status", status);
            }

            if (keyword != null && !keyword.isEmpty()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }

            return query.getSingleResult().intValue();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public Orders findByOrderNumber(String orderNumber) {
        EntityManager em = emf.createEntityManager();
        try {
            // Câu lệnh JPQL để tìm một đơn hàng theo trường orderNumber
            String jpql = "SELECT o FROM Orders o WHERE o.orderNumber = :orderNumber";

            // Thực thi truy vấn và mong đợi một kết quả duy nhất
            return em.createQuery(jpql, Orders.class)
                    .setParameter("orderNumber", orderNumber)
                    .getSingleResult();

        } catch (NoResultException e) {
            // getSingleResult() sẽ ném ra lỗi này nếu không tìm thấy kết quả nào.
            // Đây là trường hợp bình thường, ta sẽ trả về null.
            return null;
        } finally {
            // Luôn đóng EntityManager để giải phóng tài nguyên.
            em.close();
        }
    }

    public List<Orders> findOrdersByUserId(int userId) {
        EntityManager em = emf.createEntityManager();
        try {
            // Câu lệnh JPQL để chọn các đơn hàng có customerId.userId khớp với tham số
            String jpql = "SELECT o FROM Orders o WHERE o.customerId.userId = :userId ORDER BY o.createdAt DESC";

            // Thực thi truy vấn và trả về danh sách kết quả
            return em.createQuery(jpql, Orders.class)
                    .setParameter("userId", userId)
                    .getResultList();

        } catch (Exception e) {
            // Trong trường hợp có lỗi không mong muốn, in lỗi ra và trả về một danh sách rỗng
            e.printStackTrace();
            return new ArrayList<>(); // Trả về danh sách rỗng để tránh lỗi NullPointerException
        } finally {
            // Luôn đóng EntityManager
            em.close();
        }
    }

}
