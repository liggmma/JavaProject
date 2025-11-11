/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.OrderItems;

/**
 *
 * @author ADMIN
 */
public class OrderItemService {

    GenericDAO<OrderItems> orderItemDAO = new GenericDAO<>(OrderItems.class);

    public OrderItems findbyId(int id) {
        return orderItemDAO.findById(id);
    }

}
