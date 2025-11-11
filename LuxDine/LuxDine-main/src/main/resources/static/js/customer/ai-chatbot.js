/**
 * AI Chatbot Widget JavaScript
 * Handles WebSocket communication with backend AI chatbot service
 */

class AIChatbot {
    constructor() {
        this.sessionId = null;
        this.stompClient = null;
        this.isConnected = false;
        this.messageHistory = [];

        // DOM elements
        this.toggleBtn = document.getElementById('aiChatbotToggle');
        this.closeBtn = document.getElementById('aiChatbotClose');
        this.chatWindow = document.getElementById('aiChatbotWindow');
        this.messagesContainer = document.getElementById('aiChatbotMessages');
        this.inputField = document.getElementById('aiChatbotInput');
        this.sendBtn = document.getElementById('aiChatbotSend');
        this.welcomeMsg = document.getElementById('aiChatbotWelcome');
        this.typingIndicator = document.getElementById('aiChatbotTyping');

        this.init();
    }

    /**
     * Initialize the chatbot
     */
    init() {
        console.log('[AIChatbot] Initializing...');

        // Load session from localStorage
        this.sessionId = localStorage.getItem('aiChatbotSessionId');

        // Setup event listeners
        this.setupEventListeners();

        // Connect to WebSocket
        this.connect();
    }

    /**
     * Setup all event listeners
     */
    setupEventListeners() {
        // Toggle button
        this.toggleBtn.addEventListener('click', () => this.toggleChat());

        // Close button
        this.closeBtn.addEventListener('click', () => this.closeChat());

        // Send button
        this.sendBtn.addEventListener('click', () => this.sendMessage());

        // Input field - Enter key
        this.inputField.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Suggestion buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('ai-chatbot-suggestion')) {
                const message = e.target.dataset.message;
                this.inputField.value = message;
                this.sendMessage();
            }
        });
    }

    /**
     * Connect to WebSocket
     */
    connect() {
        console.log('[AIChatbot] Connecting to WebSocket...');

        try {
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);

            // Disable debug logs in production
            this.stompClient.debug = (msg) => {
                if (window.location.hostname === 'localhost') {
                    console.log('[STOMP]', msg);
                }
            };

            this.stompClient.connect({},
                () => this.onConnected(),
                (error) => this.onError(error)
            );
        } catch (error) {
            console.error('[AIChatbot] Connection error:', error);
            this.showError('Failed to connect to chat service. Please refresh the page.');
        }
    }

    /**
     * Called when WebSocket connection is established
     */
    onConnected() {
        console.log('[AIChatbot] Connected to WebSocket');
        this.isConnected = true;

        // Create or retrieve session
        this.createSession().then(() => {
            // Subscribe to messages for this session
            const topic = `/topic/ai-chatbot.${this.sessionId}`;
            console.log('[AIChatbot] Subscribing to:', topic);

            this.stompClient.subscribe(topic, (message) => {
                this.onMessageReceived(JSON.parse(message.body));
            });
        });
    }

    /**
     * Called when WebSocket connection error occurs
     */
    onError(error) {
        console.error('[AIChatbot] WebSocket error:', error);
        this.isConnected = false;

        // Attempt to reconnect after 5 seconds
        setTimeout(() => {
            console.log('[AIChatbot] Attempting to reconnect...');
            this.connect();
        }, 5000);
    }

    /**
     * Create or get chat session
     */
    async createSession() {
        try {
            // If we already have a session ID, use it
            if (this.sessionId) {
                console.log('[AIChatbot] Using existing session:', this.sessionId);
                return;
            }

            // Create new session via REST API
            const response = await fetch('/api/ai-chatbot/sessions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const data = await response.json();
                this.sessionId = data.sessionId;
                localStorage.setItem('aiChatbotSessionId', this.sessionId);
                console.log('[AIChatbot] Created new session:', this.sessionId);
            } else {
                throw new Error('Failed to create session');
            }
        } catch (error) {
            console.error('[AIChatbot] Error creating session:', error);
            // Generate fallback session ID for guests
            this.sessionId = 'guest-' + this.generateUUID();
            localStorage.setItem('aiChatbotSessionId', this.sessionId);
        }
    }

    /**
     * Toggle chat window
     */
    toggleChat() {
        if (this.chatWindow.classList.contains('open')) {
            this.closeChat();
        } else {
            this.openChat();
        }
    }

    /**
     * Open chat window
     */
    openChat() {
        this.chatWindow.classList.add('open');
        this.inputField.focus();

        // Hide badge
        const badge = document.getElementById('aiChatbotBadge');
        if (badge) {
            badge.classList.remove('show');
        }
    }

    /**
     * Close chat window
     */
    closeChat() {
        this.chatWindow.classList.remove('open');
    }

    /**
     * Send message
     */
    sendMessage() {
        const message = this.inputField.value.trim();

        if (!message || !this.isConnected) {
            return;
        }

        // Clear input
        this.inputField.value = '';

        // Hide welcome message
        if (this.welcomeMsg) {
            this.welcomeMsg.style.display = 'none';
        }

        // Display user message immediately
        this.displayMessage(message, 'user');

        // Send via WebSocket
        try {
            const messageRequest = {
                sessionId: this.sessionId,
                message: message
            };

            this.stompClient.send('/app/ai-chatbot.send', {}, JSON.stringify(messageRequest));
            console.log('[AIChatbot] Message sent:', message);

        } catch (error) {
            console.error('[AIChatbot] Error sending message:', error);
            this.showError('Failed to send message. Please try again.');
        }
    }

    /**
     * Handle received messages from WebSocket
     */
    onMessageReceived(data) {
        console.log('[AIChatbot] Message received:', data);

        // Handle different message types
        if (data.type === 'TYPING') {
            this.showTypingIndicator(data.isTyping);
        } else if (data.type === 'ERROR') {
            this.hideTypingIndicator();
            this.showError(data.message);
        } else if (data.aiResponse) {
            this.hideTypingIndicator();

            // Check if this is a reservation checkout redirect
            if (data.aiResponse.startsWith('RESERVATION_CHECKOUT:')) {
                this.handleReservationCheckout(data.aiResponse);
            } else {
                this.displayMessage(data.aiResponse, 'bot');
            }
        }
    }

    /**
     * Handle reservation checkout redirect
     */
    handleReservationCheckout(response) {
        try {
            // Extract JSON data from response
            const jsonStr = response.substring('RESERVATION_CHECKOUT:'.length);
            const reservationData = JSON.parse(jsonStr);

            console.log('[AIChatbot] Reservation checkout data:', reservationData);

            // Display confirmation message
            this.displayMessage(
                'Great! Your reservation details have been confirmed. Redirecting you to checkout...',
                'bot'
            );

            // Parse reservationDate (OffsetDateTime string) to separate fields
            let date, timeHour, timeMinute, departureTimeHour, departureTimeMinute;
            
            try {
                // Parse OffsetDateTime string (e.g., "2025-11-10T18:30:00+07:00" or "2025-11-10T18:30:00Z")
                const reservationDateStr = reservationData.reservationDate;
                if (!reservationDateStr) {
                    throw new Error('Missing reservationDate');
                }

                // Parse OffsetDateTime string directly (format: YYYY-MM-DDTHH:mm:ss[+/-]HH:mm or YYYY-MM-DDTHH:mm:ssZ)
                // Extract date and time components directly from string to avoid timezone conversion issues
                const dateTimeMatch = reservationDateStr.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})/);
                if (!dateTimeMatch) {
                    // Fallback to Date parsing if regex doesn't match
                    const dateTime = new Date(reservationDateStr);
                    const year = dateTime.getFullYear();
                    const month = String(dateTime.getMonth() + 1).padStart(2, '0');
                    const day = String(dateTime.getDate()).padStart(2, '0');
                    date = `${year}-${month}-${day}`;
                    timeHour = String(dateTime.getHours()).padStart(2, '0');
                    timeMinute = String(dateTime.getMinutes()).padStart(2, '0');
                } else {
                    // Extract directly from string (avoids timezone conversion)
                    const [, year, month, day, hour, minute] = dateTimeMatch;
                    date = `${year}-${month}-${day}`;
                    timeHour = hour;
                    timeMinute = minute;
                }
                
                // Calculate departure time (default: +2 hours from reservation time)
                const arrivalHour = parseInt(timeHour);
                const arrivalMinute = parseInt(timeMinute);
                let departureHour = arrivalHour + 2;
                let departureMinute = arrivalMinute;
                
                // Handle hour overflow (if departure time goes past 23:59, wrap to next day)
                if (departureHour >= 24) {
                    departureHour = departureHour % 24;
                }
                
                departureTimeHour = String(departureHour).padStart(2, '0');
                departureTimeMinute = String(departureMinute).padStart(2, '0');
                
                console.log('[AIChatbot] Parsed date/time:', {
                    date, timeHour, timeMinute, departureTimeHour, departureTimeMinute
                });
            } catch (parseError) {
                console.error('[AIChatbot] Error parsing reservation date:', parseError);
                this.showError('Lỗi xử lý thông tin ngày giờ. Vui lòng thử lại.');
                return;
            }

            // Create a form and submit to checkout
            setTimeout(() => {
                // Get CSRF token - REQUIRED for form submission
                const csrfToken = this.getCsrfToken();
                if (!csrfToken) {
                    console.error('[AIChatbot] CSRF token not found. Cannot submit form.');
                    this.showError('Lỗi bảo mật: Không tìm thấy token xác thực. Vui lòng làm mới trang và thử lại.');
                    return;
                }

                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/payment/customer/checkout';
                form.style.display = 'none';

                // Add form fields as required by the controller
                const fields = {
                    'date': date,
                    'timeHour': timeHour,
                    'timeMinute': timeMinute,
                    'departureTimeHour': departureTimeHour,
                    'departureTimeMinute': departureTimeMinute,
                    'numberOfGuests': String(reservationData.numberOfGuests),
                    '_csrf': csrfToken
                };

                // Add optional fields only if they have valid values
                if (reservationData.tableId && reservationData.tableId !== 'null' && reservationData.tableId !== null) {
                    fields['tableId'] = String(reservationData.tableId);
                }
                
                if (reservationData.notes) {
                    fields['notes'] = reservationData.notes;
                }

                console.log('[AIChatbot] Submitting checkout form with fields:', Object.keys(fields));

                // Add all fields to form
                for (const [key, value] of Object.entries(fields)) {
                    if (value !== null && value !== undefined && value !== '') {
                        const input = document.createElement('input');
                        input.type = 'hidden';
                        input.name = key;
                        input.value = String(value);
                        form.appendChild(input);
                    }
                }

                document.body.appendChild(form);
                form.submit();
            }, 2000);

        } catch (error) {
            console.error('[AIChatbot] Error parsing reservation checkout data:', error);
            this.showError('Sorry, there was an error processing your reservation. Please try again.');
        }
    }

    /**
     * Get CSRF token from meta tag, hidden input, or cookie
     */
    getCsrfToken() {
        // Try to get from meta tag first
        const metaTag = document.querySelector('meta[name="_csrf"]');
        if (metaTag) {
            const token = metaTag.getAttribute('content');
            if (token) {
                return token;
            }
        }

        // Try to get from hidden input in any form
        const csrfInput = document.querySelector('input[name="_csrf"]');
        if (csrfInput && csrfInput.value) {
            return csrfInput.value;
        }

        // Try to get from cookie (Spring Security stores it as XSRF-TOKEN)
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'XSRF-TOKEN' && value) {
                return decodeURIComponent(value);
            }
        }

        return null;
    }

    /**
     * Display a message in the chat
     */
    displayMessage(text, sender) {
        // Remove welcome message if present
        if (this.welcomeMsg && this.welcomeMsg.style.display !== 'none') {
            this.welcomeMsg.style.display = 'none';
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `ai-chatbot-message ${sender}`;

        const avatar = document.createElement('div');
        avatar.className = 'ai-chatbot-message-avatar';
        // Logo được tạo bằng CSS ::before, không cần text content

        const content = document.createElement('div');
        content.className = 'ai-chatbot-message-content';

        // Improve formatting for AI responses
        if (sender === 'bot') {
            content.innerHTML = this.formatAIResponse(text);
        } else {
            content.textContent = text;
        }

        messageDiv.appendChild(avatar);
        messageDiv.appendChild(content);

        // Insert before typing indicator
        this.messagesContainer.insertBefore(messageDiv, this.typingIndicator);

        // Scroll to bottom
        this.scrollToBottom();

        // Store in history
        this.messageHistory.push({ text, sender, timestamp: new Date() });
    }

    /**
     * Format AI response with better typography
     */
    formatAIResponse(text) {
        // Convert line breaks to <br>
        let formatted = text.replace(/\n/g, '<br>');

        // Convert bullet points
        formatted = formatted.replace(/^- (.+)$/gm, '<div style="margin-left: 8px;">• $1</div>');
        formatted = formatted.replace(/^\* (.+)$/gm, '<div style="margin-left: 8px;">• $1</div>');

        // Convert numbered lists
        formatted = formatted.replace(/^(\d+)\. (.+)$/gm, '<div style="margin-left: 8px;">$1. $2</div>');

        // Add emphasis for **bold** text
        formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');

        // Add emphasis for *italic* text
        formatted = formatted.replace(/\*(.+?)\*/g, '<em>$1</em>');

        return formatted;
    }

    /**
     * Show typing indicator
     */
    showTypingIndicator(show) {
        if (show) {
            this.typingIndicator.classList.add('show');
            this.scrollToBottom();
        } else {
            this.hideTypingIndicator();
        }
    }

    /**
     * Hide typing indicator
     */
    hideTypingIndicator() {
        this.typingIndicator.classList.remove('show');
    }

    /**
     * Show error message
     */
    showError(message) {
        this.displayMessage(message, 'bot');
    }

    /**
     * Scroll messages to bottom
     */
    scrollToBottom() {
        setTimeout(() => {
            this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
        }, 100);
    }

    /**
     * Generate UUID for guest sessions
     */
    generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
}

// Initialize chatbot when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('[AIChatbot] DOM loaded, initializing chatbot...');

    // Check if SockJS and Stomp are loaded
    if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.error('[AIChatbot] SockJS or Stomp.js not loaded. Please include the libraries.');
        return;
    }

    // Initialize chatbot
    window.aiChatbot = new AIChatbot();
});
