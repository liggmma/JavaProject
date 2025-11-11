/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.Shops;

/**
 *
 * @author ADMIN
 */
public class ShopService {

    GenericDAO<Shops> shopDAO;

    public ShopService() {
        this.shopDAO = new GenericDAO<>(Shops.class);
    }

    public Shops findById(int id) {
        return shopDAO.findById(id);
    }

    public void add(Shops s) {
        shopDAO.add(s);
    }

    public void delete(int id) {
        shopDAO.delete(id);
    }

    public void update(Shops s) {
        shopDAO.update(s);
    }

}
