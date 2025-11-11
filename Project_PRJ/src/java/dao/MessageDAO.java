/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import model.Messages;

/**
 *
 * @author ADMIN
 */
public class MessageDAO extends GenericDAO<Messages> {

    private final EntityManagerFactory emf;

    public MessageDAO() {
        super(Messages.class);
        this.emf = EntityManagerFactoryInit.getEntityManagerFactory();
    }

    public List<Messages> getMessagesByConversationId(int conversationId) {
        EntityManager em = emf.createEntityManager();
        List<Messages> result = new ArrayList<>();

        try {
            String jpql = "SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.sentAt ASC";
            List<Messages> messages = em.createQuery(jpql, Messages.class)
                    .setParameter("conversationId", conversationId)
                    .getResultList();
            if (messages != null) {
                result.addAll(messages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return result;
    }

}
