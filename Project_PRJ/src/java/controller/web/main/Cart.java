package controller.web.main;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.*;
import service.CartService;
import service.ProductService;

import java.io.IOException;
import java.util.Date;

@WebServlet(name = "Cart", urlPatterns = {"/cart"})
public class Cart extends HttpServlet {

    private ProductService productService;
    private CartService cartService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
        cartService = new CartService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users user = (Users) request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            handleAddToCart(request, user);
            response.sendRedirect("cart"); // redirect sau khi thêm
            return;
        }

        // Hiển thị giỏ hàng
        Carts userCart = cartService.getOrCreateCartByUser(user);
        System.out.println(userCart);
        System.out.println("Cart item size: " + userCart.getCartItemsList().size());
        for (CartItems item : userCart.getCartItemsList()) {
            System.out.println(item);
        }
        request.setAttribute("cartItems", userCart.getCartItemsList());
        request.getRequestDispatcher("cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users user = (Users) request.getSession().getAttribute("user");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("cart");
            return;
        }

        switch (action) {
            case "update":
                handleUpdateCart(request, response, user); // ✅ truyền thêm response vào đây
                return; // ✅ quan trọng: return để không tiếp tục gọi response.sendRedirect bên dưới
            case "remove":
                handleRemoveFromCart(request, user);
                break;
        }

        response.sendRedirect("cart");
    }

    private void handleAddToCart(HttpServletRequest request, Users user) {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity")); // ✅ đọc quantity

            Products product = productService.getProduct(productId);
            if (product == null) {
                return;
            }

            if (quantity < 1) {
                quantity = 1;
            }

            if (quantity > product.getStockQuantity()) {
                quantity = product.getStockQuantity(); // ✅ giới hạn nếu người dùng cố nhập vượt
            }

            Carts cart = cartService.getOrCreateCartByUser(user);
            cartService.addItemWithQuantity(cart, product, quantity); // ✅ gọi service đúng
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateCart(HttpServletRequest request, HttpServletResponse response, Users user) throws ServletException, IOException {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            Products product = productService.getProduct(productId);
            if (product == null) {
                return;
            }

            if (quantity > product.getStockQuantity()) {
                Carts cart = cartService.getOrCreateCartByUser(user);
                request.setAttribute("cartItems", cart.getCartItemsList());
                request.setAttribute("errorProductId", productId);
                request.setAttribute("errorMessage", "Số lượng vượt quá tồn kho! Tối đa: " + product.getStockQuantity());

                request.getRequestDispatcher("cart.jsp").forward(request, response);
                return;
            }

            Carts cart = cartService.getOrCreateCartByUser(user);
            cartService.updateItemQuantity(cart, product, quantity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRemoveFromCart(HttpServletRequest request, Users user) {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));

            Products product = productService.getProduct(productId);
            if (product == null) {
                return;
            }

            Carts cart = cartService.getOrCreateCartByUser(user);
            cartService.removeItem(cart, product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
