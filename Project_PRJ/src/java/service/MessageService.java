/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import dao.MessageDAO;
import java.util.List;
import model.Messages;

/**
 *
 * @author ADMIN
 */
public class MessageService {

    MessageDAO messagesDAO;

    public MessageService() {
        this.messagesDAO = new MessageDAO();
    }

    public Messages findById(int id) {
        return messagesDAO.findById(id);
    }

    public void add(Messages a) {
        messagesDAO.add(a);
    }

    public void delete(int id) {
        messagesDAO.delete(id);
    }

    public List<Messages> getMessagesByConversationId(int conversationId) {
        return messagesDAO.getMessagesByConversationId(conversationId);
    }

}
