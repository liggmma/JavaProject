/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package com.web.Agent;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import service.AgentService;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "Chat", urlPatterns = {"/chat"})
public class AgentChatServlet extends HttpServlet {

    private AgentService agentService;

    @Override
    public void init() throws ServletException {
        try {
            agentService = new AgentService();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("❌ Không thể khởi tạo AgentService: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        BufferedReader reader = req.getReader();
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        JSONObject json = new JSONObject(jsonBuilder.toString());
        String message = json.getString("message");

        String reply = agentService.chat(message);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(new JSONObject().put("reply", reply).toString());
    }

    // ✅ Thêm GET để test servlet hoạt động
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("✅ AgentChatServlet đang hoạt động!");
    }
}
