let stompClient = null;
let sessionId = null;
let currentUserId = null;
let isSendingMessage = false;

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Disable debug logging
    stompClient.debug = null;
    
    stompClient.connect({}, function(frame) {
        console.log('WebSocket Connected:', frame);
        
        const topic = '/topic/chat.' + currentUserId;
        console.log('Subscribing to topic:', topic);
        
        // Subscribe to personal message channel
        stompClient.subscribe(topic, function(message) {
            console.log('Received message on topic:', topic, message.body);
            try {
                const messageData = JSON.parse(message.body);
                console.log('Parsed message data:', messageData);
                
                // Check if this is a SESSION_ENDED notification
                if (messageData.type === 'SESSION_ENDED') {
                    console.log('Session ended by staff/admin');
                    handleSessionEnded();
                    return;
                }
                
                // Check if this is a STAFF_JOINED notification
                if (messageData.type === 'STAFF_JOINED') {
                    console.log('Staff joined:', messageData.staffName);
                    handleStaffJoined(messageData);
                    return;
                }
                
                // Check if this is an ERROR notification
                if (messageData.type === 'ERROR') {
                    console.error('Error from server:', messageData.message);
                    showPopup('Lỗi', messageData.message || 'Đã xảy ra lỗi. Vui lòng thử lại.', 'error');
                    return;
                }
                
                // Remove temporary message if exists (optimistic update)
                if (messageData.id && !messageData.id.toString().startsWith('temp-')) {
                    const tempMsgs = document.querySelectorAll('[data-temp-id]');
                    console.log('Found temporary messages to remove:', tempMsgs.length);
                    // Remove ALL temporary messages (in case server response is slow)
                    tempMsgs.forEach(msg => msg.remove());
                }
                
                // Display message (will skip if duplicate)
                displayMessage(messageData);
                scrollToBottom();
            } catch (error) {
                console.error('Error parsing message:', error, message.body);
            }
        });
        
        console.log('WebSocket subscription successful');
        
        // Load chat history if session exists
        if (sessionId) {
            loadChatHistory();
        }
    }, function(error) {
        console.error('WebSocket connection error:', error);
        const messagesContainer = document.getElementById('chatMessages');
        if (messagesContainer && !messagesContainer.querySelector('.loading')) {
            messagesContainer.innerHTML = 
                '<div class="loading">Lỗi kết nối. Đang thử kết nối lại...</div>';
        }
        setTimeout(connectWebSocket, 5000); // Retry after 5 seconds
    });
}

function loadChatHistory() {
    if (!sessionId) {
        console.warn('No sessionId available, skipping loadChatHistory');
        return;
    }
    
    console.log('Loading chat history for session:', sessionId);
    
    fetch(`/api/chat/sessions/${sessionId}/messages`)
        .then(response => {
            console.log('Load chat history response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(messages => {
            console.log('Loaded messages:', messages.length);
            const messagesContainer = document.getElementById('chatMessages');
            messagesContainer.innerHTML = '';
            
            if (messages.length === 0) {
                messagesContainer.innerHTML = '<div class="empty-chat"><p>Chưa có tin nhắn nào. Hãy bắt đầu cuộc trò chuyện!</p></div>';
                return;
            }
            
            messages.forEach(message => {
                displayMessage(message);
            });
            
            scrollToBottom();
            
            // Mark messages as read
            markMessagesAsRead();
        })
        .catch(error => {
            console.error('Error loading chat history:', error);
            const messagesContainer = document.getElementById('chatMessages');
            if (messagesContainer && !messagesContainer.querySelector('.loading')) {
                messagesContainer.innerHTML = 
                    '<div class="loading">Lỗi khi tải tin nhắn. Vui lòng tải lại trang.</div>';
            }
        });
}

function displayMessage(message) {
    const messagesContainer = document.getElementById('chatMessages');
    const loadingDiv = messagesContainer.querySelector('.loading');
    if (loadingDiv) {
        loadingDiv.remove();
    }
    
    const emptyDiv = messagesContainer.querySelector('.empty-chat');
    if (emptyDiv) {
        emptyDiv.remove();
    }
    
    const isSent = message.senderId === currentUserId;
    const messageClass = isSent ? 'sent' : 'received';
    // Trong view của customer: tin nhắn từ customer hiển thị "Bạn", từ staff/admin hiển thị role (chữ thường)
    let senderName;
    if (isSent) {
        senderName = 'Bạn';
    } else {
        // Hiển thị role (STAFF -> staff, ADMIN -> admin) thay vì username
        if (message.senderRole) {
            senderName = message.senderRole.toLowerCase(); // STAFF -> staff, ADMIN -> admin
        } else {
            senderName = 'staff'; // Mặc định nếu không có role
        }
    }
    
    // Check if message already exists (avoid duplicates)
    if (message.id && !message.id.toString().startsWith('temp-')) {
        const existingMsg = messagesContainer.querySelector(`[data-message-id="${message.id}"]`);
        if (existingMsg) {
            console.log('Message already displayed, skipping:', message.id);
            return;
        }
    }
    
    // Additional check: if this is a real message (not temp), remove any temp messages with same content
    if (message.id && !message.id.toString().startsWith('temp-') && message.message) {
        const tempMsgs = messagesContainer.querySelectorAll('[data-temp-id]');
        tempMsgs.forEach(tempMsg => {
            const tempMsgText = tempMsg.querySelector('.message-bubble')?.textContent?.trim();
            if (tempMsgText === message.message.trim()) {
                console.log('Removing duplicate temp message with content:', tempMsgText);
                tempMsg.remove();
            }
        });
    }
    
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${messageClass}`;
    if (message.id) {
        messageDiv.setAttribute('data-message-id', message.id);
    }
    if (message.id && message.id.toString().startsWith('temp-')) {
        messageDiv.setAttribute('data-temp-id', message.id);
    }
    
    const time = new Date(message.sentAt).toLocaleTimeString('vi-VN', { 
        hour: '2-digit', 
        minute: '2-digit' 
    });
    
    messageDiv.innerHTML = `
        <div class="message-bubble">${escapeHtml(message.message)}</div>
        <div class="message-meta">${senderName} • ${time}</div>
    `;
    
    messagesContainer.appendChild(messageDiv);
    scrollToBottom();
}

function sendMessage(event) {
    event.preventDefault();
    
    // Prevent double submission
    if (isSendingMessage) {
        console.log('Message sending in progress, ignoring duplicate request');
        return;
    }
    
    const messageInput = document.getElementById('messageInput');
    const message = messageInput.value.trim();
    
    if (!message) {
        return;
    }
    
    if (!stompClient || !stompClient.connected) {
        showPopup('Thông báo', 'Chưa kết nối WebSocket. Đang thử kết nối lại...', 'info');
        connectWebSocket();
        return;
    }
    
    if (!sessionId) {
        showPopup('Thông báo', 'Chưa có phiên chat. Vui lòng tải lại trang.', 'info');
        return;
    }
    
    // Set sending flag
    isSendingMessage = true;
    
    // Optimistic update: Hiển thị tin nhắn ngay
    const tempMessage = {
        id: 'temp-' + Date.now(),
        senderId: currentUserId,
        senderName: 'Bạn',
        receiverId: null,
        receiverName: '',
        sessionId: sessionId,
        message: message,
        isRead: false,
        sentAt: new Date().toISOString()
    };
    displayMessage(tempMessage);
    
    const messageRequest = {
        sessionId: sessionId,
        message: message
    };
    
    // Disable send button
    const sendButton = document.getElementById('sendButton');
    sendButton.disabled = true;
    
    try {
        stompClient.send('/app/chat.send', {}, JSON.stringify(messageRequest));
        console.log('Message sent via WebSocket:', messageRequest);
        
        // Clear input immediately after sending
        messageInput.value = '';
        messageInput.style.height = 'auto';
    } catch (error) {
        console.error('Error sending message:', error);
        showPopup('Lỗi', 'Lỗi khi gửi tin nhắn. Vui lòng thử lại.', 'error');
    }
    
    // Reset flag after a short delay to allow server response
    setTimeout(() => {
        isSendingMessage = false;
        sendButton.disabled = false;
    }, 500);
}

function markMessagesAsRead() {
    if (!sessionId) return;
    
    fetch(`/api/chat/sessions/${sessionId}/read`, {
        method: 'POST'
    }).catch(error => {
        console.error('Error marking messages as read:', error);
    });
}

function scrollToBottom() {
    const messagesContainer = document.getElementById('chatMessages');
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Request chat support - tạo session mới hoặc lấy session ACTIVE
function requestChatSupport() {
    const requestButton = document.getElementById('requestChatButton');
    if (!requestButton) return;
    
    requestButton.disabled = true;
    requestButton.textContent = 'Đang kết nối...';
    
    fetch('/api/chat/sessions', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Không thể tạo phiên chat');
            });
        }
        return response.json();
    })
    .then(session => {
        console.log('Chat session created/retrieved:', session);
        sessionId = session.id;
        window.sessionId = session.id;
        
        // Hiển thị thông tin staff hoặc trạng thái chờ
        const staffInfo = document.getElementById('staffInfo');
        if (staffInfo) {
            if (session.staffName) {
                staffInfo.textContent = `Đang chat với: ${session.staffName}`;
            } else {
                staffInfo.textContent = 'Đang chờ staff tham gia...';
                staffInfo.style.color = '#ff9800';
            }
        }
        
        // Ẩn nút yêu cầu, hiện khung chat
        showChatWindow();
        
        // Kết nối WebSocket nếu chưa kết nối
        if (!stompClient || !stompClient.connected) {
            connectWebSocket();
        } else {
            // Đã kết nối, chỉ cần load history
            loadChatHistory();
        }
    })
    .catch(error => {
        console.error('Error requesting chat support:', error);
        showPopup('Lỗi', 'Không thể tạo phiên chat. ' + error.message, 'error');
        requestButton.disabled = false;
        requestButton.textContent = 'Yêu cầu chat hỗ trợ';
    });
}

// Hiển thị khung chat
function showChatWindow() {
    const requestContainer = document.getElementById('requestChatContainer');
    const chatWindow = document.getElementById('chatWindow');
    
    if (requestContainer) {
        requestContainer.classList.add('hidden');
    }
    if (chatWindow) {
        chatWindow.classList.add('active');
    }
}

// Ẩn khung chat, hiện nút yêu cầu
function hideChatWindow() {
    const requestContainer = document.getElementById('requestChatContainer');
    const chatWindow = document.getElementById('chatWindow');
    
    if (requestContainer) {
        requestContainer.classList.remove('hidden');
    }
    if (chatWindow) {
        chatWindow.classList.remove('active');
    }
    
    // Clear session
    sessionId = null;
    window.sessionId = null;
}

// Xử lý khi session bị kết thúc
function handleSessionEnded() {
    hideChatWindow();
    const requestButton = document.getElementById('requestChatButton');
    if (requestButton) {
        requestButton.disabled = false;
        requestButton.textContent = 'Yêu cầu chat hỗ trợ';
    }
    
    // Clear messages
    const messagesContainer = document.getElementById('chatMessages');
    if (messagesContainer) {
        messagesContainer.innerHTML = '';
    }
}

// Xử lý khi staff join vào phòng
function handleStaffJoined(notification) {
    console.log('Staff joined notification:', notification);
    
    // Cập nhật thông tin staff
    const staffInfo = document.getElementById('staffInfo');
    if (staffInfo) {
        staffInfo.textContent = `Đang chat với: ${notification.staffName}`;
        staffInfo.style.color = '';
    }
    
    // Hiển thị thông báo
    const messagesContainer = document.getElementById('chatMessages');
    if (messagesContainer) {
        const notificationDiv = document.createElement('div');
        notificationDiv.className = 'message received';
        notificationDiv.style.textAlign = 'center';
        notificationDiv.style.margin = '10px 0';
        notificationDiv.style.fontStyle = 'italic';
        notificationDiv.style.color = '#666';
        notificationDiv.innerHTML = `<div style="padding: 8px; background: #e3f2fd; border-radius: 8px;">Staff ${escapeHtml(notification.staffName)} đã tham gia phòng</div>`;
        messagesContainer.appendChild(notificationDiv);
        scrollToBottom();
    }
    
    // Reload session để cập nhật thông tin
    if (sessionId) {
        fetch(`/api/chat/sessions/${sessionId}`)
            .then(response => response.json())
            .then(session => {
                if (session.staffName) {
                    const staffInfo = document.getElementById('staffInfo');
                    if (staffInfo) {
                        staffInfo.textContent = `Đang chat với: ${session.staffName}`;
                    }
                }
            })
            .catch(error => console.error('Error reloading session:', error));
    }
}

// Kiểm tra session ACTIVE khi load trang
function checkActiveSession() {
    if (!currentUserId) return;
    
    fetch('/api/chat/sessions')
        .then(response => response.json())
        .then(data => {
            if (data && data.sessions && data.sessions.length > 0) {
                // Có session ACTIVE hoặc WAITING, hiển thị chat
                const activeSession = data.sessions[0];
                sessionId = activeSession.id;
                window.sessionId = activeSession.id;
                
                // Hiển thị thông tin staff hoặc trạng thái chờ
                const staffInfo = document.getElementById('staffInfo');
                if (staffInfo) {
                    if (activeSession.staffName) {
                        staffInfo.textContent = `Đang chat với: ${activeSession.staffName}`;
                        staffInfo.style.color = '';
                    } else {
                        staffInfo.textContent = 'Đang chờ staff tham gia...';
                        staffInfo.style.color = '#ff9800';
                    }
                }
                
                showChatWindow();
                
                // Kết nối WebSocket
                if (currentUserId) {
                    connectWebSocket();
                }
            } else {
                // Không có session ACTIVE, hiển thị nút yêu cầu
                hideChatWindow();
            }
        })
        .catch(error => {
            console.error('Error checking active session:', error);
            // On error, show request button
            hideChatWindow();
        });
}

// Auto-resize textarea
document.addEventListener('DOMContentLoaded', function() {
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
        messageInput.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = Math.min(this.scrollHeight, 120) + 'px';
        });
    }
    
    // Initialize from window object
    sessionId = window.sessionId || null;
    currentUserId = window.currentUserId || null;
    
    console.log('Chat initialized - sessionId:', sessionId, 'currentUserId:', currentUserId);
    
    // Kiểm tra session ACTIVE
    if (currentUserId) {
        checkActiveSession();
    } else {
        console.error('Missing currentUserId!');
    }
});

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    if (stompClient) {
        stompClient.disconnect();
    }
});

