<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>${category.name} - Products</title>
        <link rel="stylesheet" href="./css/style-prefix.css">
        <style>
            .container-main {
                max-width: 1100px;
                margin: auto;
                padding: 40px 20px;
                font-family: 'Poppins', sans-serif;
            }

            .section-title {
                font-size: 24px;
                font-weight: 700;
                border-bottom: 2px solid #eee;
                margin-bottom: 30px;
            }

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

        <jsp:include page="./sub/header.jsp" />

        <main class="container-main">

            <h2 class="section-title">Category: ${category.name}</h2>

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
                                <button class="btn-action"><ion-icon name="heart-outline"></ion-icon></button>
                                <button class="btn-action" onclick="window.location.href = 'product?id=${product.productId}'"><ion-icon name="eye-outline"></ion-icon></button>
                                <button class="btn-action"><ion-icon name="bag-add-outline"></ion-icon></button>
                            </div>
                        </div>

                        <div class="showcase-content">
                            <a href="category?id=${product.categoryId.categoryId}" class="showcase-category">${product.categoryId.name}</a>

                            <h3><a href="product?id=${product.productId}" class="showcase-title">${product.name}</a></h3>

                            <!-- Tính trung bình rating -->
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

        </main>

        <jsp:include page="./sub/footer.jsp" />

        <script type="module" src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>
        <script nomodule src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>
    </body>
</html>
