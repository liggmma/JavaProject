<%@page import="model.Products"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>${product.name} - Product Detail</title>
        <link rel="stylesheet" href="./css/style-prefix.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --error-red: #ff4d4d;
                --link-color: #6366f1;
                --link-hover: #4338ca;
            }

            .container-main {
                max-width: 1100px;
                margin: auto;
                padding: 40px 20px;
                font-family: 'Poppins', sans-serif;
            }

            .product-detail {
                display: flex;
                flex-wrap: wrap;
                gap: 40px;
            }

            .image-carousel {
                position: relative;
                max-width: 460px;
                flex: 1;
                border-radius: 16px;
                overflow: hidden;
                background: #f5f5f5;
            }

            .carousel-images img {
                width: 100%;
                display: none;
                border-radius: 16px;
            }

            .carousel-images img.active {
                display: block;
            }

            .carousel-nav {
                position: absolute;
                top: 50%;
                transform: translateY(-50%);
                font-size: 24px;
                background-color: rgba(0,0,0,0.5);
                color: white;
                border: none;
                padding: 10px;
                cursor: pointer;
                border-radius: 50%;
            }

            .carousel-prev {
                left: 10px;
            }
            .carousel-next {
                right: 10px;
            }

            .thumbnail-list {
                margin-top: 10px;
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
            }

            .thumbnail-list img {
                width: 60px;
                height: 60px;
                object-fit: cover;
                border-radius: 8px;
                cursor: pointer;
                border: 2px solid transparent;
                transition: border 0.3s;
            }

            .thumbnail-list img.selected {
                border-color: var(--main-pink);
            }

            .product-info {
                flex: 1;
                min-width: 320px;
            }

            .product-info h2 {
                font-size: 28px;
                font-weight: bold;
                margin-bottom: 10px;
            }

            .showcase-rating {
                display: flex;
                align-items: center;
                flex-wrap: wrap;
                gap: 8px;
                color: var(--sandy-brown);
                margin: 10px 0;
            }

            .showcase-rating .stars {
                display: flex;
                gap: 3px;
            }

            .price-box {
                margin: 20px 0 10px;
            }

            .price {
                font-size: 26px;
                font-weight: bold;
                color: var(--main-pink);
            }

            .stock-status {
                font-size: 20px;
                font-weight: 500;
                margin-bottom: 20px;
                margin-top: 50px;
            }

            .stock-available {
                color: #10b981;
            }
            .stock-out {
                color: var(--error-red);
            }

            .quantity-group {
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 10px;
            }

            .quantity-group button {
                font-size: 18px;
                padding: 6px 12px;
                border: none;
                background: var(--main-orange);
                color: white;
                border-radius: 6px;
                cursor: pointer;
            }

            .quantity-group input {
                width: 60px;
                padding: 6px;
                text-align: center;
                border: 1px solid #ccc;
                border-radius: 6px;
                -moz-appearance: textfield;
            }

            .quantity-group input::-webkit-inner-spin-button,
            .quantity-group input::-webkit-outer-spin-button {
                -webkit-appearance: none;
            }

            .quantity-error {
                color: var(--error-red);
                font-size: 13px;
                margin-top: -5px;
                margin-bottom: 15px;
                display: none;
            }

            .add-cart-btn {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: white;
                padding: 12px 25px;
                border: none;
                border-radius: 30px;
                font-size: 16px;
                font-weight: 600;
                cursor: pointer;
                transition: 0.3s;
            }

            .add-cart-btn:hover {
                opacity: 0.9;
            }

            .shop-box {
                display: flex;
                align-items: center;
                gap: 12px;
                margin-top: 30px;
            }

            .shop-box a {
                font-weight: 600;
                color: var(--main-pink);
                text-decoration: none;
            }

            .shop-box img {
                width: 40px;
                height: 40px;
                border-radius: 50%;
                border: 1px solid #ccc;
            }

            .section {
                margin-top: 60px;
            }

            .section h3 {
                font-size: 20px;
                font-weight: 600;
                border-bottom: 2px solid #eee;
                margin-bottom: 15px;
                padding-bottom: 8px;
            }

            .product-description {
                line-height: 1.6;
                color: #555;
                white-space: pre-line;
            }

            .product-description a {
                color: black;
                text-decoration: none;
                font-weight: 500;
            }

            .product-description a:hover {
                color: var(--main-pink);
            }

            .review-item {
                border-top: 1px solid #eee;
                padding: 15px 0;
            }

            .review-header {
                display: flex;
                justify-content: space-between;
                font-weight: 500;
            }

            .review-rating {
                color: var(--sandy-brown);
                margin: 5px 0;
            }
        </style>
    </head>
    <body>

        <jsp:include page="./sub/header.jsp" />

        <main class="container-main">

            <!-- Product Main Section -->
            <div class="product-detail">

                <!-- Image Carousel -->
                <div class="image-carousel">
                    <div class="carousel-images">
                        <c:forEach var="img" items="${product.productImagesList}" varStatus="loop">
                            <img src="${img.imageUrl}" alt="${product.name}" class="${loop.index == 0 ? 'active' : ''}" />
                        </c:forEach>
                    </div>
                    <button class="carousel-nav carousel-prev" onclick="prevImage()">&#10094;</button>
                    <button class="carousel-nav carousel-next" onclick="nextImage()">&#10095;</button>
                    <div class="thumbnail-list">
                        <c:forEach var="img" items="${product.productImagesList}" varStatus="loop">
                            <img src="${img.imageUrl}" onclick="showImage(${loop.index})" class="${loop.index == 0 ? 'selected' : ''}" />
                        </c:forEach>
                    </div>
                </div>

                <!-- Product Info -->
                <div class="product-info">
                    <h2>${product.name}</h2>

                    <div class="showcase-rating">
                        <div class="stars">
                            <c:set var="avgRating" value="${product.averageRating}" />
                            <c:forEach begin="1" end="5" var="i">
                                <ion-icon name="${i <= avgRating ? 'star' : (i - avgRating <= 0.5 ? 'star-half-outline' : 'star-outline')}"></ion-icon>
                                </c:forEach>
                        </div>
                        <span>(${fn:length(product.reviewsList)} reviews)</span>
                        <span>| Sold: ${product.soldQuantity}</span>
                    </div>

                    <div class="price-box">
                        <p class="price">$${product.basePrice}</p>
                    </div>

                    <!-- Stock -->
                    <c:choose>
                        <c:when test="${product.stockQuantity > 0}">
                            <p class="stock-status stock-available">In stock: ${product.stockQuantity}</p>
                        </c:when>
                        <c:otherwise>
                            <p class="stock-status stock-out">Out of Stock</p>
                        </c:otherwise>
                    </c:choose>

                    <!-- Add to cart -->
                    <c:if test="${product.stockQuantity > 0}">
                        <form method="post" action="add-to-cart" onsubmit="return validateQuantity(${product.stockQuantity})">
                            <input type="hidden" name="productId" value="${product.productId}" />
                            <label for="quantity" style="font-weight: 500;">Quantity:</label>
                            <div class="quantity-group">
                                <button type="button" onclick="changeQuantity(-1)">−</button>
                                <input type="number" name="quantity" id="quantity" value="1" min="1" />
                                <button type="button" onclick="changeQuantity(1)">+</button>
                            </div>
                            <div class="quantity-error" id="quantity-error">Quantity exceeds stock!</div>
                            <button class="add-cart-btn" type="submit">Add to Cart</button>
                        </form>
                        <form action="wishList" method="post" style="margin-top: 12px;">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="${product.productId}" />
                            <button type="submit" class="add-cart-btn" style="background: #f3f4f6; color: var(--main-pink); border: 2px solid var(--main-pink);">
                                <ion-icon name="heart-outline" style="vertical-align: middle; margin-right: 6px;"></ion-icon> Thêm vào Yêu thích
                            </button>
                        </form>
                    </c:if>
                </div>
            </div>

            <!-- Shop Info -->
            <div class="shop-box">
                <c:choose>
                    <c:when test="${not empty product.shopId.shopLogoUrl}">
                        <a href="#"><img src="${product.shopId.shopLogoUrl}" alt="Shop Logo"></a>
                        </c:when>
                        <c:otherwise>
                        <a href="#"><img src="./images/shop_default.png" alt="Default Shop Logo"></a>
                        </c:otherwise>
                    </c:choose>
                <a href="#">${product.shopId.shopName}</a>
            </div>

            <!-- Product Description -->
            <div class="section product-description-section">
                <h3>Product Description</h3>
                <p class="product-description">
                    <strong>Category:</strong> <a href="#">${product.categoryId.name}</a> <br><br>
                    ${product.description}
                </p>
            </div>

            <!-- Customer Reviews -->
            <div class="section product-reviews">
                <h3>Customer Reviews</h3>
                <c:forEach var="review" items="${product.reviewsList}">
                    <div class="review-item">
                        <div class="review-header">
                            <span>${review.customerId.fullName}</span>
                            <span>${review.createdAt}</span>
                        </div>
                        <div class="review-rating">
                            <c:forEach begin="1" end="5" var="i">
                                <ion-icon name="${i <= review.rating ? 'star' : 'star-outline'}"></ion-icon>
                                </c:forEach>
                        </div>
                        <p>${review.comment}</p>
                    </div>
                </c:forEach>
            </div>

        </main>

        <jsp:include page="./sub/footer.jsp" />

        <!-- Scripts -->
        <script type="module" src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.esm.js"></script>
        <script nomodule src="https://unpkg.com/ionicons@5.5.2/dist/ionicons/ionicons.js"></script>
        <script>
                                    let currentIndex = 0;
                                    const images = document.querySelectorAll(".carousel-images img");
                                    const thumbs = document.querySelectorAll(".thumbnail-list img");

                                    function showImage(index) {
                                        currentIndex = index;
                                        images.forEach(img => img.classList.remove("active"));
                                        images[index].classList.add("active");
                                        thumbs.forEach(img => img.classList.remove("selected"));
                                        thumbs[index].classList.add("selected");
                                    }

                                    function nextImage() {
                                        currentIndex = (currentIndex + 1) % images.length;
                                        showImage(currentIndex);
                                    }

                                    function prevImage() {
                                        currentIndex = (currentIndex - 1 + images.length) % images.length;
                                        showImage(currentIndex);
                                    }

                                    function changeQuantity(delta) {
                                        const input = document.getElementById("quantity");
                                        const max = parseInt(${product.stockQuantity});
                                        let value = parseInt(input.value) || 1;
                                        value += delta;
                                        if (value < 1)
                                            value = 1;
                                        if (value > max)
                                            value = max;
                                        input.value = value;
                                        document.getElementById("quantity-error").style.display = "none";
                                    }

                                    function validateQuantity(maxStock) {
                                        const input = document.getElementById("quantity");
                                        const value = parseInt(input.value);
                                        if (value > maxStock) {
                                            document.getElementById("quantity-error").style.display = "block";
                                            return false;
                                        }
                                        return true;
                                    }
        </script>
    </body>
</html>
