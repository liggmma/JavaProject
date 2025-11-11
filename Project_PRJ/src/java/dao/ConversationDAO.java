/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import model.Conversations;

/**
 *
 * @author ADMIN
 */
public class ConversationDAO extends GenericDAO<Conversations> {

    private final EntityManagerFactory emf;

    public ConversationDAO() {
        super(Conversations.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public List<Conversations> getConversationForShop(int shopId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Conversations c WHERE c.shopId.shopId = :shopId", Conversations.class)
                    .setParameter("shopId", shopId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Conversations> getConversationForUser(int userId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Conversations c WHERE c.customerId.userId = :userId", Conversations.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
