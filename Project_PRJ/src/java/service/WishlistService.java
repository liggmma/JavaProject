package service;

import dao.ProductsDAO;
import dao.WishlistDAO;
import dao.WishlistItemDAO;
import java.util.Date;
import model.Products;
import model.Users;
import model.WishlistItems;
import model.Wishlists;

public class WishlistService {

    private final WishlistDAO wishlistDAO;
    private final WishlistItemDAO wishlistItemDAO;
    private final ProductsDAO productsDAO;

    public WishlistService() {
        wishlistDAO = new WishlistDAO();
        wishlistItemDAO = new WishlistItemDAO();
        productsDAO = new ProductsDAO();
    }

    public Wishlists findById(int id) {
        return wishlistDAO.findById(id);
    }

    public void add(Wishlists wishlist) {
        wishlistDAO.add(wishlist);
    }

    public void delete(int id) {
        wishlistDAO.delete(id);
    }

    public Wishlists findByUserId(int userId) {
        return wishlistDAO.findByUserId(userId);
    }

    public boolean exists(int userId, int productId) {
        return wishlistDAO.exists(userId, productId);
    }

}
