/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.GenericDAO;
import model.Wallets;
import model.WishlistItems;

/**
 *
 * @author ADMIN
 */
public class WishListItemService {

    GenericDAO<WishlistItems> wishlistItemstDAO;

    public WishListItemService() {
        this.wishlistItemstDAO = new GenericDAO<>(WishlistItems.class);
    }

    public WishlistItems findById(int id) {
        return wishlistItemstDAO.findById(id);
    }

    public void add(WishlistItems a) {
        wishlistItemstDAO.add(a);
    }

    public void delete(int id) {
        wishlistItemstDAO.delete(id);
    }

}
