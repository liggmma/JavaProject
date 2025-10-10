package org.example.DAO;

import jakarta.persistence.*;
import org.example.Model.*;

import java.util.List;

public class CustomerDao {
    private final EntityManager em;

    public CustomerDao(EntityManager em) {
        this.em = em;
    }

    public Customer addCustomer(Customer c) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(c);
            tx.commit();
            return c;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        }
    }

    public Customer updateCustomer(Customer c) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Customer merged = em.merge(c);
            tx.commit();
            return merged;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        }
    }
    public Customer updateCustomerDetails(int customerId, Customer updatedData) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Customer existing = em.find(Customer.class, customerId);
            if (existing == null) {
                throw new IllegalArgumentException("Customer not found with id: " + customerId);
            }

            existing.getInvoices().removeIf(inv ->
                    updatedData.getInvoices().stream().noneMatch(newInv -> newInv.getId() == inv.getId()));


            for (Invoice newInv : updatedData.getInvoices()) {
                if (newInv.getId() == 0) {

                    newInv.setCustomer(existing);
                    existing.addInvoice(newInv);
                } else {

                    for (Invoice oldInv : existing.getInvoices()) {
                        if (oldInv.getId() == newInv.getId()) {
                            oldInv.setAmount(newInv.getAmount());
                            oldInv.setDate(newInv.getDate());
                            oldInv.setDescription(newInv.getDescription());
                        }
                    }
                }
            }


            existing.getPromotions().removeIf(pro ->
                    updatedData.getPromotions().stream().noneMatch(newPro -> newPro.getId() == pro.getId()));

            for (Promotion newPro : updatedData.getPromotions()) {
                boolean exists = existing.getPromotions().stream().anyMatch(p -> p.getId() == newPro.getId());
                if (!exists) {
                    existing.addPromotion(em.merge(newPro));
                }
            }

            tx.commit();
            return existing;
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        }
    }


    public Customer getCustomer(int id) {
        return em.find(Customer.class, id);
    }

    public List<Customer> getAllCustomers() {
        return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
    }

    public void deleteCustomer(int id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Customer c = em.find(Customer.class, id);
            if (c != null) {
                c.getPromotions().forEach(p -> p.getCustomers().remove(c));
                c.getPromotions().clear();

                em.remove(c);
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        }
    }
}

