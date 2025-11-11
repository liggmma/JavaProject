<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Order Detail</title>
        <link rel="stylesheet" href="./css/style-prefix.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --gray: #6b7280;
                --light-gray: #f9fafb;
                --dark: #111827;
                --border: #e5e7eb;
            }

            body {
                font-family: 'Poppins', sans-serif;
                background-color: #f3f4f6;
                margin: 0;
                padding: 0;
            }

            .order-container {
                max-width: 1100px;
                margin: 40px auto;
                background: white;
                padding: 40px;
                border-radius: 16px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
            }

            .order-header {
                font-size: 24px;
                font-weight: 700;
                color: var(--dark);
                margin-bottom: 30px;
            }

            .summary-table,
            .items-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 40px;
            }

            .summary-table th,
            .summary-table td,
            .items-table th,
            .items-table td {
                padding: 14px 16px;
                border-bottom: 1px solid var(--border);
                font-size: 15px;
            }

            .summary-table th,
            .items-table th {
                background-color: var(--light-gray);
                color: var(--gray);
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 0.5px;
            }

            .summary-table td {
                color: var(--dark);
            }

            .items-table td {
                vertical-align: middle;
            }

            .total-highlight {
                font-weight: 700;
                color: var(--main-orange);
                background-color: #fff7ed;
            }

            .product-name {
                font-weight: 600;
                color: var(--main-pink);
            }

            .items-table td:last-child,
            .summary-table td:last-child {
                text-align: right;
            }

            @media (max-width: 768px) {
                .summary-table,
                .items-table {
                    font-size: 14px;
                }
            }
        </style>
    </head>
    <body>



        <div class="order-container">
            <div class="order-header">Order #${orderDetail.orderNumber}</div>

            <table class="summary-table">
                <tr><th>Status</th><td>${orderDetail.status}</td></tr>
                <tr><th>Created At</th><td>${orderDetail.createdAt}</td></tr>
                <tr><th>Updated At</th><td>${orderDetail.updatedAt}</td></tr>
                <tr><th>Full Name</th><td>${orderDetail.fullName}</td></tr>
                <tr><th>Phone Number</th><td>${orderDetail.phoneNumber}</td></tr>
                <tr><th>Shipping Address ID</th><td>${orderDetail.shippingAddress}</td></tr>
                <tr><th>Subtotal</th><td>${orderDetail.subtotal} ₫</td></tr>
                <tr><th>Shipping Fee</th><td>${orderDetail.shippingFee} ₫</td></tr>
                <tr><th>Discount</th><td>${orderDetail.discountAmount} ₫</td></tr>
                <tr class="total-highlight">
                    <th>Total Amount</th><td>${orderDetail.totalAmount} ₫</td>
                </tr>
            </table>

            <div class="order-header" style="font-size: 20px;">Items in this Order</div>
            <table class="items-table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Unit Price</th>
                        <th>Quantity</th>
                        <th>Total</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${orderDetail.orderItemsList}">
                        <tr>
                            <td class="product-name">${item.productId.name}</td>
                            <td>${item.unitPrice} ₫</td>
                            <td>${item.quantity}</td>
                            <td>${item.totalPrice} ₫</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>



    </body>
</html>
