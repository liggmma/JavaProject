<%-- 
    Document   : home
    Created on : Jul 5, 2025, 5:47:42 PM
    Author     : ADMIN
--%>

<%@page import="model.ProductImages"%>
<%@page import="java.util.List"%>
<%@page import="model.Products"%>
<%@page import="model.Products"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ShopGO - eCommerce Website</title>

        <!--
          - favicon
        -->
        <link rel="shortcut icon" href="./images/logo/favicon.ico" type="image/x-icon">

        <!--
          - custom css link
        -->
        <link rel="stylesheet" href="./css/style-prefix.css">

        <!--
          - google font link
        -->
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700;800;900&display=swap"
              rel="stylesheet">

        <style>
            .product-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr) !important;
                gap: 30px;
            }

            .showcase {
                background: #fff;
                border-radius: 16px;
                overflow: hidden;
                box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
                transition: transform 0.3s ease;
            }

            .showcase:hover {
                transform: translateY(-4px);
            }

            .showcase-banner {
                position: relative;
                overflow: hidden;
                height: 260px; /* hoặc 240px tùy ý bạn */
                display: flex;
                align-items: center;
                justify-content: center;
                background-color: #f9fafb;
            }

            .product-img {
                height: 100%;
                width: 100%;
                object-fit: cover;
            }

            .product-img.hover {
                position: absolute;
                top: 0;
                left: 0;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .showcase:hover .product-img.hover {
                opacity: 1;
            }

            .showcase-actions {
                position: absolute;
                top: 10px;
                right: 10px;
                display: flex;
                flex-direction: column;
                gap: 8px;
            }

            .btn-action {
                background: #fff;
                border: none;
                padding: 8px;
                border-radius: 50%;
                box-shadow: 0 2px 6px rgba(0,0,0,0.1);
                cursor: pointer;
                font-size: 16px;
                transition: background 0.2s ease;
            }

            .btn-action:hover {
                background: #ff6f91;
                color: #fff;
            }

            .showcase-content {
                padding: 16px;
            }

            .showcase-category {
                font-size: 14px;
                color: #ff6f91;
                text-decoration: none;
            }

            .showcase-title {
                font-size: 16px;
                color: #111827;
                text-decoration: none;
                font-weight: 600;
                display: block;
                margin-top: 6px;
                margin-bottom: 8px;
            }

            .showcase-rating {
                color: #fbbf24;
                margin-bottom: 8px;
            }

            .price-box .price {
                font-size: 18px;
                font-weight: bold;
            }

            .empty-message {
                text-align: center;
                font-size: 16px;
                color: #6b7280;
                margin-top: 40px;
            }

            .showcase-rating ion-icon {
                display: inline-block;
                vertical-align: middle;
            }
        </style>
    </head>

    <body>

        <!--
          - HEADER
        -->

        <jsp:include page="./sub/header.jsp" />

        <main>

            <!--
              - BANNER
            -->

            <!--    <div class="banner">
            
                  <div class="container">
            
                    <div class="slider-container has-scrollbar">
            
                      <div class="slider-item">
            
                        <img src="./assets/images/banner-1.jpg" alt="women's latest fashion sale" class="banner-img">
            
                        <div class="banner-content">
            
                          <p class="banner-subtitle">Trending item</p>
            
                          <h2 class="banner-title">Women's latest fashion sale</h2>
            
                          <p class="banner-text">
                            starting at &dollar; <b>20</b>.00
                          </p>
            
                          <a href="#" class="banner-btn">Shop now</a>
            
                        </div>
            
                      </div>
            
                      <div class="slider-item">
            
                        <img src="./assets/images/banner-2.jpg" alt="modern sunglasses" class="banner-img">
            
                        <div class="banner-content">
            
                          <p class="banner-subtitle">Trending accessories</p>
            
                          <h2 class="banner-title">Modern sunglasses</h2>
            
                          <p class="banner-text">
                            starting at &dollar; <b>15</b>.00
                          </p>
            
                          <a href="#" class="banner-btn">Shop now</a>
            
                        </div>
            
                      </div>
            
                      <div class="slider-item">
            
                        <img src="./assets/images/banner-3.jpg" alt="new fashion summer sale" class="banner-img">
            
                        <div class="banner-content">
            
                          <p class="banner-subtitle">Sale Offer</p>
            
                          <h2 class="banner-title">New fashion summer sale</h2>
            
                          <p class="banner-text">
                            starting at &dollar; <b>29</b>.99
                          </p>
            
                          <a href="#" class="banner-btn">Shop now</a>
            
                        </div>
            
                      </div>
            
                    </div>
            
                  </div>
            
                </div>-->




            <!--
              - CATEGORY
            -->
            <div class="category">
                <div class="container">

                    <h2 class="title">Categories</h2>

                    <div class="category-item-container has-scrollbar">

                        <c:forEach var="category" items="${categories}">
                            <div class="category-item">

                                <div class="category-img-box">
                                    <c:choose>
                                        <c:when test="${category.logoUrl != null}">
                                            <img src="<c:out value='${category.logoUrl}'/>" width="30">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="./images/icons/default.png" width="30">
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="category-content-box">

                                    <div class="category-content-flex">
                                        <h3 class="category-item-title">${category.name}</h3>

                                    </div>

                                    <a href="category?id=${category.categoryId}" class="category-btn">Show all</a>

                                </div>

                            </div>
                        </c:forEach>
                    </div>

                </div>
            </div>





            <!--
              - PRODUCT
            -->

            <div class="product-container">
                <div class="container">
                    <div class="product-box">
                        <div class="product-main">
                            <h2 class="title">Recommend</h2>

                            <div class="product-grid">
                                <c:forEach var="product" items="${products}">
                                    <div class="showcase">
                                        <div class="showcase-banner">
                                            <!-- Ảnh chính -->
                                            <c:forEach var="img" items="${product.productImagesList}" varStatus="loop">
                                                <c:if test="${loop.index == 0}">
                                                    <img src="${img.imageUrl}" alt="${product.name}" width="300" class="product-img default" />
                                                </c:if>
                                                <c:if test="${loop.index == 1}">
                                                    <img src="${img.imageUrl}" alt="${product.name}" width="300" class="product-img hover" />
                                                </c:if>
                                            </c:forEach>

                                            <div class="showcase-actions">
                                                <!-- Thêm vào wishlist -->
                                                <form action="wishList" method="post">
                                                    <input type="hidden" name="action" value="add">
                                                    <input type="hidden" name="productId" value="${product.productId}" />
                                                    <button type="submit" class="btn-action">
                                                        <ion-icon name="heart-outline"></ion-icon>
                                                    </button>
                                                </form>


                                                <!-- Xem chi tiết -->
                                                <button class="btn-action" onclick="window.location.href = 'product?id=${product.productId}'" title="View product">
                                                    <ion-icon name="eye-outline"></ion-icon>
                                                </button>

                                                <!-- Add to cart -->
                                                <button class="btn-action" onclick="window.location.href = 'cart?action=add&productId=${product.productId}&quantity=1'" title="Add to cart">
                                                    <ion-icon name="bag-add-outline"></ion-icon>
                                                </button>
                                            </div>
                                        </div>

                                        <div class="showcase-content">
                                            <a href="category?id=${product.categoryId.categoryId}" class="showcase-category">${product.categoryId.name}</a>
                                            <h3><a href="product?id=${product.productId}" class="showcase-title">${product.name}</a></h3>

                                            <!-- Rating -->
                                            <c:set var="avgRating" value="${product.averageRating}" />
                                            <div class="showcase-rating">
                                                <c:forEach begin="1" end="5" var="i">
                                                    <c:choose>
                                                        <c:when test="${i <= avgRating}">
                                                            <ion-icon name="star"></ion-icon>
                                                            </c:when>
                                                            <c:when test="${i - avgRating <= 0.5}">
                                                            <ion-icon name="star-half-outline"></ion-icon>
                                                            </c:when>
                                                            <c:otherwise>
                                                            <ion-icon name="star-outline"></ion-icon>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:forEach>
                                            </div>

                                            <div class="price-box">
                                                <p class="price">$${product.basePrice}</p>
                                            </div>
                                        </div>

                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
            </div>



            <!--
              - BLOG
            -->

            <!--    <div class="blog">
            
                  <div class="container">
            
                    <h2 class="title">Blog</h2>
            
                    <div class="blog-container has-scrollbar">
            
                      <div class="blog-card">
            
                        <a href="#">
                          <img src="./assets/images/blog-1.jpg" alt="Clothes Retail KPIs 2021 Guide for Clothes Executives" width="300" class="blog-banner">
                        </a>
            
                        <div class="blog-content">
            
                          <a href="#" class="blog-category">Fashion</a>
            
                          <a href="#">
                            <h3 class="blog-title">Clothes Retail KPIs 2021 Guide for Clothes Executives.</h3>
                          </a>
            
                          <p class="blog-meta">
                            By <cite>Mr Admin</cite> / <time datetime="2022-04-06">Apr 06, 2022</time>
                          </p>
            
                        </div>
            
                      </div>
            
                      <div class="blog-card">
                      
                        <a href="#">
                          <img src="./assets/images/blog-2.jpg" alt="Curbside fashion Trends: How to Win the Pickup Battle."
                            class="blog-banner" width="300">
                        </a>
                      
                        <div class="blog-content">
                      
                          <a href="#" class="blog-category">Clothes</a>
                      
                          <h3>
                            <a href="#" class="blog-title">Curbside fashion Trends: How to Win the Pickup Battle.</a>
                          </h3>
                      
                          <p class="blog-meta">
                            By <cite>Mr Robin</cite> / <time datetime="2022-01-18">Jan 18, 2022</time>
                          </p>
                      
                        </div>
                      
                      </div>
            
                      <div class="blog-card">
                      
                        <a href="#">
                          <img src="./assets/images/blog-3.jpg" alt="EBT vendors: Claim Your Share of SNAP Online Revenue."
                            class="blog-banner" width="300">
                        </a>
                      
                        <div class="blog-content">
                      
                          <a href="#" class="blog-category">Shoes</a>
                      
                          <h3>
                            <a href="#" class="blog-title">EBT vendors: Claim Your Share of SNAP Online Revenue.</a>
                          </h3>
                      
                          <p class="blog-meta">
                            By <cite>Mr Selsa</cite> / <time datetime="2022-02-10">Feb 10, 2022</time>
                          </p>
                      
                        </div>
                      
                      </div>
            
                      <div class="blog-card">
                      
                        <a href="#">
                          <img src="./assets/images/blog-4.jpg" alt="Curbside fashion Trends: How to Win the Pickup Battle."
                            class="blog-banner" width="300">
                        </a>
                      
                        <div class="blog-content">
                      
                          <a href="#" class="blog-category">Electronics</a>
                      
                          <h3>
                            <a href="#" class="blog-title">Curbside fashion Trends: How to Win the Pickup Battle.</a>
                          </h3>
                      
                          <p class="blog-meta">
                            By <cite>Mr Pawar</cite> / <time datetime="2022-03-15">Mar 15, 2022</time>
                          </p>
                      
                        </div>
                      
                      </div>
            
                    </div>
            
                  </div>
            
                </div>-->

        </main>


        <jsp:include page="./sub/footer.jsp" />







        <!--
          - custom js link
        -->
        <script src="./js/script.js"></script>

        <!--
          - ionicon link
        -->
        <script type="module" src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>
        <script nomodule src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>
        <!-- Chatbox -->
        <div id="chatbox" class="minimized">

            <div id="chat-header">
                <img src="images/abc.png" alt="AI Icon" class="chat-icon">
                <div id="title">AI Trợ lý bán hàng</div>
                <div style="margin-left:auto; display:flex; gap:10px;">
                    <span id="toggle-expand" onclick="toggleExpand(event)">⛶</span>
                    <span id="toggle-icon" onclick="toggleChatbox()">💬</span>
                </div>
            </div>
            <div id="chat-body">
                <div id="chat-messages"></div>
                <div id="chat-input-box">

                    <input type="text" id="chat-input" placeholder="Nhập câu hỏi..." onkeydown="handleKeyPress(event)" />
                    <button onclick="sendMessage()">➤</button>
                </div>
            </div>
        </div>
        <style>
            #chatbox {
                position: fixed;
                bottom: 20px;
                right: 20px;
                width: 320px;
                font-family: 'Segoe UI', sans-serif;
                z-index: 9999;
                border-radius: 12px;
                box-shadow: 0 0 12px rgba(0,0,0,0.2);
                overflow: hidden;
                background: white;
                border: 1px solid #ccc;
                display: flex;
                flex-direction: column;
            }

            #chat-header {
                background-image: url('images/headerNew.jpg'); /* <-- đường dẫn ảnh nền */
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
                color: white;
                padding: 10px;
                font-weight: bold;
                cursor: pointer;
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-top-left-radius: 12px;
                border-top-right-radius: 12px;
            }
            .chat-icon {
                width: 30px;
                height: 30px;
                margin-right: 8px;
            }


            #toggle-icon {
                font-size: 18px;
                margin-left: 10px;
                user-select: none;
            }
            #chatbox.expanded {
                width: 50vw !important;
                height: 100vh !important;
                right: 0 !important;
                bottom: 0 !important;
                border-radius: 0 !important;
            }

            #chatbox.expanded #chat-body {
                height: calc(100vh - 50px);
                background-image: url('images/expandNew.jpg'); /* <-- đường dẫn ảnh nền */
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
            }

            #toggle-expand {
                font-size: 18px;
                cursor: pointer;
                user-select: none;
            }

            #chat-body {
                display: flex;
                flex-direction: column;
                height: 360px;
                transition: all 0.3s ease;
                background-image: url('images/robotBG.jpeg'); /* <-- đường dẫn ảnh nền */
                background-size: cover;
                background-position: center;
                background-repeat: no-repeat;
            }

            #chatbox.minimized #chat-body {
                display: none;
            }
            #chatbox.minimized #toggle-expand {
                display: none;
            }

            #chat-messages {
                flex: 1;
                padding: 10px;
                overflow-y: auto;
                font-size: 14px;
                display: flex;
                flex-direction: column;
                gap: 6px;

            }

            .message {
                padding: 10px 14px;
                border-radius: 16px;
                max-width: 80%;
                word-wrap: break-word;
                line-height: 1.4;
                font-size: 14px;
                display: inline-block;
                clear: both;
                margin: 4px 0;
            }

            .user {
                background: linear-gradient(135deg, purple, green, pink); /* Xanh, hồng, vàng */
                background-size: 200% 200%;
                animation: gradientAnimation 10s ease infinite;
                color: white;
                align-self: flex-end;
                border-bottom-right-radius: 0;
                margin-left: auto;
                text-align: right;
            }

            .bot {
                background: linear-gradient(135deg, #2196f3, #e91e63, #ffeb3b); /* Xanh, hồng, vàng */
                background-size: 200% 200%;
                animation: gradientAnimation 10s ease infinite;
                color: white;
                align-self: flex-start;
                border-bottom-left-radius: 0;
                margin-right: auto;
                text-align: left;
            }




            #chat-input-box {
                display: flex;
                align-items: center;
                justify-content: space-between;
                background: transparent;
                border-radius: 999px;
                padding: 8px 12px;
                width: 100%;
                max-width: 800px;

            }

            #chat-input {
                flex: 1;
                background-color: #333;
                border-radius: 999px;
                overflow-y: auto;
                padding: 10px 20px;
                border: none;
                color: white;
                font-size: 16px;
                outline: none;
                flex-direction: column;
            }

            #chat-input:focus {
                outline: none;
            }

            #chat-input-box button {
                background: transparent;
                border: none;
                color: #000;
                font-size: 18px;
                border-radius: 50%;
                width: 32px;
                height: 32px;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
            }
            #chat-input-box button:hover {
                background: rgba(255, 255, 255, 0.1);
            }

            .typing-indicator {
                display: inline-block;
                padding: 10px 14px;
                border-radius: 16px;
                background: #eee;
                color: #333;
                font-size: 14px;
                max-width: 60%;
                align-self: flex-start;
                border-bottom-left-radius: 0;
                margin-right: auto;
                text-align: left;
            }

            .typing-indicator span {
                display: inline-block;
                width: 6px;
                height: 6px;
                margin: 0 2px;
                background-color: #333;
                border-radius: 50%;
                animation: bounce 1.2s infinite ease-in-out;
            }

            .typing-indicator span:nth-child(2) {
                animation-delay: 0.2s;
            }

            .typing-indicator span:nth-child(3) {
                animation-delay: 0.4s;
            }

            @keyframes bounce {
                0%, 80%, 100% {
                    transform: scale(0.8);
                    opacity: 0.3;
                }
                40% {
                    transform: scale(1.4);
                    opacity: 1;
                }
            }

        </style>

        <script>
            let typingIndicator = null;

            async function sendMessage() {
                const inputField = document.getElementById('chat-input');
                const message = inputField.value.trim();
                if (!message)
                    return;

                appendMessage(message, 'user');
                inputField.value = '';
                inputField.disabled = true;

                showTypingIndicator(); 

                try {
                    const response = await fetch('<c:url value="/chatAI"/>', {
                        method: 'POST',
                        headers: {'Content-Type': 'application/json'},
                        body: JSON.stringify({message})
                    });

                    const data = await response.json();
                    removeTypingIndicator();    
                    appendMessage(data.reply, 'bot');
                } catch (error) {
                    removeTypingIndicator();
                    appendMessage("⚠️ Không thể kết nối tới AI Agent.", 'bot');
                    console.error(error);
                } finally {
                    inputField.disabled = false;
                    inputField.focus();
                }
            }

            function showTypingIndicator() {
                const messagesDiv = document.getElementById('chat-messages');

                typingIndicator = document.createElement('div');
                typingIndicator.classList.add('typing-indicator');

                typingIndicator.innerHTML = `
            <span></span><span></span><span></span>
        `;

                messagesDiv.appendChild(typingIndicator);
                messagesDiv.scrollTop = messagesDiv.scrollHeight;
            }

            function removeTypingIndicator() {
                if (typingIndicator && typingIndicator.parentNode) {
                    typingIndicator.parentNode.removeChild(typingIndicator);
                    typingIndicator = null;
                }
            }

            function appendMessage(text, sender) {
                const messagesDiv = document.getElementById('chat-messages');
                const msg = document.createElement('div');

                msg.classList.add('message');
                if (sender === 'user' || sender === 'bot') {
                    msg.classList.add(sender);
                }


                let formatted = text
                        .replace(/\*\*\*(.*?)\*\*\*/g, '<strong><em>$1</em></strong>') // ***bold italic***
                        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')              // **bold**
                        .replace(/\*(.*?)\*/g, '<em>$1</em>')                          // *italic*
                        .replace(/\n/g, "<br>");                                       // xuống dòng

                msg.innerHTML = formatted;

                messagesDiv.appendChild(msg);
                messagesDiv.scrollTop = messagesDiv.scrollHeight;
            }





            function handleKeyPress(event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    sendMessage();
                }
            }

            function toggleChatbox() {
                const icon = document.getElementById('toggle-expand');
                const chatbox = document.getElementById('chatbox');
                const toggleIcon = document.getElementById('toggle-icon');

                if (chatbox.classList.contains('expanded')) {
                    // Nếu đang phóng to → thu nhỏ và xóa trạng thái expanded
                    chatbox.classList.remove('expanded');
                    chatbox.classList.add('minimized');
                    toggleIcon.textContent = '💬';
                    icon.textContent = '⛶'; // biểu tượng phóng to
                } else {
                    // Nếu không → chuyển trạng thái giữa thu nhỏ ↔ bình thường
                    chatbox.classList.toggle('minimized');
                    toggleIcon.textContent = chatbox.classList.contains('minimized') ? '💬' : '✖️';
                }
            }


            function toggleExpand(event) {
                event.stopPropagation(); // Ngăn không gọi toggleChatbox()
                const toggleIcon = document.getElementById('toggle-icon');
                const chatbox = document.getElementById('chatbox');
                const isExpanded = chatbox.classList.contains('expanded');
                const icon = document.getElementById('toggle-expand');

                if (isExpanded) {
                    // Nếu đang phóng to → thu nhỏ lại
                    chatbox.classList.remove('expanded');
                    icon.textContent = '⛶'; // biểu tượng phóng to
                } else {
                    // Nếu đang thu nhỏ → phóng to
                    chatbox.classList.remove('minimized'); // đảm bảo mở rộng nếu đang thu nhỏ
                    chatbox.classList.add('expanded');
                    icon.textContent = '➖️'; // biểu tượng thu nhỏ
                    // Đổi biểu tượng toggle-icon thành 'X'
                    document.getElementById('toggle-icon').textContent = '✖';
                }
            }

            window.onload = () => {
                fetch("chatAI")
                        .then(res => res.json())
                        .then(data => {
                            const history = data.history || [];
                            history.forEach(msg => {
                                appendMessage(msg.text, msg.sender);
                            });
                        });
            };


        </script>


    </body>

</html>