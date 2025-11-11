<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Search Results</title>
        <link rel="stylesheet" href="./css/style-prefix.css">
        <style>
            .container_main {
                max-width: 1200px;
                margin: auto;
                padding: 40px 20px;
                font-family: 'Poppins', sans-serif;
            }

            .section-title {
                font-size: 24px;
                font-weight: 600;
                margin-bottom: 30px;
            }

            .product-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
                gap: 30px;
                justify-content: center;
                align-items: start;
            }

            .product-card {
                background: #fff;
                border-radius: 16px;
                overflow: hidden;
                box-shadow: 0 8px 24px rgba(0,0,0,0.05);
                transition: 0.3s ease-in-out;
                text-align: center;
            }

            .product-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 12px 28px rgba(0,0,0,0.1);
            }

            .product-card img {
                width: 100%;
                height: 220px;
                object-fit: cover;
            }

            .product-info {
                padding: 16px;
            }

            .product-name {
                font-size: 16px;
                font-weight: 600;
                margin-bottom: 6px;
                color: #333;
                text-decoration: none;
                display: block;
                height: 40px;
                overflow: hidden;
            }

            .product-price {
                color: var(--main-pink);
                font-weight: 600;
                font-size: 16px;
            }

            .rating-stars {
                margin-top: 8px;
                color: var(--sandy-brown);
            }

            .no-result {
                text-align: center;
                font-size: 18px;
                margin-top: 60px;
                color: gray;
            }
        </style>
    </head>
    <body>

        <jsp:include page="./sub/header.jsp" />

        <main class="container_main">
            <c:choose>
                <c:when test="${not empty products}">
                    <h2 class="section-title">Search Results for: "<c:out value="${param.search}" />"</h2>
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
                </c:when>
                <c:otherwise>
                    <div class="no-result">
                        No products found for "<c:out value="${param.search}" />"
                    </div>
                </c:otherwise>
            </c:choose>

        </main>

        <jsp:include page="./sub/footer.jsp" />


    </body>
</html>
