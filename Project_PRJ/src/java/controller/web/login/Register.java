/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.web.login;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Carts;
import model.Users;
import model.Wallets;
import model.Wishlists;
import service.CartService;
import service.UserService;
import service.WalletService;
import service.WishlistService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "RegisterController", urlPatterns = {"/register"})
public class Register extends HttpServlet {

    private UserService userService;
    private CartService cartService;
    private WalletService walletService;
    private WishlistService wishlistService;

    @Override
    public void init() {
        this.userService = new UserService();
        this.cartService = new CartService();
        this.walletService = new WalletService();
        this.wishlistService = new WishlistService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            response.sendRedirect("home");
        } else {
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }

    @Override

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");

        // Kiểm tra định dạng email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(email);

        // Kiểm tra định dạng username (có thể thay đổi theo yêu cầu, ví dụ chỉ cho phép chữ và số, ít nhất 3 ký tự)
        String usernameRegex = "^[a-zA-Z0-9]{3,}$";
        Pattern usernamePattern = Pattern.compile(usernameRegex);
        Matcher usernameMatcher = usernamePattern.matcher(username);

        // Kiểm tra định dạng password (ít nhất 6 ký tự, chứa cả chữ và số)
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$";
        Pattern passwordPattern = Pattern.compile(passwordRegex);
        Matcher passwordMatcher = passwordPattern.matcher(password);

        // Kiểm tra nếu định dạng không đúng
        boolean error = false;
        if (!emailMatcher.matches()) {
            request.setAttribute("errorEmail", "Email không hợp lệ");
            error = true;
        }
        if (!usernameMatcher.matches()) {
            request.setAttribute("errorUsername", "Username phải có ít nhất 3 ký tự và chỉ chứa chữ và số");
            error = true;
        }
        if (!passwordMatcher.matches()) {
            request.setAttribute("errorPassword", "Password phải có ít nhất 6 ký tự và chứa cả chữ cái và số");
            error = true;
        }

        if (error) {
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // Kiểm tra trùng username và email
        for (Users u : userService.getAll()) {
            if (u.getUsername().equals(username)) {
                request.setAttribute("error", "Username đã tồn tại");
                request.getRequestDispatcher("register.jsp").forward(request, response);
                return;
            } else if (u.getEmail().equals(email)) {
                request.setAttribute("error", "Email đã tồn tại");
                request.getRequestDispatcher("register.jsp").forward(request, response);
                return;
            }
        }

        // Tạo người dùng mới
        Users u = new Users(username, password, email);
        u.setRole("user");
        u.setStatus("active");
        u.setCreatedAt(new Date());
        u.setUpdatedAt(new Date());
        userService.add(u);

        // Tạo giỏ hàng cho người dùng mới
        Carts c = new Carts();
        c.setCustomerId(u);
        c.setCreatedAt(new Date());
        cartService.add(c);

        // Tạo ví cho người dùng mới
        Wallets w = new Wallets();
        w.setUserId(u);
        w.setBalance(BigDecimal.ZERO);
        w.setCreatedAt(new Date());
        w.setUpdatedAt(new Date());
        walletService.add(w);

        // Tạo danh sách yêu thích cho người dùng mới
        Wishlists wishlist = new Wishlists();
        wishlist.setCustomerId(u);
        wishlist.setCreatedAt(new Date());
        wishlistService.add(wishlist);

        // Đăng nhập người dùng
        Users loggedUser1 = userService.findById(u.getUserId());
        HttpSession session = request.getSession();
        session.setAttribute("user", loggedUser1);

        response.sendRedirect("home");
    }

}
