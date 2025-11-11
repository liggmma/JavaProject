package service;

import dao.CartItemDAO;
import model.CartItems;
import model.Carts;
import model.Products;

import java.util.Date;

public class CartItemService {

    private final CartItemDAO cartItemDAO;

    public CartItemService() {
        this.cartItemDAO = new CartItemDAO();
    }

    // Thêm mới hoặc tăng số lượng
    public void addOrUpdate(Carts cart, Products product, int quantity) {
        CartItems item = cartItemDAO.findByCartAndProduct(cart, product);
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
            cartItemDAO.update(item);
        } else {
            CartItems newItem = new CartItems();
            newItem.setCartId(cart);
            newItem.setProductId(product);
            newItem.setQuantity(quantity);
            newItem.setAddedAt(new Date());
            cartItemDAO.add(newItem);
        }
    }

    // Cập nhật số lượng
    public void updateQuantityByCartAndProduct(Carts cart, Products product, int newQuantity) {
        CartItems item = cartItemDAO.findByCartAndProduct(cart, product);
        if (item != null) {
            item.setQuantity(newQuantity);
            cartItemDAO.update(item);
        }
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeByCartAndProduct(Carts cart, Products product) {
        CartItems item = cartItemDAO.findByCartAndProduct(cart, product);
        if (item != null) {
            cartItemDAO.delete(item.getCartItemId());
        }
    }

    public void addItemWithQuantity(Carts cart, Products product, int quantity) {
        cartItemDAO.addOrUpdateItemWithQuantity(cart, product, quantity);
    }
}
