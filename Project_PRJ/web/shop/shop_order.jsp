<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>

<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Seller Dashboard</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --bg-light: #f9fafb;
                --gray: #6b7280;
                --dark: #111827;
                --green: #10b981;
                --red: #ef4444;
                --blue: #3b82f6; /* Added for general buttons/links */
                --yellow: #f59e0b; /* Added for Pending/Processing status */
            }

            body {
                font-family: 'Poppins', sans-serif;
                background: var(--bg-light);
                margin: 0;
                padding: 30px;
            }

            .dashboard-wrapper {
                display: flex;
                max-width: 1400px;
                margin: auto;
                gap: 30px;
            }

            /* --- Sidebar Styles --- */
            .sidebar {
                width: 240px;
                background: #fff;
                border-radius: 16px;
                padding: 20px;
                box-shadow: 0 4px 16px rgba(0,0,0,0.05);
                min-height: fit-content;
            }

            .sidebar .dropdown {
                margin-bottom: 10px;
            }

            .sidebar .dropdown-toggle {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 16px;
                font-weight: 500;
                border-radius: 10px;
                color: var(--dark);
                background: #fff;
                border: 1px solid transparent;
                cursor: pointer;
                width: 100%;
                text-align: left;
                transition: all 0.3s;
            }

            .sidebar .dropdown-toggle i {
                margin-right: 10px;
            }

            .sidebar .dropdown-toggle:hover {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: #fff;
                border-color: var(--main-pink);
            }

            .sidebar .dropdown-toggle.active {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: #fff;
                border-color: var(--main-pink);
            }

            .sidebar .dropdown-toggle.active .fa-chevron-down {
                transform: rotate(180deg);
            }

            .sidebar .dropdown-menu {
                display: none;
                flex-direction: column;
                padding-left: 20px;
                margin-top: 8px;
            }

            .sidebar .dropdown-menu.active {
                display: flex;
            }

            .sidebar .dropdown-menu a {
                padding: 8px;
                text-decoration: none;
                color: var(--gray);
                font-size: 14px;
                border-radius: 8px;
                transition: background 0.2s;
            }

            .sidebar .dropdown-menu a:hover {
                background: #f3f4f6;
            }

            /* --- Main Content Styles --- */
            .dashboard-container {
                flex: 1;
                background: #fff;
                border-radius: 16px;
                padding: 30px;
                box-shadow: 0 10px 20px rgba(0,0,0,0.06);
            }

            .dashboard-title {
                font-size: 28px;
                font-weight: 600;
                margin-bottom: 30px;
                display: flex;
                align-items: center;
                gap: 12px;
            }

            /* --- KPI Cards Styles --- */
            .stats-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 20px;
                margin-bottom: 30px;
            }
            .stat-card {
                background: #fff;
                padding: 20px;
                border-radius: 12px;
                border: 1px solid #e5e7eb;
                box-shadow: 0 2px 4px rgba(0,0,0,0.02);
                transition: transform 0.2s, box-shadow 0.2s;
            }
            .stat-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 8px 15px rgba(0,0,0,0.06);
            }
            .stat-card .title {
                color: var(--gray);
                font-size: 14px;
                margin-bottom: 8px;
            }
            .stat-card .value {
                color: var(--dark);
                font-size: 26px;
                font-weight: 600;
            }
            .stat-card .change {
                font-size: 13px;
                font-weight: 500;
                margin-top: 8px;
            }
            .stat-card .change.positive {
                color: var(--green);
            }
            .stat-card .change.negative {
                color: var(--red);
            }

            /* --- Chart Section Styles --- */
            .chart-section {
                padding: 20px;
                border: 1px solid #e5e7eb;
                border-radius: 12px;
                margin-bottom: 30px;
                position: relative;
            }
            .chart-section canvas {
                height: 350px !important;
                width: 100% !important;
            }
            .chart-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 20px;
            }
            .chart-title {
                font-size: 18px;
                font-weight: 600;
            }
            #timeFilter {
                padding: 8px 14px;
                border-radius: 6px;
                border: 1px solid #ccc;
            }

            /* --- Table Section Styles (Adjusted for transaction-card) --- */
            .transaction-table {
                display: flex;
                flex-direction: column;
                gap: 15px; /* Space between cards */
                margin-top: 20px;
            }

            .transaction-card {
                background: #fff;
                border: 1px solid #e0e0e0;
                border-radius: 10px;
                padding: 15px 20px;
                display: flex;
                justify-content: space-between;
                align-items: center;
                box-shadow: 0 2px 5px rgba(0,0,0,0.05);
                transition: transform 0.2s ease, box-shadow 0.2s ease;
            }

            .transaction-card:hover {
                transform: translateY(-3px);
                box-shadow: 0 4px 10px rgba(0,0,0,0.1);
            }

            .txn-left {
                display: flex;
                flex-direction: column;
            }

            .txn-date {
                font-size: 12px;
                color: var(--gray);
                margin-bottom: 5px;
            }

            .txn-type {
                font-weight: 600;
                color: var(--dark);
                font-size: 16px;
            }

            .txn-desc {
                font-size: 13px;
                color: var(--gray);
            }

            .txn-right {
                display: flex;
                align-items: center;
                gap: 15px;
                flex-wrap: wrap; /* Allow buttons to wrap on smaller screens */
                justify-content: flex-end; /* Align buttons to the right */
            }

            .txn-amount {
                font-weight: 600;
                color: var(--dark);
                font-size: 17px;
            }

            .txn-status {
                padding: 5px 10px;
                border-radius: 5px;
                font-weight: 500;
                font-size: 13px;
                color: white;
            }

            .txn-status.Delivered {
                background-color: var(--green);
            }

            .txn-status.Cancelled {
                background-color: var(--red);
            }
            .txn-status.Pending, .txn-status.Processing, .txn-status.Shipped {
                background-color: var(--yellow);
            }

            /* New styles for action buttons container */
            .action-buttons {
                display: flex;
                gap: 10px; /* Space between action buttons */
                flex-wrap: wrap; /* Allow buttons to wrap on smaller screens */
                justify-content: flex-end; /* Align buttons to the right */
            }

            /* Consolidated button styles */
            .confirm-btn, .cancel-btn, .shipped-btn, .view-detail-btn {
                padding: 8px 12px;
                border-radius: 6px;
                font-size: 14px;
                cursor: pointer;
                transition: background-color 0.2s ease;
                display: flex;
                align-items: center;
                gap: 5px;
                text-decoration: none;
                border: none; /* Ensure no default borders */
                white-space: nowrap; /* Prevent text from wrapping */
            }

            .confirm-btn {
                background-color: var(--green);
                color: white;
            }
            .confirm-btn:hover {
                background-color: #0c8a68;
            }

            .cancel-btn {
                background-color: var(--red);
                color: white;
            }
            .cancel-btn:hover {
                background-color: #cc3131;
            }

            .shipped-btn {
                background-color: var(--blue); /* Blue for shipped */
                color: white;
            }
            .shipped-btn:hover {
                background-color: #2a6edc;
            }

            .view-detail-btn {
                background-color: #6c757d; /* A neutral gray for view detail */
                color: white;
            }
            .view-detail-btn:hover {
                background-color: #5a6268;
            }

            .view-detail-btn i {
                margin-right: 0; /* Remove margin for icon in view detail button */
            }

            /* --- Pagination Styles --- */
            .pagination {
                display: flex;
                justify-content: center;
                gap: 8px;
                margin-top: 20px;
            }
            .pagination button, .pagination a {
                padding: 8px 14px;
                background: white;
                color: var(--dark);
                border: 1px solid #d1d5db;
                border-radius: 8px;
                cursor: pointer;
                font-weight: 500;
                transition: all 0.3s;
                text-decoration: none;
            }
            .pagination button:hover, .pagination a:hover {
                border-color: var(--main-pink);
            }
            .pagination button.active, .pagination a.active {
                background: var(--main-pink);
                color: white;
                border-color: var(--main-pink);
            }
            /* Search and Filter Styles */
            .filter-controls {
                display: flex;
                gap: 15px;
                margin-bottom: 20px;
                flex-wrap: wrap;
            }

            .filter-controls input[type="text"],
            .filter-controls select,
            .filter-controls button {
                padding: 10px 15px;
                border: 1px solid #ccc;
                border-radius: 8px;
                font-size: 15px;
            }

            .filter-controls input[type="text"] {
                flex-grow: 1;
            }

            .filter-controls button {
                background-color: var(--main-pink);
                color: white;
                cursor: pointer;
                border: none;
            }

            .filter-controls button:hover {
                opacity: 0.9;
            }

            /* Responsive */
            @media (max-width: 992px) {
                .dashboard-wrapper {
                    flex-direction: column;
                }
                .sidebar {
                    width: 100%;
                }
                .filter-controls {
                    flex-direction: column;
                }
                .transaction-card {
                    flex-direction: column;
                    align-items: flex-start;
                    gap: 10px;
                }
                .txn-right {
                    width: 100%;
                    justify-content: flex-start; /* Align action buttons to start on mobile */
                    flex-wrap: wrap; /* Allow buttons to wrap */
                }
                .action-buttons {
                    width: 100%;
                    justify-content: flex-start; /* Align action buttons to start on mobile */
                }
            }
        </style>
    </head>
    <body>

        <div class="dashboard-wrapper">
            <div class="sidebar">

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-boxes-stacked"></i> Quản lý sản phẩm</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="${pageContext.request.contextPath}/shop/manageProducts">Tất cả sản phẩm</a>
                        <a href="${pageContext.request.contextPath}/shop/manageProducts?action=add">Thêm sản phẩm</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle active">
                        <span><i class="fas fa-file-invoice"></i> Quản lý đơn hàng</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu active">
                        <a href="${pageContext.request.contextPath}/shop/manageOrders" class="${status == null || status == '' ? 'active' : ''}">Xem tất cả đơn hàng</a>
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?status=Pending" class="${status == 'Pending' ? 'active' : ''}">Đơn chờ xác nhận</a>
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?status=Processing" class="${status == 'Processing' ? 'active' : ''}">Đơn đang xử lý</a>
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?status=Shipped" class="${status == 'Shipped' ? 'active' : ''}">Đơn đang giao</a>
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?status=Delivered" class="${status == 'Delivered' ? 'active' : ''}">Đơn giao thành công</a>
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?status=Cancelled" class="${status == 'Cancelled' ? 'active' : ''}">Đơn hủy và trả hàng</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-headset"></i> Chăm sóc khách hàng</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="${pageContext.request.contextPath}/shop/chat">Quản lý chat</a>
                        <a href="${pageContext.request.contextPath}/shop/reviewManager">Quản lý đánh giá</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-wallet"></i> Tài chính</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="${pageContext.request.contextPath}/shop/wallet">Số dư Wallet</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-chart-line"></i> Dữ liệu</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="${pageContext.request.contextPath}/shop/analytics">Phân tích bán hàng</a>
                        <a href="${pageContext.request.contextPath}/shop/performance">Hiệu quả hoạt động</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-store"></i> Quản lý shop</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="${pageContext.request.contextPath}/shop/profile">Hồ sơ shop</a>
                    </div>
                </div>

            </div>

            <main class="dashboard-container">
                <h2 class="dashboard-title"><i class="fas fa-file-invoice"></i> Quản lý đơn hàng</h2>

                <div class="filter-controls">
                    <form action="${pageContext.request.contextPath}/shop/manageOrders" method="GET" style="display: flex; gap: 10px; width: 100%;">
                        <input type="text" name="keyword" placeholder="Tìm kiếm theo mã đơn, tên, SĐT" value="${keyword}">
                        <select name="status" onchange="this.form.submit()">
                            <option value="">Tất cả trạng thái</option>
                            <option value="Pending" ${status == 'Pending' ? 'selected' : ''}>Đơn chờ xác nhận</option>
                            <option value="Processing" ${status == 'Processing' ? 'selected' : ''}>Đơn đang xử lý</option>
                            <option value="Shipped" ${status == 'Shipped' ? 'selected' : ''}>Đơn đang giao</option>
                            <option value="Delivered" ${status == 'Delivered' ? 'active' : ''}>Đơn giao thành công</option>
                            <option value="Cancelled" ${status == 'Cancelled' ? 'active' : ''}>Đơn hủy và trả hàng</option>
                        </select>
                        <button type="submit">Tìm kiếm</button>
                    </form>
                </div>


                <div id="orderList" class="transaction-table">
                    <c:choose>
                        <c:when test="${not empty orders}">
                            <c:forEach var="order" items="${orders}" varStatus="loop">
                                <div class="transaction-card" data-status="${order.status}" data-index="${loop.index}" data-order-id="${order.orderId}">
                                    <div class="txn-left">
                                        <div class="txn-date"><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/></div>
                                        <div class="txn-type">Order #${order.orderNumber}</div>
                                        <div class="txn-desc">Người nhận: ${order.fullName} - ${order.phoneNumber}</div>
                                        <div class="txn-desc">Địa chỉ: ${order.shippingAddress}</div>
                                        <%-- Debugging line: Show order ID --%>
                                        <div class="txn-desc" style="font-size: 10px; color: #aaa;">Debug Order ID: ${order.orderId}</div>
                                    </div>
                                    <div class="txn-right">
                                        <div class="txn-amount"><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫" groupingUsed="true" maxFractionDigits="0"/></div>

                                        <div class="action-buttons">
                                            <%-- Add a check to ensure order.orderId is not null or empty before rendering buttons --%>
                                            <c:if test="${not empty order.orderId}">
                                                <c:choose>
                                                    <c:when test="${order.status == 'Pending'}">
                                                        <button class="confirm-btn"
                                                                onclick="updateOrderStatus(${order.orderId}, 'confirmProcessing', 'xác nhận và chuyển sang đang xử lý')">
                                                            <i class="fas fa-check"></i> Xác nhận
                                                        </button>
                                                        <button class="cancel-btn" onclick="updateOrderStatus(${order.orderId}, 'cancelOrder', 'hủy')">
                                                            <i class="fas fa-times"></i> Hủy đơn
                                                        </button>
                                                    </c:when>
                                                    <c:when test="${order.status == 'Processing'}">
                                                        <button class="shipped-btn"
                                                                onclick="updateOrderStatus(${order.orderId}, 'confirmShipped', 'chuyển sang đang giao')">
                                                            <i class="fas fa-truck"></i> Đã gửi hàng
                                                        </button>
                                                    </c:when>
                                                    <%-- REMOVED: <c:when test="${order.status == 'Shipped'}"> block --%>
                                                    <c:otherwise>
                                                        <%-- For Shipped, Delivered, Cancelled, and any other final statuses, just show the status text --%>
                                                        <div class="txn-status ${order.status}">${order.status}</div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/shop/orderDetail?orderId=${order.orderId}" class="view-detail-btn">
                                                <i class="fas fa-eye"></i> Xem chi tiết
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p>Không có đơn hàng nào.</p>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="pagination" id="orderPaginationControls">
                    <c:if test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?page=${currentPage - 1}&status=${status}&keyword=${keyword}">Trước</a>
                    </c:if>
                    <c:forEach begin="1" end="${totalPages}" var="i">
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?page=${i}&status=${status}&keyword=${keyword}" class="${i == currentPage ? 'active' : ''}">${i}</a>
                    </c:forEach>
                    <c:if test="${currentPage < totalPages}">
                        <a href="${pageContext.request.contextPath}/shop/manageOrders?page=${currentPage + 1}&status=${status}&keyword=${keyword}">Sau</a>
                    </c:if>
                </div>
            </main>
        </div>

        <script src="[https://cdn.jsdelivr.net/npm/chart.js](https://cdn.jsdelivr.net/npm/chart.js)"></script>
        <script>
                                                                    document.addEventListener('DOMContentLoaded', function () {
                                                                        function toggleDropdown(button) {
                                                                            const parentDropdown = button.closest('.dropdown');
                                                                            const dropdownMenu = parentDropdown.querySelector('.dropdown-menu');

                                                                            document.querySelectorAll('.sidebar .dropdown-toggle.active').forEach(activeButton => {
                                                                                if (activeButton !== button) {
                                                                                    activeButton.classList.remove('active');
                                                                                    activeButton.closest('.dropdown').querySelector('.dropdown-menu').classList.remove('active');
                                                                                }
                                                                            });

                                                                            button.classList.toggle('active');
                                                                            dropdownMenu.classList.toggle('active');
                                                                        }

                                                                        document.querySelectorAll('.sidebar .dropdown-toggle').forEach(button => {
                                                                            button.addEventListener('click', () => toggleDropdown(button));
                                                                        });

                                                                        // Function to update order status via AJAX
                                                                        window.updateOrderStatus = function (orderId, action, confirmMessage) {
                                                                            // --- DEBUGGING LINE ---
                                                                            console.log("Attempting to update order. orderId:", orderId, "action:", action);
                                                                            // --- END DEBUGGING LINE ---

                                                                            if (!orderId) { // Explicitly check if orderId is falsy (null, undefined, 0, or empty string)
                                                                                alert("Lỗi: Không thể xác định mã đơn hàng. Vui lòng thử lại hoặc tải lại trang.");
                                                                                console.error("Order ID is invalid:", orderId);
                                                                                return; // Stop execution if orderId is invalid
                                                                            }

                                                                            if (confirm(`Bạn có chắc chắn muốn ${confirmMessage} đơn hàng này không?`)) {
                                                                                fetch('${pageContext.request.contextPath}/shop/manageOrders', {
                                                                                    method: 'POST',
                                                                                    headers: {
                                                                                        'Content-Type': 'application/x-www-form-urlencoded',
                                                                                    },
                                                                                    body: `orderId=${orderId}&action=${action}`
                                                                                })
                                                                                        .then(response => {
                                                                                            // Log the raw response status for debugging
                                                                                            console.log("Fetch response status:", response.status);
                                                                                            if (!response.ok) {
                                                                                                // Attempt to parse JSON error message from server
                                                                                                return response.json().then(errorData => {
                                                                                                    throw new Error(errorData.message || `Server error with status ${response.status}`);
                                                                                                });
                                                                                            }
                                                                                            return response.json();
                                                                                        })
                                                                                        .then(data => {
                                                                                            if (data.success) {
                                                                                                alert(data.message);
                                                                                                const orderCard = document.querySelector(`.transaction-card[data-order-id="${orderId}"]`);
                                                                                                if (orderCard) {
                                                                                                    let newStatus = '';
                                                                                                    if (action === 'confirmProcessing') {
                                                                                                        newStatus = 'Processing';
                                                                                                    } else if (action === 'cancelOrder') {
                                                                                                        newStatus = 'Cancelled';
                                                                                                    } else if (action === 'confirmShipped') {
                                                                                                        newStatus = 'Shipped';
                                                                                                    } else if (action === 'confirmDelivered') { // This action is still valid if called from elsewhere, but the button is removed from shop UI for 'Shipped'
                                                                                                        newStatus = 'Delivered';
                                                                                                    }

                                                                                                    // Update data-status attribute
                                                                                                    orderCard.dataset.status = newStatus;

                                                                                                    // Get the action-buttons container
                                                                                                    const actionButtonsContainer = orderCard.querySelector('.action-buttons');
                                                                                                    if (actionButtonsContainer) {
                                                                                                        actionButtonsContainer.innerHTML = ''; // Clear all existing buttons

                                                                                                        // Re-add buttons based on the new status
                                                                                                        if (newStatus === 'Pending') {
                                                                                                            const confirmBtn = document.createElement('button');
                                                                                                            confirmBtn.className = 'confirm-btn';
                                                                                                            confirmBtn.onclick = () => updateOrderStatus(orderId, 'confirmProcessing', 'xác nhận và chuyển sang đang xử lý');
                                                                                                            confirmBtn.innerHTML = '<i class="fas fa-check"></i> Xác nhận';
                                                                                                            actionButtonsContainer.appendChild(confirmBtn);

                                                                                                            const cancelBtn = document.createElement('button');
                                                                                                            cancelBtn.className = 'cancel-btn';
                                                                                                            cancelBtn.onclick = () => updateOrderStatus(orderId, 'cancelOrder', 'hủy');
                                                                                                            cancelBtn.innerHTML = '<i class="fas fa-times"></i> Hủy đơn';
                                                                                                            actionButtonsContainer.appendChild(cancelBtn);
                                                                                                        } else if (newStatus === 'Processing') {
                                                                                                            const shippedBtn = document.createElement('button');
                                                                                                            shippedBtn.className = 'shipped-btn';
                                                                                                            shippedBtn.onclick = () => updateOrderStatus(orderId, 'confirmShipped', 'chuyển sang đang giao');
                                                                                                            shippedBtn.innerHTML = '<i class="fas fa-truck"></i> Đã gửi hàng';
                                                                                                            actionButtonsContainer.appendChild(shippedBtn);
                                                                                                        } else { // This block now handles Shipped, Delivered, Cancelled, etc.
                                                                                                            // For Shipped, Delivered, Cancelled, and other final states, just show the status text
                                                                                                            const statusElement = document.createElement('div');
                                                                                                            statusElement.className = `txn-status ${newStatus}`;
                                                                                                            statusElement.textContent = newStatus;
                                                                                                            actionButtonsContainer.appendChild(statusElement);
                                                                                                        }

                                                                                                        // Always re-add the "Xem chi tiết" button at the end
                                                                                                        const viewDetailBtn = document.createElement('a');
                                                                                                        viewDetailBtn.href = `${pageContext.request.contextPath}/shop/orderDetail?orderId=${orderId}`;
                                                                                                                                                viewDetailBtn.className = 'view-detail-btn';
                                                                                                                                                viewDetailBtn.innerHTML = '<i class="fas fa-eye"></i> Xem chi tiết';
                                                                                                                                                actionButtonsContainer.appendChild(viewDetailBtn);
                                                                                                                                            }
                                                                                                                                        } else {
                                                                                                                                            location.reload(); // Fallback: reload if card not found
                                                                                                                                        }
                                                                                                                                    } else {
                                                                                                                                        alert('Thất bại: ' + data.message);
                                                                                                                                    }
                                                                                                                                })
                                                                                                                                .catch(error => {
                                                                                                                                    console.error('Error:', error);
                                                                                                                                    alert('Đã xảy ra lỗi: ' + error.message);
                                                                                                                                });
                                                                                                                    }
                                                                                                                };
                                                                                                            });
        </script>
    </body>

</html>