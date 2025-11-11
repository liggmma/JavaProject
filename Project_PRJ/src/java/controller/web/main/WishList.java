package controller.web.main;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import model.Users;
import model.WishlistItems;
import model.Wishlists;
import service.ProductService;
import service.WishListItemService;
import service.WishlistService;

@WebServlet(name = "wishList", urlPatterns = {"/wishList"})
public class WishList extends HttpServlet {

    private ProductService productService;
    private WishlistService wishlistService;
    private WishListItemService wishListItemService;

    @Override
    public void init() {
        this.wishlistService = new WishlistService();
        this.productService = new ProductService();
        this.wishListItemService = new WishListItemService();
    }

    // Hiển thị danh sách wishlist
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        request.setAttribute("wishlists", user.getWishlists().getWishlistItemsList());
        request.getRequestDispatcher("/wishlist.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        String action = request.getParameter("action");
        if ("add".equalsIgnoreCase(action)) {
            int productId = Integer.parseInt(request.getParameter("productId"));
            boolean check = true;
            for (WishlistItems wi : user.getWishlists().getWishlistItemsList()) {
                if (wi.getProductId().getProductId().equals(productId)) {
                    check = false;
                    break;
                }
            }

            // Check = true -> not in wishlist -> wishlist
            if (check) {
                WishlistItems wi = new WishlistItems(new Date(), productService.findById(productId), wishlistService.findByUserId(user.getUserId()));

                // Update in db
                wishListItemService.add(wi);

                // Update in session
                Wishlists w = user.getWishlists();
                w.getWishlistItemsList().add(wi);
                user.setWishlists(w);
            }

        } else if ("delete".equalsIgnoreCase(action)) {
            int wishlistItemId = Integer.parseInt(request.getParameter("wishlistItemId"));

            Wishlists w = user.getWishlists();
            w.getWishlistItemsList().remove(wishListItemService.findById(wishlistItemId));

            wishListItemService.delete(wishlistItemId);

            user.setWishlists(w);
        }

        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "WishList Controller for viewing and modifying wishlist";
    }
}
