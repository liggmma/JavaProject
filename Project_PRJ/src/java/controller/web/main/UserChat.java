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
import jakarta.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;
import model.Conversations;
import model.Messages;
import model.Users;
import service.ConversationService;
import service.MessageService;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "UserChat", urlPatterns = {"/chat"})
public class UserChat extends HttpServlet {

    private MessageService messageService = new MessageService();
    private ConversationService conversationService = new ConversationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy thông tin người dùng và cửa hàng
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        // Đảm bảo người dùng đã đăng nhập và có shop
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        int userId = user.getUserId();

        // 2. Lấy toàn bộ danh sách hội thoại cho khung bên trái
        List<Conversations> conversations = conversationService.getConversationForUser(userId);

        // Trả về danh sách đã được sắp xếp theo tin nhắn mới nhất
        conversations.sort((c1, c2) -> {
            Date d1 = c1.getLastMessageAt();
            Date d2 = c2.getLastMessageAt();
            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return 1;  // null xuống cuối
            }
            if (d2 == null) {
                return -1;
            }
            return d2.compareTo(d1); // sắp xếp giảm dần (mới nhất trước)
        });

        request.setAttribute("conversations", conversations);

        // 3. Xác định và tải hội thoại cần hiển thị ở khung bên phải
        String conversationIdParam = request.getParameter("id");
        Conversations currentConversation = null;
        int unReadCount = 0;
        int currentConversationId = 0;

        if (conversationIdParam != null) {
            try {
                currentConversationId = Integer.parseInt(conversationIdParam);

                Conversations conversation = conversationService.findById(currentConversationId);

                for (Messages m : conversation.getMessagesList()) {
                    if (m.getSenderType().equalsIgnoreCase("shop")) {
                        m.setIsRead(Boolean.TRUE);
                    }
                }
                conversationService.update(conversation);
                // Giả sử bạn có service để tìm hội thoại theo ID
                currentConversation = conversationService.findById(currentConversationId);
            } catch (NumberFormatException e) {
                // Xử lý nếu ID không hợp lệ, có thể ghi log
                System.err.println("Invalid conversation ID: " + conversationIdParam);
            }
        } else if (conversations == null && conversations.isEmpty() || conversationIdParam == null) {
            currentConversation = null; // Lấy hội thoại đầu tiên trong danh sách đã sắp xếp
        }

        // 4. Gửi dữ liệu sang JSP
        // Gửi đối tượng hội thoại đang được chọn (chứa các tin nhắn)
        request.setAttribute("currentConversation", currentConversation);
        request.setAttribute("unreadCount", unReadCount);
        // Gửi ID để JSP có thể làm nổi bật mục đang được chọn trong danh sách
        request.setAttribute("currentConversationId", currentConversationId);

        // 5. Chuyển hướng đến trang JSP
        request.getRequestDispatcher("user_chat.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Lấy thông tin từ form và session
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        String content = request.getParameter("messageContent");
        String conversationIdStr = request.getParameter("conversationId");

        // 2. Kiểm tra dữ liệu đầu vào
        if (user == null || content == null || content.trim().isEmpty() || conversationIdStr == null) {
            // Nếu thiếu dữ liệu, không làm gì và chuyển hướng lại
            response.sendRedirect("chat" + (conversationIdStr != null ? "?id=" + conversationIdStr : ""));
            return;
        }

        try {
            int conversationId = Integer.parseInt(conversationIdStr);
            Conversations conversation = conversationService.findById(conversationId);

            // 3. Tạo đối tượng tin nhắn mới
            Messages newMessage = new Messages();
            newMessage.setContent(content);
            newMessage.setSentAt(new java.util.Date()); // Thời gian hiện tại
            newMessage.setSenderType("user"); // Người gửi là user
            newMessage.setMessageType("text");
            newMessage.setIsRead(Boolean.FALSE);
            newMessage.setSenderId(user.getUserId()); // ID của người gửi 

            // Liên kết tin nhắn với cuộc hội thoại
            newMessage.setConversationId(conversation);

            // 4. Lưu tin nhắn vào cơ sở dữ liệu
            // Giả sử bạn có messageService để tạo tin nhắn mới
            messageService.add(newMessage);

            conversation.setLastMessageAt(new java.util.Date());
            // 5. Chuyển hướng người dùng trở lại đúng cuộc hội thoại đó
            response.sendRedirect("chat?id=" + conversationId);

        } catch (NumberFormatException e) {
            // Xử lý nếu ID hội thoại không hợp lệ
            System.err.println("Lỗi định dạng ID hội thoại khi gửi tin nhắn: " + conversationIdStr);
            response.sendRedirect("dash-board"); // Chuyển về trang chính
        } catch (Exception e) {
            e.printStackTrace();
            // Xử lý các lỗi khác
            response.sendRedirect("error-page");
        }
    }

}
