/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.ConversationDAO;
import dao.GenericDAO;
import java.util.List;
import model.Conversations;

/**
 *
 * @author ADMIN
 */
public class ConversationService {

    ConversationDAO conversationDAO;

    public ConversationService() {
        this.conversationDAO = new ConversationDAO();
    }

    public Conversations findById(int id) {
        return conversationDAO.findById(id);
    }

    public void add(Conversations a) {
        conversationDAO.add(a);
    }

    public void delete(int id) {
        conversationDAO.delete(id);
    }

    public void update(Conversations c) {
        conversationDAO.update(c);
    }

    public List<Conversations> getConversationForShop(int shopId) {
        return conversationDAO.getConversationForShop(shopId);
    }

    public List<Conversations> getConversationForUser(int userId) {
        return conversationDAO.getConversationForUser(userId);
    }

}
