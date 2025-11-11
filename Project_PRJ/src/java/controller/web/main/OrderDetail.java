/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.web.main;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Orders;
import service.OrdersService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "OrderDetail", urlPatterns = {"/order-detail"})
public class OrderDetail extends HttpServlet {

    OrdersService orderSerivce = new OrdersService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        int id = Integer.parseInt(idStr);
        Orders order = orderSerivce.findById(id);
        request.setAttribute("orderDetail", order);
        request.getRequestDispatcher("order-detail.jsp").forward(request, response);
    }

}
