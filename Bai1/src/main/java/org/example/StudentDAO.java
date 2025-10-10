package org.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class StudentDAO {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU");
    EntityManager em = emf.createEntityManager();

    // CREATE
    public int addStudent(Student student) {
        try {
            em.getTransaction().begin();
            em.persist(student);
            em.getTransaction().commit();
            return student.getId();
        } catch (Exception e) {
            System.out.println("err : " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
        return 0;
    }

    // READ by ID
    public Student getStudentById(int id) {
        try {
            return em.find(Student.class, id);
        } catch (Exception e) {
            System.out.println("err : " + e.getMessage());
        }
        return null;
    }

    // READ ALL
    public List<Student> getAllStudents() {
        try {
            TypedQuery<Student> query = em.createQuery("SELECT s FROM Student s", Student.class);
            return query.getResultList();
        } catch (Exception e) {
            System.out.println("err : " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public boolean updateStudent(Student student) {
        try {
            em.getTransaction().begin();
            em.merge(student);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            System.out.println("err : " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
        return false;
    }

    // DELETE
    public boolean deleteStudent(int id) {
        try {
            Student student = em.find(Student.class, id);
            if (student != null) {
                em.getTransaction().begin();
                em.remove(student);
                em.getTransaction().commit();
                return true;
            }
        } catch (Exception e) {
            System.out.println("err : " + e.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
        return false;
    }

    // Close resources
    public void close() {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }
}

