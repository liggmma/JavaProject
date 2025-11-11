/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.ProductViewHistory;

/**
 *
 * @author ADMIN
 */
public class ViewHistory {

    GenericDAO<ProductViewHistory> viewHistoryDAO = new GenericDAO<>(ProductViewHistory.class);

}
