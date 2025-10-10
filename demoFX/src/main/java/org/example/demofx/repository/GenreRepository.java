package org.example.demofx.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.demofx.model.Genre;

import java.util.List;

public class GenreRepository {

    protected static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("PU");

    public void save(Genre genre) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (genre.getId() == 0) {
                em.persist(genre);
            } else {
                em.merge(genre);
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
            Genre genre = em.find(Genre.class, id);
            if (genre != null) {
                em.remove(genre);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public Genre findById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Genre.class, id);
        } finally {
            em.close();
        }
    }

    public List<Genre> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT g FROM Genre g", Genre.class).getResultList();
        } finally {
            em.close();
        }
    }
}
