<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="./sub/header.jsp" />

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Giỏ hàng</title>
        <link rel="shortcut icon" href="./images/logo/favicon.ico" type="image/x-icon">
        <link rel="stylesheet" href="./css/style-prefix.css">
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600&display=swap" rel="stylesheet">

        <style>
            body {
                font-family: 'Poppins', sans-serif;
                background-color: #f9fafb;
                color: #111827;
            }

            .cart-container {
                max-width: 1200px;
                margin: 40px auto;
                padding: 0 16px;
            }

            h1 {
                text-align: center;
                font-size: 32px;
                font-weight: 700;
                margin-bottom: 40px;
                color: #1f2937;
            }

            .cart-table {
                width: 100%;
                border-collapse: collapse;
                background-color: #fff;
                border-radius: 12px;
                overflow: hidden;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
            }

            .cart-table th, .cart-table td {
                padding: 16px;
                text-align: center;
            }

            .cart-table th {
                background-color: #f3f4f6;
                font-weight: 600;
                font-size: 16px;
                color: #374151;
            }

            .cart-table td {
                border-bottom: 1px solid #e5e7eb;
                vertical-align: middle;
            }

            .cart-table img {
                max-height: 80px;
                border-radius: 8px;
                margin-right: 8px;
            }

            .showcase-title {
                font-weight: 600;
                font-size: 15px;
                color: #111827;
                display: inline-block;
                margin-left: 8px;
                text-decoration: none;
            }

            .showcase-title:hover {
                text-decoration: underline;
                color: #2563eb;
            }

            .quantity-form {
                display: flex;
                justify-content: center;
                align-items: center;
                gap: 8px;
            }

            .quantity-form input[type="number"] {
                width: 60px;
                padding: 6px;
                border: 1px solid #d1d5db;
                border-radius: 6px;
            }

            .btn-action {
                background-color: #ef4444;
                color: #fff;
                border: none;
                padding: 8px 16px;
                border-radius: 8px;
                cursor: pointer;
                font-size: 14px;
                transition: background-color 0.3s;
            }

            .btn-action:hover {
                background-color: #dc2626;
            }

            .cart-summary {
                margin-top: 30px;
                text-align: right;
                font-size: 20px;
                font-weight: bold;
                color: #111827;
            }

            .checkout-btn {
                margin-top: 20px;
                background-color: #10b981;
                padding: 12px 24px;
                font-size: 16px;
                border-radius: 10px;
                border: none;
                color: #fff;
                cursor: pointer;
                transition: background-color 0.3s;
            }

            .checkout-btn:hover {
                background-color: #059669;
            }

            .empty-message {
                text-align: center;
                font-size: 18px;
                color: #6b7280;
                margin-top: 60px;
            }
        </style>
    </head>

    <body>

        <div class="cart-container">
            <h1><i class="fas fa-shopping-cart"></i> Giỏ hàng của bạn</h1>

            <c:choose>
                <c:when test="${empty cartItems}">
                    <div class="empty-message">
                        <i class="fas fa-box-open" style="font-size: 28px;"></i><br>
                        Giỏ hàng của bạn đang trống.
                    </div>
                </c:when>

                <c:otherwise>
                    <table class="cart-table">
                        <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Giá</th>
                                <th>Số lượng</th>
                                <th>Thành tiền</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="totalPrice" value="0" />
                            <c:forEach var="item" items="${cartItems}">
                                <c:set var="product" value="${item.productId}" />
                                <c:set var="subtotal" value="${item.quantity * product.basePrice}" />
                                <c:set var="totalPrice" value="${totalPrice + subtotal}" />
                                <tr>
                                    <td>
                                        <img src="${product.productImagesList[0].imageUrl}" alt="${product.name}">
                                        <a href="product?id=${product.productId}" class="showcase-title">${product.name}</a>
                                    </td>
                                    <td>${product.basePrice}₫</td>
                                    <td>
                                        <form method="post" action="cart?action=update" class="quantity-form">
                                            <input type="hidden" name="productId" value="${product.productId}">
                                            <input type="number" class="quantity-input" name="quantity"
                                                   value="${item.quantity}" min="1" max="${product.stockQuantity}"
                                                   onchange="validateAndSubmit(this)">

                                            <button type="submit" class="btn-action">Cập nhật</button>
                                        </form>

                                        <!-- ✅ Hiển thị tồn kho -->
                                        <div style="font-size: 13px; color: #6b7280; margin-top: 4px;">
                                            Tồn kho: ${product.stockQuantity} sản phẩm
                                        </div>

                                        <!-- ✅ Hiển thị lỗi nếu có -->
                                        <c:if test="${errorProductId == product.productId}">
                                            <div style="color:red; font-size: 14px; margin-top: 4px;">
                                                ${errorMessage}
                                            </div>
                                        </c:if>


                                    </td>
                                    <td>${subtotal}₫</td>
                                    <td>
                                        <form method="post" action="cart?action=remove">
                                            <input type="hidden" name="productId" value="${product.productId}">
                                            <button type="submit" class="btn-action">Xóa</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <div class="cart-summary">
                        Tổng cộng: ${totalPrice}₫
                        <form method="post" action="checkout">
                            <button type="submit" class="checkout-btn">Thanh toán</button>
                        </form>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <script>
            // Simple quantity validation
            document.querySelectorAll('.quantity-input').forEach(input => {
                input.addEventListener('change', function () {
                    const max = parseInt(this.getAttribute('max'));
                    const value = parseInt(this.value);

                    if (value < 1)
                        this.value = 1;
                    if (value > max)
                        this.value = max;
                });
            });

            function validateAndSubmit(input) {
                // Ensure value stays within min/max bounds
                const min = parseInt(input.min);
                const max = parseInt(input.max);
                let value = parseInt(input.value);

                if (isNaN(value))
                    value = min;
                if (value < min)
                    value = min;
                if (value > max)
                    value = max;

                input.value = value;
                input.form.submit(); // Tự động submit form cập nhật
            }
        </script>
    </body>
</html>

<jsp:include page="./sub/footer.jsp" />
