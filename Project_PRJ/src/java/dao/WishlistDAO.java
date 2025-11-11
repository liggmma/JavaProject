package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import model.Users;
import model.Wishlists;

public class WishlistDAO extends GenericDAO<Wishlists> {

    private final EntityManagerFactory emf;

    public WishlistDAO() {
        super(Wishlists.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public Wishlists findByUserId(int userId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Wishlists> results = em.createQuery(
                    "SELECT w FROM Wishlists w "
                    + "LEFT JOIN FETCH w.wishlistItemsList i "
                    + "LEFT JOIN FETCH i.productId "
                    + "WHERE w.customerId.userId = :userId "
                    + "ORDER BY w.createdAt DESC", // lấy wishlist mới nhất nếu có nhiều
                    Wishlists.class
            )
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .getResultList();

            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public boolean exists(int userId, int productId) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT COUNT(wi) FROM WishlistItems wi "
                    + "WHERE wi.wishlistId.customerId.userId = :userId "
                    + "AND wi.productId.productId = :productId";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("userId", userId);
            query.setParameter("productId", productId);
            Long count = query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public List<Wishlists> getRecentWishlists(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT w FROM Wishlists w ORDER BY w.wishlistId DESC";
            return em.createQuery(jpql, Wishlists.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Wishlists createForUser(Users user) {
        EntityManager em = emf.createEntityManager();
        Wishlists wishlist = new Wishlists();
        wishlist.setCustomerId(user);
        wishlist.setCreatedAt(new Date());

        try {
            em.getTransaction().begin();
            em.persist(wishlist);
            em.getTransaction().commit();
            return wishlist;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
