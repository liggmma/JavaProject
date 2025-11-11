package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import model.Carts;

public class CartDAO extends GenericDAO<Carts> {

    private final EntityManagerFactory emf;

    public CartDAO() {
        super(Carts.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public Carts findByUserId(int userId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Carts c WHERE c.customerId.userId = :userId", Carts.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
