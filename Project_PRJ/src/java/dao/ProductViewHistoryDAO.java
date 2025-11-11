/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import model.ProductViewHistory;
import model.Products;

/**
 *
 * @author ADMIN
 */
public class ProductViewHistoryDAO extends GenericDAO<ProductViewHistory> {

    private final EntityManagerFactory emf;

    public ProductViewHistoryDAO() {
        super(ProductViewHistory.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public int getShopNumberOfView(int shopId) {
        EntityManager em = emf.createEntityManager();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        try {
            String jpql = "SELECT COUNT(pvh) FROM ProductViewHistory pvh "
                    + "WHERE pvh.productId.shopId.shopId = :shopId "
                    + "AND pvh.viewedAt BETWEEN :startOfDay AND :endOfDay";
            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("shopId", shopId)
                    .setParameter("startOfDay", java.sql.Timestamp.valueOf(startOfDay))
                    .setParameter("endOfDay", java.sql.Timestamp.valueOf(endOfDay))
                    .getSingleResult();
            return count.intValue();
        } finally {
            em.close();
        }
    }

    public List<Products> getUserProductView(int userId) {
        EntityManager em = emf.createEntityManager();

        try {
            String jpql = "SELECT pvh.productId FROM ProductViewHistory pvh "
                    + "WHERE pvh.userId.userId = :userId "
                    + "ORDER BY pvh.viewedAt DESC";

            return em.createQuery(jpql, Products.class)
                    .setParameter("userId", userId)
                    .setMaxResults(30)
                    .getResultList();

        } finally {
            em.close();
        }
    }

}
