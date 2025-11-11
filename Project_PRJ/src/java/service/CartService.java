package service;

import dao.CartDAO;
import model.Carts;
import model.Products;
import model.Users;

import java.util.Date;

public class CartService {

    private final CartDAO cartDAO;
    private final CartItemService cartItemService;

    public CartService() {
        this.cartDAO = new CartDAO();
        this.cartItemService = new CartItemService();
    }

    public Carts findById(int id) {
        return cartDAO.findById(id);
    }

    public void add(Carts cart) {
        cartDAO.add(cart);
    }

    public void delete(int id) {
        cartDAO.delete(id);
    }

    public Carts getOrCreateCartByUser(Users user) {
        Carts cart = cartDAO.findByUserId(user.getUserId());
        if (cart == null) {
            cart = new Carts();
            cart.setCustomerId(user);
            cart.setCreatedAt(new Date());
            cartDAO.add(cart);
        }
        return cart;
    }

    public void addOrIncrementItem(Carts cart, Products product) {
        cartItemService.addOrUpdate(cart, product, 1);
    }

    public void updateItemQuantity(Carts cart, Products product, int quantity) {
        cartItemService.updateQuantityByCartAndProduct(cart, product, quantity);
    }

    public void removeItem(Carts cart, Products product) {
        cartItemService.removeByCartAndProduct(cart, product);
    }

    public void addItemWithQuantity(Carts cart, Products product, int quantity) {
        cartItemService.addItemWithQuantity(cart, product, quantity);
    }

}
