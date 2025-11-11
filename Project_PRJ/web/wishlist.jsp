<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="./sub/header.jsp" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Danh sách yêu thích</title>
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

            .wishlist-container {
                max-width: 1200px;
                margin: auto;
            }

            h1 {
                text-align: center;
                font-size: 28px;
                margin-bottom: 30px;
                color: #111827;
            }

            .wishlist-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
                gap: 30px;
                justify-content: center;
                align-items: start;
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
            }

            .product-img {
                width: 100%;
                display: block;
                transition: opacity 0.3s ease;
            }

            .product-img.hover {
                position: absolute;
                top: 0;
                left: 0;
                opacity: 0;
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

        <div class="wishlist-container">
            <h1><i class="fas fa-heart"></i> Danh sách yêu thích</h1>

            <c:choose>
                <c:when test="${empty wishlists}">
                    <div class="empty-message">
                        <i class="fas fa-box-open"></i> Bạn chưa có sản phẩm yêu thích nào.
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="wishlist-grid">
                        <c:forEach var="item" items="${wishlists}">
                            <c:set var="product" value="${item.productId}" />
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
                                        <!-- Xóa khỏi wishlist -->
                                        <form action="wishList" method="post">
                                            <input type="hidden" name="action" value="delete" />
                                            <input type="hidden" name="wishlistItemId" value="${item.wishlistItemId}" />
                                            <button type="submit" class="btn-action" title="Xóa khỏi danh sách yêu thích">
                                                <ion-icon name="heart-dislike-outline"></ion-icon>
                                            </button>
                                        </form>

                                        <!-- Xem chi tiết -->
                                        <button class="btn-action" onclick="window.location.href = 'product?id=${product.productId}'" title="Xem chi tiết">
                                            <ion-icon name="eye-outline"></ion-icon>
                                        </button>

                                        <!-- Add to cart -->
                                        <form method="post" action="addToCart">
                                            <input type="hidden" name="productId" value="${product.productId}" />
                                            <button type="submit" class="btn-action" title="Thêm vào giỏ hàng">
                                                <ion-icon name="bag-add-outline"></ion-icon>
                                            </button>
                                        </form>
                                    </div>
                                </div>

                                <div class="showcase-content">
                                    <a href="category?id=${product.categoryId.categoryId}" class="showcase-category">
                                        ${product.categoryId.name}
                                    </a>

                                    <h3>
                                        <a href="product?id=${product.productId}" class="showcase-title">
                                            ${product.name}
                                        </a>
                                    </h3>

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
                                        <p class="price">${product.basePrice}₫</p>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

    </body>
</html>

<jsp:include page="./sub/footer.jsp" />
