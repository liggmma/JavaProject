<%-- 
    Document   : header
    Created on : Jul 6, 2025, 6:41:29 PM
    Author     : ADMIN
--%>

<%@page import="service.ConversationService"%>
<%@page import="model.Conversations"%>
<%@page import="model.Notifications"%>
<%@page import="model.Users"%>
<%@page import="service.CategoryService"%>
<%@page import="java.util.ArrayList"%>
<%@page import="model.Categories"%>
<%@page import="java.util.List"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<head>
    <link rel="stylesheet" href="./css/style-prefix.css">
</head>
<header>

    <div class="header-top">

        <div class="container">

            <ul class="header-social-container">

                <li>
                    <a href="#" class="social-link">
                        <ion-icon name="logo-facebook"></ion-icon>
                    </a>
                </li>

                <li>
                    <a href="#" class="social-link">
                        <ion-icon name="logo-twitter"></ion-icon>
                    </a>
                </li>

                <li>
                    <a href="#" class="social-link">
                        <ion-icon name="logo-instagram"></ion-icon>
                    </a>
                </li>

                <li>
                    <a href="#" class="social-link">
                        <ion-icon name="logo-linkedin"></ion-icon>
                    </a>
                </li>

            </ul>

            <div class="header-alert-news">
                <p>
                    <b>HAPPY SHOPPING</b>
                    Enjoy your shopping
                </p>
            </div>

            <div class="header-top-actions">

                <select name="currency">

                    <option value="usd">USD &dollar;</option>
                    <option value="eur">EUR &euro;</option>

                </select>

                <select name="language">

                    <option value="en-US">English</option>
                    <option value="es-ES">Espa&ntilde;ol</option>
                    <option value="fr">Fran&ccedil;ais</option>

                </select>

            </div>

        </div>

    </div>

    <div class="header-main">

        <div class="container">

            <a href="home" class="header-logo">
                <img src="./images/logo.png" alt="ShopGO logo" width="120" height="60">
            </a>


            <div class="header-search-container">
                <form id="smartSearchForm" enctype="multipart/form-data" class="search-form">
                    <div class="search-input-wrapper" style="display: flex; align-items: center; gap: 10px; position: relative;">

                        <!-- Nút upload ảnh -->
                        <button type="button" id="image-upload-btn" class="upload-icon" style="cursor: pointer;">
                            <ion-icon name="image-outline"></ion-icon>
                        </button>

                        <!-- Ảnh preview -->
                        <img id="preview-image" src="" alt="Preview"
                             style="height: 40px; display: none; border-radius: 4px; border: 1px solid #ccc;" />

                        <!-- Nút xóa ảnh -->
                        <button type="button" id="remove-image-btn" title="Remove image"
                                style="display: none; font-size: 18px; background: none; border: none; cursor: pointer;">
                            ✖
                        </button>

                        <!-- Input tìm kiếm văn bản -->
                        <input type="search" name="search" id="text-input" class="search-field"
                               placeholder="Enter your product name..." required />

                        <!-- Input file (ẩn) -->
                        <input type="file" name="image" id="image-upload" accept="image/*" style="display: none;" />

                        <!-- Nút submit -->
                        <button class="search-btn" type="submit">
                            <ion-icon name="search-outline"></ion-icon>
                        </button>
                    </div>
                </form>
            </div>

            <div class="header-user-actions">

                <%
                    ConversationService conversationService = new ConversationService();
                    int unreadCount1 = 0;
                    if (session.getAttribute("user") != null) {
                        Users user = (Users) session.getAttribute("user");
                        List<Conversations> conversations = conversationService.getConversationForUser(user.getUserId());
                        for (Conversations conv : conversations) {
                            unreadCount1 = unreadCount1 + conv.getShopUnReadCount();
                        }
                    }
                    request.setAttribute("unreadCount1", unreadCount1);

                %>
                <!-- Chat -->
                <a href="chat">
                    <button class="action-btn" title="Tin nhắn">
                        <ion-icon name="chatbubbles-outline"></ion-icon>
                        <span class="count">${unreadCount1}</span>
                    </button>
                </a>

                <%                    int unreadCount2 = 0;
                    if (session.getAttribute("user") != null) {
                        Users user = (Users) session.getAttribute("user");
                        if (user.getNotificationsList() != null) {
                            for (Notifications n : user.getNotificationsList()) {
                                if (n.getIsRead() == null || !n.getIsRead()) {
                                    unreadCount2++;
                                }
                            }
                        }
                    }
                    request.setAttribute("unreadCount2", unreadCount2);
                %>

                <a href="notifications">
                    <button class="action-btn" title="Thông báo">
                        <ion-icon name="notifications-outline"></ion-icon>
                        <span class="count">${unreadCount2}</span>
                    </button>
                </a>

                <a href="wishList">
                    <button class="action-btn">
                        <ion-icon name="heart-outline"></ion-icon>
                        <span class="count">
                            <c:choose>
                                <c:when test="${not empty sessionScope.user}">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.user.wishlists && not empty sessionScope.user.wishlists.wishlistItemsList}">
                                            ${sessionScope.user.wishlists.wishlistItemsList.size()}
                                        </c:when>
                                        <c:otherwise>0</c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>0</c:otherwise>
                            </c:choose>
                        </span>
                    </button>
                </a>

                <a href="cart">
                    <button class="action-btn">
                        <ion-icon name="bag-handle-outline"></ion-icon>
                        <span class="count">
                            <c:choose>
                                <c:when test="${not empty sessionScope.user}">
                                    ${sessionScope.user.carts.cartItemsList.size()}
                                </c:when>
                                <c:otherwise>
                                    0
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </button>
                </a>
                <button class="action-btn" onclick="window.location.href = 'profile'">
                    <ion-icon name="person-outline"></ion-icon>
                </button>

            </div>

        </div>

    </div>
    <%
        CategoryService categoryService = new CategoryService();
        List<Categories> categoryListMain = new ArrayList<>();
        List<Categories> allCategory = new ArrayList<>();
        categoryListMain = categoryService.getMainCate();
        allCategory = categoryService.getAll();

        request.setAttribute("categories", categoryListMain);
        request.setAttribute("allCategories", allCategory);
    %>
    <nav class="desktop-navigation-menu">

        <div class="container">

            <ul class="desktop-menu-category-list">

                <li class="menu-category">
                    <a href="home" class="menu-title">Home</a>
                </li>

                <li class="menu-category">
                    <a href="#" class="menu-title">Categories</a>
                    <div class="dropdown-panel">
                        <c:forEach var="cata" items="${categories}">
                            <ul class="dropdown-panel-list">

                                <li class="menu-title">
                                    <a href="category?id=${cata.categoryId}">${cata.name}</a>
                                </li>

                                <c:forEach var="sub" items="${allCategories}">
                                    <c:if test="${sub.parentId.categoryId == cata.categoryId}">
                                        <li class="panel-list-item">
                                            <a href="category?id=${sub.categoryId}">${sub.name}</a>
                                        </li>
                                    </c:if>
                                </c:forEach>

                                <li class="panel-list-item">
                                    <!-- Có thể thêm ảnh minh họa tại đây nếu cần -->
                                </li>

                            </ul>
                        </c:forEach>
                    </div>
                </li>

                <li class="menu-category">
                    <a href="#" class="menu-title">Men's</a>

                    <ul class="dropdown-list">

                        <li class="dropdown-item">
                            <a href="#">Shirt</a>
                        </li>

                        <li class="dropdown-item">
                            <a href="#">Shorts & Jeans</a>
                        </li>

                        <li class="dropdown-item">
                            <a href="#">Safety Shoes</a>
                        </li>

                        <li class="dropdown-item">
                            <a href="#">Wallet</a>
                        </li>

                    </ul>
                </li>



                <li class="menu-category">
                    <a href="#" class="menu-title">Blog</a>
                </li>

            </ul>

        </div>

    </nav>

</header>

<!-- Ionicons -->
<script type="module" src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>
<script nomodule src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>
<script>
                    const form = document.getElementById("smartSearchForm");
                    const imageInput = document.getElementById("image-upload");
                    const imageBtn = document.getElementById("image-upload-btn");
                    const removeImageBtn = document.getElementById("remove-image-btn");
                    const textInput = document.getElementById("text-input");
                    const previewImage = document.getElementById("preview-image");

                    // Nhấn nút icon ảnh → chọn file ảnh
                    imageBtn.addEventListener("click", () => imageInput.click());

                    // Khi chọn ảnh xong
                    imageInput.addEventListener("change", function () {
                        if (imageInput.files && imageInput.files[0]) {
                            const reader = new FileReader();
                            reader.onload = function (e) {
                                previewImage.src = e.target.result;
                                previewImage.style.display = "block";
                                removeImageBtn.style.display = "inline-block";
                                textInput.disabled = true;
                                textInput.placeholder = "Đã chọn ảnh...";
                                textInput.value = "";
                            };
                            reader.readAsDataURL(imageInput.files[0]);
                        }
                    });

                    // Xoá ảnh đã chọn
                    removeImageBtn.addEventListener("click", function () {
                        imageInput.value = "";
                        previewImage.src = "";
                        previewImage.style.display = "none";
                        removeImageBtn.style.display = "none";
                        textInput.disabled = false;
                        textInput.placeholder = "Enter your product name...";
                    });

                    // Trước khi submit → đổi action và method tuỳ loại tìm kiếm
                    form.addEventListener("submit", function (e) {
                        if (imageInput.files.length > 0) {
                            form.action = "search";
                            form.method = "post";
                        } else {
                            form.action = "search";
                            form.method = "get";
                        }
                    });
</script>
