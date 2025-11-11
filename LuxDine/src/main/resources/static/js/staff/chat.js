let stompClient = null;
let currentUserId = null;
let currentSessionId = null;
let currentSessionCustomerId = null; // Lưu customerId của session hiện tại
let initialSessionId = null; // Session ID từ URL (nếu có)
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
                
                // Check if this is an ERROR notification
                if (messageData.type === 'ERROR') {
                    console.error('Error from server:', messageData.message);
                    showPopup('Lỗi', messageData.message || 'Đã xảy ra lỗi. Vui lòng thử lại.', 'error');
                    return;
                }
                
                // Always refresh chat list to update unread counts
                loadChatList();
                
                // If message is for current session, display it
                if (messageData.sessionId === currentSessionId) {
                    // Remove temporary messages (optimistic update)
                    if (messageData.id && !messageData.id.toString().startsWith('temp-')) {
                        const tempMsgs = document.querySelectorAll('[data-temp-id]');
                        console.log('Found temporary messages to remove:', tempMsgs.length);
                        tempMsgs.forEach(msg => msg.remove());
                    }
                    
                    displayMessage(messageData);
                    scrollToBottom();
                    markMessagesAsRead(currentSessionId);
                }
            } catch (error) {
                console.error('Error parsing message:', error, message.body);
            }
        });
        
        console.log('WebSocket subscription successful');
        
        // Load chat list
        loadChatList();
        
        // If there's an initial session, load it
        if (window.initialSessionId) {
            selectChatSession(window.initialSessionId);
        }
    }, function(error) {
        console.error('WebSocket connection error:', error);
        const chatList = document.getElementById('chatList');
        if (chatList && !chatList.querySelector('.loading')) {
            chatList.innerHTML = 
                '<div class="loading">Lỗi kết nối. Đang thử kết nối lại...</div>';
        }
        setTimeout(connectWebSocket, 5000); // Retry after 5 seconds
    });
}

function loadChatList() {
    // Admin có thể filter theo status
    let url = '/api/chat/sessions';
    if (window.isAdmin && window.currentFilter) {
        url += '?status=' + window.currentFilter;
    }
    
    fetch(url)
        .then(response => {
            console.log('Load chat list response status:', response.status);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Loaded chat list data:', data);
            const chatList = document.getElementById('chatList');
            const previousActiveId = currentSessionId;
            chatList.innerHTML = '';
            
            if (!data || !data.sessions || data.sessions.length === 0) {
                console.log('No chat sessions found in data:', data);
                chatList.innerHTML = '<div class="loading">Chưa có cuộc trò chuyện nào</div>';
                return;
            }
            
            console.log('Creating chat list items for', data.sessions.length, 'sessions');
            data.sessions.forEach(session => {
                const chatItem = createChatListItem(session);
                chatList.appendChild(chatItem);
            });
            
            // Restore active state after refresh
            if (previousActiveId) {
                const activeItem = chatList.querySelector(`[data-session-id="${previousActiveId}"]`);
                if (activeItem) {
                    activeItem.classList.add('active');
                }
            }
        })
        .catch(error => {
            console.error('Error loading chat list:', error);
            document.getElementById('chatList').innerHTML = 
                '<div class="loading">Lỗi khi tải danh sách chat. Xem console để biết chi tiết.</div>';
        });
}

function createChatListItem(session) {
    const item = document.createElement('div');
    item.className = 'chat-item';
    item.dataset.sessionId = session.id;
    if (session.id === currentSessionId) {
        item.classList.add('active');
    }
    
    // Kiểm tra nếu session WAITING (không có staffId) - hiển thị nút "Tham gia"
    const isWaiting = !session.staffId || session.staffId === null;
    
    if (!isWaiting) {
        // Session ACTIVE - click vào để xem chat
        item.onclick = function() {
            selectChatSession(session.id);
        };
    }
    
    const time = session.lastMessageAt 
        ? new Date(session.lastMessageAt).toLocaleTimeString('vi-VN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        })
        : new Date(session.createdAt).toLocaleTimeString('vi-VN', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
    
    const preview = session.lastMessage 
        ? (session.lastMessage.length > 50 ? session.lastMessage.substring(0, 50) + '...' : session.lastMessage)
        : 'Chưa có tin nhắn';
    
    if (isWaiting) {
        // WAITING session - hiển thị nút "Tham gia"
        item.innerHTML = `
            <div class="chat-item-header">
                <div>
                    <div class="chat-item-name">${escapeHtml(session.customerName)} <span style="color: #ff9800; font-size: 0.85em;">(Đang chờ)</span></div>
                    <div class="chat-item-time">${time}</div>
                </div>
            </div>
            <div class="chat-item-preview">${escapeHtml(preview)}</div>
            <button class="btn-join-chat" onclick="event.stopPropagation(); joinChatSession(${session.id})" style="
                width: 100%;
                margin-top: 8px;
                padding: 8px;
                background: linear-gradient(135deg, var(--color-primary) 0%, #1a1a3a 100%);
                color: #fff;
                border: none;
                border-radius: 6px;
                font-weight: 600;
                cursor: pointer;
                transition: filter 0.2s;
            " onmouseover="this.style.filter='brightness(0.95)'" onmouseout="this.style.filter=''">
                Tham gia
            </button>
        `;
    } else {
        // ACTIVE session - hiển thị như bình thường
        item.innerHTML = `
            <div class="chat-item-header">
                <div>
                    <div class="chat-item-name">${escapeHtml(session.customerName)}</div>
                    <div class="chat-item-time">${time}</div>
                </div>
                ${session.unreadCount > 0 ? `<span class="unread-badge">${session.unreadCount}</span>` : ''}
            </div>
            <div class="chat-item-preview">${escapeHtml(preview)}</div>
        `;
    }
    
    return item;
}

function selectChatSession(sessionId) {
    // Kiểm tra xem session có phải WAITING không (chỉ cho staff, không cho admin)
    // Admin có thể xem tất cả sessions
    if (!window.isAdmin) {
        const sessionItem = document.querySelector(`[data-session-id="${sessionId}"]`);
        if (sessionItem) {
            const isWaiting = sessionItem.querySelector('.btn-join-chat') !== null;
            if (isWaiting) {
                // Không cho phép select WAITING session, phải join trước
                showPopup('Thông báo', 'Vui lòng nhấn nút "Tham gia" để tham gia phòng này', 'info');
                return;
            }
        }
    }
    
    currentSessionId = sessionId;
    currentSessionCustomerId = null; // Reset khi chuyển session
    
    // Update active state in list - store sessionId in data attribute for easier lookup
    document.querySelectorAll('.chat-item').forEach(item => {
        item.classList.remove('active');
        if (item.dataset.sessionId === String(sessionId)) {
            item.classList.add('active');
        }
    });
    
    // Load session details trước, sau đó mới load messages
    loadChatSession(sessionId).then(() => {
        loadChatMessages(sessionId);
    });
    
    // Show input area
    document.getElementById('chatInputArea').style.display = 'block';
    
    // Show end chat button (chỉ cho staff đã join hoặc admin)
    const endChatButton = document.getElementById('endChatButton');
    if (endChatButton) {
        // Chỉ hiện nút end chat nếu không phải WAITING session
        const sessionItem = document.querySelector(`[data-session-id="${sessionId}"]`);
        const isWaiting = sessionItem && sessionItem.querySelector('.btn-join-chat') !== null;
        if (!isWaiting) {
            endChatButton.style.display = 'block';
        } else {
            endChatButton.style.display = 'none';
        }
    }
}

function loadChatSession(sessionId) {
    return fetch(`/api/chat/sessions/${sessionId}`)
        .then(response => response.json())
        .then(session => {
            document.getElementById('chatWindowTitle').textContent = 
                `Chat với ${session.customerName}`;
            // Lưu customerId để xác định tin nhắn từ customer hay staff
            currentSessionCustomerId = session.customerId;
            return session;
        })
        .catch(error => {
            console.error('Error loading chat session:', error);
            throw error;
        });
}

function loadChatMessages(sessionId) {
    fetch(`/api/chat/sessions/${sessionId}/messages`)
        .then(response => response.json())
        .then(messages => {
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
            markMessagesAsRead(sessionId);
            loadChatList(); // Refresh to update unread count
        })
        .catch(error => {
            console.error('Error loading chat messages:', error);
            document.getElementById('chatMessages').innerHTML = 
                '<div class="loading">Lỗi khi tải tin nhắn</div>';
        });
}

function displayMessage(message) {
    const messagesContainer = document.getElementById('chatMessages');
    const emptyDiv = messagesContainer.querySelector('.empty-chat');
    if (emptyDiv) {
        emptyDiv.remove();
    }
    
    // Trong view của staff:
    // - Tin nhắn từ customer (senderId === customerId) → hiển thị bên trái (received)
    // - Tin nhắn từ staff (bất kỳ staff nào) → hiển thị bên phải (sent)
    // - Nếu senderId === currentUserId thì hiển thị "Bạn", ngược lại hiển thị tên staff
    const isFromCustomer = currentSessionCustomerId && message.senderId === currentSessionCustomerId;
    const isSent = !isFromCustomer; // Tin nhắn từ staff (bất kỳ) → sent
    const messageClass = isSent ? 'sent' : 'received';
    const senderName = (isSent && message.senderId === currentUserId) 
        ? 'Bạn' 
        : (message.senderName || 'Người dùng');
    
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
    
    if (!currentSessionId) {
        showPopup('Thông báo', 'Vui lòng chọn một cuộc trò chuyện trước', 'info');
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
    
    // Set sending flag
    isSendingMessage = true;
    
    // Optimistic update: Hiển thị tin nhắn ngay
    const tempMessage = {
        id: 'temp-' + Date.now(),
        senderId: currentUserId,
        senderName: 'Bạn',
        receiverId: null,
        receiverName: '',
        sessionId: currentSessionId,
        message: message,
        isRead: false,
        sentAt: new Date().toISOString()
    };
    displayMessage(tempMessage);
    
    const messageRequest = {
        sessionId: currentSessionId,
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

function markMessagesAsRead(sessionId) {
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
    currentUserId = window.currentUserId || null;
    initialSessionId = window.initialSessionId || null;
    
    // Ẩn filter tabs nếu không phải admin
    if (!window.isAdmin) {
        const filterTabs = document.getElementById('chatFilterTabs');
        if (filterTabs) {
            filterTabs.style.display = 'none';
        }
    }
    
    console.log('Staff chat initialized - currentUserId:', currentUserId, 'initialSessionId:', initialSessionId, 'isAdmin:', window.isAdmin);
    
    // Connect WebSocket
    if (currentUserId) {
        connectWebSocket();
    } else {
        console.error('Missing currentUserId!');
        document.getElementById('chatList').innerHTML = 
            '<div class="loading">Lỗi: Không thể khởi tạo chat. Vui lòng tải lại trang.</div>';
    }
});

function endChatSession() {
    if (!currentSessionId) {
        showPopup('Thông báo', 'Vui lòng chọn một cuộc trò chuyện trước', 'info');
        return;
    }
    
    showConfirmPopup(
        'Xác nhận kết thúc chat',
        'Bạn có chắc muốn kết thúc cuộc trò chuyện này? Customer sẽ không thấy chat và lịch sử nữa.',
        () => {
            // onConfirm callback
            proceedEndChatSession();
        },
        () => {
            // onCancel callback - do nothing
        }
    );
}

function proceedEndChatSession() {
    
    const endChatButton = document.getElementById('endChatButton');
    if (endChatButton) {
        endChatButton.disabled = true;
        endChatButton.textContent = 'Đang kết thúc...';
    }
    
    fetch(`/api/chat/sessions/${currentSessionId}/end`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Không thể kết thúc chat');
            });
        }
        return response;
    })
    .then(() => {
        console.log('Chat session ended:', currentSessionId);
        
        // Refresh chat list
        loadChatList();
        
        // Clear current session
        currentSessionId = null;
        
        // Hide input area
        document.getElementById('chatInputArea').style.display = 'none';
        
        // Hide end chat button
        if (endChatButton) {
            endChatButton.style.display = 'none';
            endChatButton.disabled = false;
            endChatButton.textContent = 'Kết thúc chat';
        }
        
        // Show empty state
        document.getElementById('chatWindowTitle').textContent = 'Chọn một cuộc trò chuyện';
        document.getElementById('chatMessages').innerHTML = 
            '<div class="empty-chat"><h3>Chưa có cuộc trò chuyện nào</h3><p>Chọn một cuộc trò chuyện từ danh sách bên trái để bắt đầu</p></div>';
        
        showPopup('Thành công', 'Đã kết thúc cuộc trò chuyện. Customer sẽ không thấy chat và lịch sử nữa.', 'success');
    })
    .catch(error => {
        console.error('Error ending chat session:', error);
        showPopup('Lỗi', 'Không thể kết thúc chat. ' + error.message, 'error');
        if (endChatButton) {
            endChatButton.disabled = false;
            endChatButton.textContent = 'Kết thúc chat';
        }
    });
}

// Join chat session (chỉ cho WAITING sessions)
function joinChatSession(sessionId) {
    const button = event.target;
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = 'Đang tham gia...';
    
    fetch(`/api/chat/sessions/${sessionId}/join`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || 'Không thể tham gia phòng');
            });
        }
        return response.json();
    })
    .then(session => {
        console.log('Successfully joined session:', session);
        
        // Refresh chat list để cập nhật
        loadChatList();
        
        // Tự động select session vừa join
        setTimeout(() => {
            selectChatSession(sessionId);
        }, 500);
        
        showPopup('Thành công', 'Đã tham gia phòng hỗ trợ', 'success');
    })
    .catch(error => {
        console.error('Error joining chat session:', error);
        const errorMessage = error.message.includes('Đã có staff khác') 
            ? 'Đã có staff khác tham gia phòng này' 
            : 'Không thể tham gia phòng. ' + error.message;
        showPopup('Lỗi', errorMessage, 'error');
        button.disabled = false;
        button.textContent = originalText;
    });
}

// Switch filter (chỉ cho admin)
function switchFilter(status) {
    if (!window.isAdmin) return;
    
    window.currentFilter = status;
    
    // Update active tab
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.classList.remove('active');
        if (tab.dataset.filter === status) {
            tab.classList.add('active');
        }
    });
    
    // Reload chat list
    loadChatList();
}

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    if (stompClient) {
        stompClient.disconnect();
    }
});

