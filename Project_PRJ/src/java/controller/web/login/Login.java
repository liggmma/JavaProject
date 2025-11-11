/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.web.login;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Users;
import service.UserService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("user") != null) {
            response.sendRedirect("home");
        } else {
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean rememberMe = "true".equals(request.getParameter("rememberMe"));

        List<Users> users = userService.getAll();
        boolean authenticated = false;
        Users loggedUser = null;
        for (Users user : users) {
            if (user.getUsername().equals(username) && user.getPasswordHash().equals(password) && user.getStatus().equals("active")) {
                loggedUser = userService.findById(user.getUserId());
                HttpSession session = request.getSession();
                session.setAttribute("user", loggedUser);
                session.setAttribute("userInfoString", loggedUser.toString());
                authenticated = true;
                break;
            }
        }
        if (authenticated) {
            if (rememberMe) {
                Cookie usernameCookie = new Cookie("username", username);
                Cookie passwordCookie = new Cookie("password", password);
                Cookie rememberMeCookie = new Cookie("rememberMe", "true");
                usernameCookie.setMaxAge(30 * 24 * 60 * 60);
                passwordCookie.setMaxAge(30 * 24 * 60 * 60);
                rememberMeCookie.setMaxAge(30 * 24 * 60 * 60);
                response.addCookie(usernameCookie);
                response.addCookie(passwordCookie);
                response.addCookie(rememberMeCookie);
            } else {
                Cookie usernameCookie = new Cookie("username", username);
                Cookie passwordCookie = new Cookie("password", "");
                Cookie rememberMeCookie = new Cookie("rememberMe", "");
                usernameCookie.setMaxAge(30 * 24 * 60 * 60);
                passwordCookie.setMaxAge(0);
                rememberMeCookie.setMaxAge(0);
                response.addCookie(usernameCookie);
                response.addCookie(passwordCookie);
                response.addCookie(rememberMeCookie);
            }
            if (loggedUser.getRole().equals("admin")) {
                response.sendRedirect("home");
            } else {
                response.sendRedirect("home");
            }
        } else {
            request.setAttribute("error", "Invalid username or password");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
