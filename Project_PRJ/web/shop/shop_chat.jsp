<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản Lý Tin Nhắn</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --bg-light: #f9fafb;
                --gray: #6b7280;
                --dark: #111827;
                --border-color: #e5e7eb;
                --blue-chat: #3b82f6;
            }

            body {
                font-family: 'Poppins', sans-serif;
                background: var(--bg-light);
                margin: 0;
                padding: 30px;
                color: var(--dark);
            }

            .page-wrapper {
                max-width: 1200px;
                margin: auto;
            }
            .page-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 25px;
            }
            .page-title {
                font-size: 24px;
                font-weight: 600;
                display: flex;
                align-items: center;
                gap: 12px;
            }
            .back-btn {
                padding: 10px 18px;
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: #fff;
                border: none;
                border-radius: 30px;
                font-weight: 500;
                cursor: pointer;
                text-decoration: none;
                transition: all 0.3s;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }

            .back-btn:hover {
                opacity: 0.9;
                transform: translateY(-2px);
            }

            /* --- Chat App Styles --- */
            .chat-container {
                height: calc(100vh - 125px);
                max-height: 800px;
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 10px 20px rgba(0,0,0,0.06);
                display: flex;
                overflow: hidden;
                border: 1px solid var(--border-color);
            }

            /* Left Pane: Conversation List */
            .conversation-list {
                width: 320px;
                border-right: 1px solid var(--border-color);
                display: flex;
                flex-direction: column;
            }
            .conversation-header {
                padding: 20px;
                font-size: 18px;
                font-weight: 600;
                border-bottom: 1px solid var(--border-color);
                flex-shrink: 0;
            }
            .conversations {
                flex-grow: 1;
                overflow-y: auto;
            }
            .conversation-item {
                display: flex;
                padding: 15px 20px;
                cursor: pointer;
                transition: background-color 0.2s;
                border-bottom: 1px solid var(--border-color);
                gap: 12px;
            }
            .conversation-item:hover {
                background-color: var(--bg-light);
            }
            .conversation-item.active {
                background-color: #fee2e2;
                border-right: 3px solid var(--main-pink);
            }
            .conversation-item .avatar img {
                width: 50px;
                height: 50px;
                border-radius: 50%;
                object-fit: cover;
            }
            .conversation-item .details {
                flex-grow: 1;
                overflow: hidden;
            }
            .conversation-item .name {
                font-weight: 600;
                margin: 0;
            }
            .conversation-item .last-message {
                font-size: 14px;
                color: var(--gray);
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .conversation-item .meta {
                text-align: right;
                font-size: 12px;
                color: var(--gray);
                flex-shrink: 0;
                display: flex;
                flex-direction: column;
                align-items: flex-end;
                gap: 4px;
            }

            /* Huy hiệu tin nhắn chưa đọc - Rõ ràng hơn */
            .unread-badge {
                background-color: var(--main-pink);
                color: white;
                font-size: 11px;
                font-weight: 600;
                border-radius: 50%;
                width: 20px;
                height: 20px;
                display: flex;
                justify-content: center;
                align-items: center;
            }

            /* Right Pane: Chat Window */
            .chat-window {
                flex-grow: 1;
                display: flex;
                flex-direction: column;
            }
            .chat-header {
                display: flex;
                align-items: center;
                padding: 15px 20px;
                border-bottom: 1px solid var(--border-color);
                flex-shrink: 0;
            }
            .chat-header .avatar img {
                width: 40px;
                height: 40px;
                border-radius: 50%;
                object-fit: cover;
                margin-right: 15px;
            }
            .chat-header .name {
                font-weight: 600;
            }

            .message-area {
                flex-grow: 1;
                padding: 20px;
                overflow-y: auto;
                background-color: #f7f7f7;
                display: flex;
                flex-direction: column;
                gap: 10px;
            }
            .message {
                display: flex;
                max-width: 70%;
                align-items: flex-end;
                gap: 8px;
            }
            .message-content {
                padding: 10px 16px;
                border-radius: 18px;
                line-height: 1.5;
                position: relative;
            }
            .message-timestamp {
                font-size: 11px;
                color: var(--gray);
                margin-bottom: 5px;
                opacity: 0;
                transition: opacity 0.2s;
            }
            .message:hover .message-timestamp {
                opacity: 1;
            }

            .message.incoming {
                align-self: flex-start;
            }
            .message.incoming .message-content {
                background-color: #e5e7eb;
                color: var(--dark);
                border-bottom-left-radius: 4px;
            }
            .message.outgoing {
                align-self: flex-end;
                flex-direction: row-reverse;
            }
            .message.outgoing .message-content {
                background-color: var(--main-pink);
                color: white;
                border-bottom-right-radius: 4px;
            }

            .input-area {
                display: flex;
                padding: 15px 20px;
                border-top: 1px solid var(--border-color);
                gap: 10px;
                background: #fff;
                flex-shrink: 0;
            }
            .input-area input {
                flex-grow: 1;
                border: 1px solid var(--border-color);
                border-radius: 30px;
                padding: 10px 20px;
                font-size: 14px;
            }
            .input-area button {
                background: var(--main-pink);
                border: none;
                color: white;
                width: 45px;
                height: 45px;
                border-radius: 50%;
                font-size: 18px;
                cursor: pointer;
                transition: background-color 0.2s;
            }
            .input-area button:hover {
                background: #2563eb;
            }
            .empty-chat-window {
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                height: 100%;
                color: var(--gray);
            }
            @media (max-width: 768px) {
                .conversation-list {
                    width: 100%;
                    border-right: none;
                }
                .chat-window {
                    display: none;
                }
                .chat-container {
                    flex-direction: column;
                }
            }
        </style>
    </head>
    <body>

        <div class="page-wrapper">
            <header class="page-header">
                <h1 class="page-title"><i class="fas fa-headset"></i> Trò Chuyện</h1>
                <a href="dash-board" class="back-btn"><i class="fas fa-arrow-left"></i> Quay lại Dashboard</a>
            </header>

            <main class="chat-container">
                <div class="conversation-list">
                    <div class="conversations">
                        <c:forEach var="conv" items="${conversations}">
                            <a href="chat?id=${conv.conversationId}" style="text-decoration: none; color: inherit;">
                                <div class="conversation-item ${conv.conversationId == currentConversationId ? 'active' : ''}">
                                    <div class="avatar">
                                        <c:choose>
                                            <c:when test="${not empty conv.customerId.avatarUrl}">
                                                <img src="${conv.customerId.avatarUrl}" alt="Avatar">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/images/user_default.svg" alt="Avatar">
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="details">
                                        <h4 class="name">${conv.customerId.username}</h4>
                                        <c:choose>
                                            <c:when test="${not empty conv.messagesList}">
                                                <p class="last-message">${conv.messagesList[conv.messagesList.size() - 1].content}</p>
                                            </c:when>
                                            <c:otherwise>
                                                <p class="last-message text-muted">Chưa có tin nhắn</p>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="meta">
                                        <span class="timestamp"><fmt:formatDate value="${conv.lastMessageAt}" pattern="HH:mm" /></span>
                                        <c:if test="${conv.userUnReadCount > 0}">
                                            <div class="unread-badge">${conv.userUnReadCount}</div>
                                        </c:if>
                                    </div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </div>

                <div class="chat-window">
                    <c:choose>
                        <c:when test="${not empty currentConversation}">
                            <header class="chat-header">
                                <div class="avatar">
                                    <c:choose>
                                        <c:when test="${not empty currentConversation.customerId.avatarUrl}">
                                            <img src="${currentConversation.customerId.avatarUrl}" alt="Avatar">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/images/user_default.svg" alt="Avatar">
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="name">${currentConversation.customerId.username}</div>
                            </header>

                            <div class="message-area" id="messageArea">
                                <c:forEach var="msg" items="${currentConversation.messagesList}">
                                    <div class="message ${msg.senderType == 'shop' ? 'outgoing' : 'incoming'}">
                                        <div class="message-content">${msg.content}</div>
                                        <span class="message-timestamp"><fmt:formatDate value="${msg.sentAt}" pattern="HH:mm"/></span>
                                    </div>
                                </c:forEach>
                            </div>
                            <form class="input-area" method="post" action="chat">
                                <input type="hidden" name="conversationId" value="${currentConversationId}">
                                <input type="text" id="messageInput" name="messageContent" placeholder="Nhập tin nhắn..." required autocomplete="off">
                                <button type="submit" title="Gửi"><i class="fas fa-paper-plane"></i></button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-chat-window">
                                <i class="fas fa-comments" style="font-size: 48px; margin-bottom: 20px;"></i>
                                <p>Chọn một hội thoại để bắt đầu</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>

        <script>
            document.addEventListener('DOMContentLoaded', function () {
                const messageArea = document.getElementById('messageArea');
                if (messageArea) {
                    messageArea.scrollTop = messageArea.scrollHeight;
                }
            });
        </script>
    </body>
</html>