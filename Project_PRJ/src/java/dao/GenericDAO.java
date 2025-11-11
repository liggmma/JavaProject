package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import model.Conversations;

/**
 *
 * @author ADMIN
 * @param <T>
 */
public class GenericDAO<T> {

    private final Class<T> entityClass;
    private final EntityManagerFactory emf;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public List<T> getAll() {
        EntityManager em = emf.createEntityManager();
        List<T> result = null;
        try {
            result = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return result;
    }

    public void add(T t) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(t);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            T t = em.find(entityClass, id);
            if (t != null) {
                em.remove(t);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public void update(T t) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.merge(t);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public T findById(int id) {
        EntityManager em = emf.createEntityManager();
        T t = null;
        try {
            t = em.find(entityClass, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return t;
    }

}
