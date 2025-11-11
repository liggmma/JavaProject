/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.Chat;

import agentAI.ChatTurn;
import agentAI.GeminiChat;
import agentAI.GeminiSummarize;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.*;
import service.*;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONException;
import org.cloudinary.json.JSONObject;

@WebServlet(name = "GeminiChatServlet", urlPatterns = {"/chatAI"})
public class GeminiChatServlet extends HttpServlet {
    
    private ProductService productService;
    private CartService cartService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
        cartService = new CartService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        List<Map<String, String>> history = (session != null)
                ? (List<Map<String, String>>) session.getAttribute("chatHistory")
                : null;

        if (history == null) {
            history = new ArrayList<>();
        }

        JSONArray jsonArray = new JSONArray();
        for (Map<String, String> message : history) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("text", message.get("text"));
                obj.put("sender", message.get("sender"));
            } catch (JSONException ex) {
                Logger.getLogger(GeminiChatServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            jsonArray.put(obj);
        }

        JSONObject result = new JSONObject();
        try {
            result.put("history", jsonArray);
        } catch (JSONException ex) {
            Logger.getLogger(GeminiChatServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Đọc dữ liệu từ body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String prompt;
        try {
            JSONObject json = new JSONObject(sb.toString());
            prompt = json.getString("message").trim();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"reply\": \"Thiếu dữ liệu đầu vào.\"}");
            return;
        }

        // Nếu người dùng nhập "reset" -> xoá session chat history
        if (prompt.equalsIgnoreCase("reset") || prompt.equalsIgnoreCase("quit")) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute("chatHistory");
            }

            JSONObject resJson = new JSONObject();
            try {
                resJson.put("reply", "Đã xóa lịch sử chat.");
            } catch (JSONException ex) {
                Logger.getLogger(GeminiChatServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(resJson.toString());
            return;
        }

        // Lấy hoặc tạo session
        HttpSession session = request.getSession(true);
        Users user = (Users) request.getSession().getAttribute("user");

        String userInfo = (String) session.getAttribute("userInfoString");

        List<Map<String, String>> history = (List<Map<String, String>>) session.getAttribute("chatHistory");
        if (history == null) {
            history = new ArrayList<>();
        }

        // Tạo danh sách ngữ cảnh cho Gemini
        List<ChatTurn> memory = new ArrayList<>();
        for (Map<String, String> msg : history) {
            String role = msg.get("sender").equals("user") ? "user" : "model";
            String text = msg.get("text");
            memory.add(new ChatTurn(role, text));
        }
        memory.add(new ChatTurn("user", prompt));

        // Gọi Gemini API
        String reply;
        try {
            reply = GeminiChat.callGeminiAPI(memory, userInfo);
            if (reply == null || reply.trim().isEmpty()) {
                reply = " Không có phản hồi.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"reply\": \"❌ Lỗi khi gọi Gemini API.\"}");
            return;
        }

        // Lưu lại vào lịch sử session
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("text", prompt);
        userMsg.put("sender", "user");
        history.add(userMsg);

        Map<String, String> botMsg = new HashMap<>();
        botMsg.put("text", reply);
        botMsg.put("sender", "bot");
        history.add(botMsg);

        session.setAttribute("chatHistory", history);

        // Trả kết quả cho client
        JSONObject resJson = new JSONObject();
        try {
            resJson.put("reply", reply);
        } catch (JSONException ex) {
            Logger.getLogger(GeminiChatServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(resJson.toString());

        prompt = prompt.toLowerCase();
        if (prompt.contains("mật khẩu mới của tôi là") || prompt.contains("email mới của tôi là") || prompt.contains("số điện thoại mới của tôi là")) {
            try {
                handleUserUpdate(user, prompt, memory);
            } catch (Exception ex) {
                Logger.getLogger(GeminiChatServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (prompt.contains("tôi muốn thêm sản phẩm")) {
            handleAddToCart(request, user);
            response.sendRedirect("cart"); 
        }
    }

    private void handleUserUpdate(Users user, String prompt, List<ChatTurn> chatHistory) throws Exception {

        prompt = prompt.toLowerCase();

        // Đổi mật khẩu
        if (prompt.contains("mật khẩu") || prompt.contains("password")) {

            GeminiChat.updatePassword(user, GeminiSummarize.summarizeUserRequest(chatHistory));

        }
        // Đổi sdt
        if (prompt.contains("số điện thoại") || prompt.contains("phone")) {

            GeminiChat.updatePhone(user, GeminiSummarize.summarizeUserRequest(chatHistory));

        }
        // Đổi email
        if (prompt.contains("email")) {
            GeminiChat.updateEmail(user, GeminiSummarize.summarizeUserRequest(chatHistory));

        }
    }
  
    
        private void handleAddToCart(HttpServletRequest request, Users user) {
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int quantity = Integer.parseInt(request.getParameter("quantity")); //  đọc quantity

            Products product = productService.getProduct(productId);
            if (product == null) {
                return;
            }

            if (quantity < 1) {
                quantity = 1;
            }

            if (quantity > product.getStockQuantity()) {
                quantity = product.getStockQuantity(); //  giới hạn nếu người dùng cố nhập vượt
            }

            Carts cart = cartService.getOrCreateCartByUser(user);
            cartService.addItemWithQuantity(cart, product, quantity); // gọi service đúng
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
