package org.example.demofx.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.demofx.model.Book;

import java.util.List;

public class BookRepository {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("PU");

    public void save(Book book) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (book.getId() == 0) {
                em.persist(book);  // thêm mới
            } else {
                em.merge(book);    // cập nhật
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Book book = em.find(Book.class, id);
            if (book != null) {
                em.remove(book);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public Book findById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Book.class, id);
        } finally {
            em.close();
        }
    }

    public List<Book> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM Book b", Book.class).getResultList();
        } finally {
            em.close();
        }
    }
}
