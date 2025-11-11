/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.shop.main;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Users;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "Check", urlPatterns = {"/shop"})
public class Check extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users loggedUser = (Users) session.getAttribute("user");
        if (loggedUser.getShops() != null) {
            response.sendRedirect(request.getContextPath() + "/shop/dash-board");
        } else {
            response.sendRedirect(request.getContextPath() + "/shop/register");
        }
    }

}
