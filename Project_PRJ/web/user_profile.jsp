<%@page import="model.Users"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>User Profile</title>
        <link rel="stylesheet" href="./css/style-prefix.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --gray: #6b7280;
                --dark: #111827;
                --green: #10b981;
            }

            body {
                font-family: 'Poppins', sans-serif;
                margin: 0;
            }

            .container-main {
                display: flex;
                max-width: 1100px;
                margin: 40px auto;
                background: white;
                border-radius: 16px;
                overflow: hidden;
                box-shadow: 0 10px 20px rgba(0,0,0,0.05);
            }

            .sidebar {
                width: 240px;
                background: #f3f4f6;
                padding: 30px 20px;
                border-right: 1px solid #e5e7eb;
            }

            .sidebar h3 {
                font-size: 20px;
                margin-bottom: 20px;
                font-weight: 600;
            }

            .sidebar ul {
                list-style: none;
                padding: 0;
            }

            .sidebar li {
                margin-bottom: 15px;
            }

            .sidebar button {
                background: none;
                border: none;
                font-size: 16px;
                color: var(--dark);
                cursor: pointer;
                width: 100%;
                text-align: left;
                padding: 10px;
                border-radius: 8px;
                transition: 0.2s;
            }

            .sidebar button:hover {
                background: var(--main-pink);
                color: white;
            }

            .content_main {
                flex: 1;
                padding: 40px;
            }

            .section {
                display: none;
            }

            .section.active {
                display: block;
            }

            .alert-success {
                background-color: #d1fae5;
                color: #065f46;
                padding: 15px 20px;
                border: 1px solid #10b981;
                border-radius: 8px;
                margin-bottom: 20px;
                font-weight: 500;
                animation: fadeIn 0.5s ease-in-out;
            }

            @keyframes fadeIn {
                from {
                    opacity: 0;
                    transform: translateY(-5px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .profile-header {
                display: flex;
                align-items: center;
                gap: 20px;
                margin-bottom: 30px;
            }

            .avatar-wrapper {
                position: relative;
                width: 90px;
                height: 90px;
            }

            .avatar-wrapper img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                border-radius: 50%;
                border: 3px solid var(--main-pink);
            }

            .change-avatar-btn {
                position: absolute;
                bottom: 0;
                left: 50%;
                transform: translateX(-50%);
                background: rgba(0, 0, 0, 0.5);
                color: white;
                font-size: 12px;
                padding: 4px 10px;
                cursor: pointer;
                border-radius: 12px;
                display: none;
                white-space: nowrap;
            }

            #avatarUpload {
                display: none;
            }

            .profile-info {
                display: grid;
                grid-template-columns: 150px 1fr;
                row-gap: 15px;
                column-gap: 20px;
            }

            .profile-info input {
                padding: 10px;
                border: 1px solid #ccc;
                border-radius: 6px;
                font-size: 14px;
                background: #f9fafb;
            }

            .profile-info input[readonly] {
                background: #f3f4f6;
                color: #888;
            }

            .profile-info label {
                font-weight: 500;
                color: var(--gray);
            }

            .btn-group {
                margin-top: 30px;
            }

            .edit-btn, .save-btn {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                border: none;
                padding: 10px 25px;
                font-weight: 600;
                color: white;
                border-radius: 30px;
                cursor: pointer;
                transition: 0.3s;
            }

            .edit-btn:hover, .save-btn:hover {
                opacity: 0.9;
            }

            ul.simple-list {
                padding-left: 20px;
                line-height: 1.8;
            }

            .alert-success {
                transition: opacity 0.4s ease, transform 0.4s ease;
            }

            .switch {
                position: relative;
                display: inline-block;
                width: 46px;
                height: 24px;
            }

            .switch input {
                opacity: 0;
                width: 0;
                height: 0;
            }

            .slider {
                position: absolute;
                cursor: pointer;
                background-color: #ccc;
                border-radius: 24px;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                transition: .4s;
            }

            .slider:before {
                content: "";
                position: absolute;
                height: 18px;
                width: 18px;
                left: 3px;
                bottom: 3px;
                background-color: white;
                border-radius: 50%;
                transition: .4s;
            }

            .switch input:checked + .slider {
                background-color: var(--main-orange);
            }

            .switch input:checked + .slider:before {
                transform: translateX(22px);
            }

            .modal_main.active {
                display: flex !important; /* hiện tức thì */
            }

            .modal_main {
                position: fixed;
                top: 0;
                left: 0;
                width: 100vw;
                height: 100vh;
                display: flex;
                align-items: center;
                justify-content: center;
                background: rgba(0,0,0,0.5);
                z-index: 9999;
            }

            .modal_main.hidden {
                display: none;
            }

            .modal_overlay {
                position: absolute;
                width: 100%;
                height: 100%;
                background: transparent;
                z-index: 1;
            }

            .modal_container {
                position: relative;
                z-index: 2;
                background: #fff;
                padding: 30px 25px;
                border-radius: 12px;
                width: 500px;
                max-width: 95%;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                animation: fadeInScale 0.25s ease-out;
            }

            @keyframes fadeInScale {
                from {
                    opacity: 0;
                    transform: scale(0.9);
                }
                to {
                    opacity: 1;
                    transform: scale(1);
                }
            }

            .modal_title {
                font-size: 20px;
                font-weight: 600;
                color: var(--dark);
                margin-bottom: 20px;
                text-align: center;
            }

            .modal_grid {
                display: grid;
                grid-template-columns: 130px 1fr;
                row-gap: 15px;
                column-gap: 15px;
            }

            .modal_grid input[type="text"] {
                padding: 10px;
                border: 1px solid #ccc;
                border-radius: 6px;
                font-size: 14px;
                background: #f9fafb;
            }

            .modal_actions {
                margin-top: 25px;
                display: flex;
                justify-content: space-between;
            }

            .modal_close {
                position: absolute;
                top: 12px;
                right: 15px;
                background: none;
                border: none;
                font-size: 22px;
                cursor: pointer;
                color: var(--gray);
            }

            .transaction-table {
                display: flex;
                flex-direction: column;
                gap: 12px;
            }

            .transaction-card {
                display: flex;
                justify-content: space-between;
                background: #fff;
                border: 1px solid #e5e7eb;
                border-left: 5px solid var(--main-pink);
                border-radius: 10px;
                padding: 16px 20px;
                box-shadow: 0 2px 8px rgba(0,0,0,0.03);
                transition: all 0.3s ease;
            }

            .transaction-card:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0,0,0,0.06);
            }

            .txn-left {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }

            .txn-date {
                font-weight: 500;
                font-size: 14px;
                color: #6b7280;
            }

            .txn-type {
                font-size: 16px;
                font-weight: 600;
                color: var(--dark);
            }

            .txn-desc {
                font-size: 13px;
                color: #9ca3af;
                font-style: italic;
            }

            .txn-right {
                display: flex;
                flex-direction: column;
                text-align: right;
                gap: 6px;
            }

            .txn-amount {
                font-size: 16px;
                font-weight: 700;
                color: var(--main-orange);
            }

            .txn-status {
                font-size: 13px;
                font-weight: 600;
                padding: 4px 8px;
                border-radius: 6px;
                display: inline-block;
                width: fit-content;
            }

            .txn-status.Completed {
                background: #d1fae5;
                color: #047857;
            }

            .txn-status.Pending {
                background: #fef3c7;
                color: #92400e;
            }

            .txn-status.Failed {
                background: #fee2e2;
                color: #b91c1c;
            }

            /* Nút lọc trạng thái giao dịch */
            .filter-btn {
                padding: 8px 16px;
                border: 1px solid #d1d5db;
                border-radius: 20px;
                font-size: 14px;
                font-weight: 500;
                color: #374151;
                background: #f3f4f6;
                cursor: pointer;
                transition: all 0.3s ease;
            }

            .filter-btn:not(.active):hover {
                background: #e5e7eb;
            }

            .filter-btn.active {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: white;
                border-color: transparent;
            }

            /* Phân trang */
            .pagination {
                display: flex;
                justify-content: center;
                margin-top: 25px;
                gap: 6px;
            }

            .pagination button {
                padding: 6px 12px;
                border: 1px solid #d1d5db;
                border-radius: 6px;
                background: white;
                color: #374151;
                cursor: pointer;
                transition: 0.3s;
                font-size: 14px;
            }

            .pagination button:hover {
                background: #f3f4f6;
            }

            .pagination button.active {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: white;
                border-color: transparent;
            }

            /* Trạng thái Order */
            .txn-status.Pending {
                background: #fef3c7;
                color: #92400e;
            }
            .txn-status.Processing {
                background: #e0f2fe;
                color: #0369a1;
            }
            .confirm-delivery-btn {
                background-color: var(--green);
                color: white;
                border: none;
                padding: 6px 14px;
                font-size: 13px;
                font-weight: 600;
                border-radius: 20px;
                cursor: pointer;
                transition: background-color 0.3s;
            }
            .confirm-delivery-btn:hover {
                background-color: #059669;
            }
            .cancel-order-btn {
                background-color: #e5e7eb; /* Màu xám nhạt */
                color: var(--dark);
                border: 1px solid #d1d5db;
                padding: 5px 12px;
                font-size: 13px;
                font-weight: 500;
                border-radius: 20px;
                cursor: pointer;
                transition: all 0.2s;
            }

            .cancel-order-btn:hover {
                background-color: #d1d5db;
                border-color: #9ca3af;
            }
            .txn-status.Delivered {
                background: #d1fae5;
                color: #047857;
            }
            .txn-status.Cancelled {
                background: #fee2e2;
                color: #b91c1c;
            }
            .txn-status.Returned {
                background: #f3e8ff;
                color: #7e22ce;
            }

            .pac-container {
                z-index: 10000 !important;
                position: absolute !important;
            }

            .pac-item {
                white-space: normal !important;
                word-break: break-word !important;
                line-height: 1.4 !important;
                padding: 8px 12px !important;
            }

            .view-detail-btn {
                display: inline-flex;
                align-items: center;
                gap: 6px; /* Khoảng cách giữa icon và chữ */
                margin-top: 8px;
                padding: 5px 12px;
                font-size: 13px;
                font-weight: 500;
                text-decoration: none;
                color: var(--main-pink);
                background-color: #fff1f2; /* Màu nền hồng rất nhạt */
                border: 1px solid #ffdde5;
                border-radius: 20px; /* Bo tròn để tạo hình con nhộng */
                transition: all 0.2s ease-in-out;
            }

            .view-detail-btn:hover {
                background-color: var(--main-pink);
                color: white;
                transform: translateY(-1px);
                box-shadow: 0 2px 8px rgba(255, 111, 145, 0.4);
            }

            .modal_container {
                position: relative;
                /* các thuộc tính khác */
            }

        </style>
    </head>
    <body>

        <jsp:include page="./sub/header.jsp" />

        <div class="container-main">
            <!-- Sidebar -->
            <div class="sidebar">
                <h3 style="padding-left: 20px; padding-top: 20px">My Account</h3>
                <ul>
                    <li><button onclick="showSection('info')">User Info</button></li>
                    <li><button onclick="showSection('address')">Addresses</button></li>
                    <li><button onclick="showSection('voucher')">Vouchers(Not done)</button></li>
                    <li><button onclick="showSection('wallet')">Wallet</button></li>
                    <li><button onclick="showSection('orders')">Orders</button></li>
                    <li><button onclick="showSection('membership')">Membership(Not done)</button></li>
                    <li>
                        <button onclick="location.href = 'shop'">My Shop</button>
                    </li>
                    <form id="logoutForm" action="logout" method="post" style="display: none;"></form>
                    <li>
                        <button onclick="document.getElementById('logoutForm').submit()">Logout</button>
                    </li>
                </ul>
            </div>

            <!-- Content -->
            <div class="content_main">
                <!-- Alert -->
                <c:if test="${not empty requestScope.success}">
                    <div class="alert-success">${requestScope.success}</div>
                    <c:remove var="success" scope="request"/>
                </c:if>

                <!-- Info -->
                <div id="info" class="section active">
                    <form method="post" action="profile?id=updateProfile" enctype="multipart/form-data" id="profileForm">
                        <div class="profile-header">
                            <div class="avatar-wrapper">
                                <img src="<c:choose>
                                         <c:when test="${not empty user.avatarUrl}">${user.avatarUrl}</c:when>
                                         <c:otherwise>images/logo_default.svg</c:otherwise>
                                     </c:choose>" alt="Avatar" id="avatarPreview">
                                <label for="avatarUpload" class="change-avatar-btn" id="changeAvatarBtn">Change</label>
                                <input type="file" name="avatar" id="avatarUpload" accept="image/*" onchange="previewAvatar(event)" disabled>
                            </div>
                            <h2>${user.username}</h2>
                        </div>
                        <div class="profile-info">
                            <label>Email:</label>
                            <input type="text" name="email" id="email" value="${user.email}" readonly>

                            <label>Phone:</label>
                            <input type="text" name="phone" id="phone" value="${user.phone}" readonly>

                            <label>Birthdate:</label>
                            <input type="date" name="dateOfBirth" id="dob" value="${user.dateOfBirth}" readonly>
                        </div>
                        <div class="btn-group">
                            <button type="button" class="edit-btn" onclick="enableEdit()">Edit Profile</button>
                            <button type="submit" class="save-btn" id="saveBtn" style="display: none;">Save</button>
                        </div>
                    </form>
                </div>

                <!-- Address Section -->
                <div id="address" class="section">
                    <h2>My Addresses</h2>
                    <!-- Danh sách địa chỉ -->
                    <c:forEach var="addr" items="${user.addressesList}" varStatus="i">
                        <div class="address-card"
                             style="margin-bottom: 20px; background: #f9fafb; padding: 16px; border-radius: 10px; box-shadow: 0 2px 6px rgba(0,0,0,0.05);">
                            <div style="display: grid; grid-template-columns: 150px 1fr; row-gap: 10px; column-gap: 15px;">
                                <label>Full Name:</label>
                                <div>${addr.fullName}</div>

                                <label>Phone Number:</label>
                                <div>${addr.phoneNumber}</div>

                                <label>Address Line:</label>
                                <div>${addr.addressLine}</div>

                                <label>Default:</label>
                                <div>
                                    <span style="color: ${addr.isDefault ? '#10b981' : '#999'}; font-weight: bold;">
                                        ${addr.isDefault ? "✔ Yes" : "No"}
                                    </span>
                                </div>
                            </div>
                            <div style="margin-top: 12px;">
                                <button type="button" class="edit-btn"
                                        onclick='openAddressModal(${addr.addressId}, "${addr.fullName}", "${addr.phoneNumber}", "${addr.addressLine}", ${addr.isDefault})'>
                                    Edit
                                </button>
                            </div>
                        </div>
                    </c:forEach>

                    <!-- Nút thêm địa chỉ mới -->
                    <div style="margin-top: 30px;">
                        <button type="button" class="edit-btn"
                                onclick="openAddressModal(null, '', '', '', false)">+ Add New Address</button>
                    </div>
                </div>

                <!-- Modal popup cho Edit/Add -->
                <div id="addressModal" class="modal_main hidden">
                    <div class="modal_overlay" onclick="closeAddressModal()"></div>
                    <form method="post" action="profile?id=updateAddress" id="addressForm" class="modal_container">
                        <input type="hidden" name="addressId" id="modalAddressId" />

                        <h3 class="modal_title">Edit Address</h3>
                        <div class="modal_grid">
                            <label for="modalFullName">Full Name:</label>
                            <input type="text" name="fullName" id="modalFullName" required>

                            <label for="modalPhone">Phone Number:</label>
                            <input type="text" name="phoneNumber" id="modalPhone" required>

                            <label for="modalAddressLine">Address Line:</label>
                            <input type="text" name="addressLine" id="modalAddressLine" required>

                            <label for="modalIsDefault">Set as Default:</label>
                            <label class="switch">
                                <input type="checkbox" name="isDefault" id="modalIsDefault">
                                <span class="slider round"></span>
                            </label>
                        </div>

                        <div class="modal_actions">
                            <button type="submit" class="save-btn">Save</button>
                            <button type="button" class="edit-btn" onclick="deleteAddress()">Delete</button>
                        </div>

                        <button type="button" class="modal_close" onclick="closeAddressModal()">×</button>
                    </form>
                </div>


                <!-- Vouchers -->
                <div id="voucher" class="section">
                    <h2>My Vouchers</h2>
                    <ul class="simple-list">

                    </ul>
                </div>


                <!-- Wallet Section -->
                <div id="wallet" class="section">
                    <h2 style="margin-bottom: 25px;">My Wallet</h2>
                    <!-- Wallet Info -->
                    <div class="profile-info" style="margin-bottom: 40px;">
                        <label>Balance:</label>
                        <input type="text" value="${user.wallets.balance} ₫" readonly>
                    </div>

                    <!-- Filter Buttons -->
                    <div style="margin-bottom: 25px; display: flex; gap: 12px;">
                        <button class="filter-btn active" onclick="filterTransactions('all')">All</button>
                        <button class="filter-btn" onclick="filterTransactions('Completed')">Completed</button>
                        <button class="filter-btn" onclick="filterTransactions('Pending')">Pending</button>
                        <button class="filter-btn" onclick="filterTransactions('Failed')">Failed</button>
                    </div>

                    <!-- Transaction History -->
                    <div id="transactionList" class="transaction-table">
                        <c:forEach var="txn" items="${user.wallets.walletTransactionsList}" varStatus="loop">
                            <div class="transaction-card" data-status="${txn.status}" data-index="${loop.index}">
                                <div class="txn-left">
                                    <div class="txn-date">${txn.transactionDate}</div>
                                    <div class="txn-type">${txn.transactionType}</div>
                                    <div class="txn-desc">${empty txn.description ? '-' : txn.description}</div>
                                </div>
                                <div class="txn-right">
                                    <div class="txn-amount">${txn.amount} ₫</div>
                                    <div class="txn-status ${txn.status}">${txn.status}</div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="pagination" id="paginationControls"></div>
                    <!-- Pagination -->
                    <div id="pagination" style="margin-top: 20px; text-align: center;"></div>
                </div>

                <!-- Orders Section -->
                <div id="orders" class="section">
                    <h2 style="margin-bottom: 25px;">My Orders</h2>

                    <!-- Filter Buttons -->
                    <div style="margin-bottom: 25px; display: flex; gap: 12px;">
                        <button class="filter-btn" onclick="filterOrders('Pending')">Pending</button>
                        <button class="filter-btn" onclick="filterOrders('Processing')">Processing</button>
                        <button class="filter-btn" onclick="filterOrders('Shipped')">Shipped</button>
                        <button class="filter-btn" onclick="filterOrders('Delivered')">Delivered</button>
                        <button class="filter-btn" onclick="filterOrders('Cancelled')">Cancelled</button>
                        <button class="filter-btn" onclick="filterOrders('Returned')">Returned</button>
                    </div>
                    <!-- Order List -->
                    <div id="orderList" class="transaction-table">
                        <c:forEach var="order" items="${user.ordersList}" varStatus="loop">
                            <div class="transaction-card" data-status="${order.status}" data-index="${loop.index}" style="text-decoration: none;">
                                <div class="txn-left">
                                    <div class="txn-date">${order.createdAt}</div>
                                    <div class="txn-type">Order #${order.orderNumber}</div>
                                    <div class="txn-desc">Shipping Address ID: ${order.shippingAddress}</div>
                                </div>
                                <div class="txn-right">
                                    <div class="txn-amount">${order.totalAmount} ₫</div>
                                    <c:choose>
                                        <c:when test="${order.status == 'Shipped'}">
                                            <button class="confirm-delivery-btn" 
                                                    onclick="confirmDelivery(${order.orderId})">
                                                Xác nhận đã nhận
                                            </button>
                                        </c:when>
                                        <c:when test="${order.status == 'Pending' || order.status == 'Processing'}">
                                            <button class="cancel-order-btn" onclick="cancelOrder(${order.orderId})">
                                                <i class="fas fa-times"></i> Hủy đơn
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="txn-status ${order.status}">${order.status}</div>
                                        </c:otherwise>
                                    </c:choose>
                                    <a href="order-detail?id=${order.orderId}" class="view-detail-btn">
                                        <i class="fas fa-eye"></i> Xem chi tiết
                                    </a>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                    <div class="pagination" id="orderPaginationControls"></div>
                </div>
                <!-- Membership -->
                <div id="membership" class="section">
                    <h2>Membership</h2>
                    <ul class="simple-list">

                    </ul>
                </div>




            </div>
        </div>

        <jsp:include page="./sub/footer.jsp" />

        <!-- Scripts -->
        <script>
            function showSection(id) {
                document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
                document.getElementById(id).classList.add('active');
            }

            function enableEdit() {
                ['email', 'phone', 'dob'].forEach(id => {
                    document.getElementById(id).removeAttribute('readonly');
                });
                document.getElementById('avatarUpload').disabled = false;
                document.getElementById('changeAvatarBtn').style.display = 'block';
                document.querySelector('.edit-btn').style.display = 'none';
                document.getElementById('saveBtn').style.display = 'inline-block';
            }

            function previewAvatar(event) {
                const [file] = event.target.files;
                if (file) {
                    document.getElementById('avatarPreview').src = URL.createObjectURL(file);
                }
            }

            window.addEventListener("DOMContentLoaded", () => {
                const alertBox = document.querySelector(".alert-success");
                if (alertBox) {
                    setTimeout(() => {
                        alertBox.style.opacity = '0';
                        alertBox.style.transform = 'translateY(-10px)';
                        setTimeout(() => alertBox.remove(), 500); // đợi hiệu ứng hoàn tất rồi xóa khỏi DOM
                    }, 3000);
                }
            });

            function openAddressModal(id, name, phone, addressLine, isDefault) {
                // Gán dữ liệu vào form
                document.getElementById('modalAddressId').value = id || -1;
                document.getElementById('modalFullName').value = name || '';
                document.getElementById('modalPhone').value = phone || '';
                document.getElementById('modalAddressLine').value = addressLine || '';
                document.getElementById('modalIsDefault').checked = isDefault === true;

                // Reset form action (mặc định là update)
                document.getElementById("addressForm").action = "profile?id=updateAddress";

                // Hiển thị modal
                const modal = document.getElementById('addressModal');
                modal.classList.add('active');

                setTimeout(() => {
                    initAddressAutocomplete();
                }, 200);

            }

            function closeAddressModal() {
                const modal = document.getElementById('addressModal');
                modal.classList.remove('active');

                // Reset form dữ liệu khi đóng
                document.getElementById('addressForm').reset();
            }

            function deleteAddress() {
                const id = document.getElementById('modalAddressId').value;
                if (id) {
                    if (confirm("Are you sure you want to delete this address?")) {
                        const form = document.getElementById("addressForm");
                        form.action = "profile?id=deleteAddress";
                        form.submit();
                    }
                } else {
                    alert("This address has not been saved yet.");
                }
            }

            const ITEMS_PER_PAGE = 8;
            let currentFilter = 'all';
            let currentPage = 1;

            function filterTransactions(status) {
                currentFilter = status.toLowerCase();
                currentPage = 1;
                updateTransactionDisplay();
                updatePaginationControls();

                // Cập nhật trạng thái nút active
                const buttons = document.querySelectorAll('.filter-btn');
                buttons.forEach(btn => {
                    const btnText = btn.textContent.trim().toLowerCase();
                    if (btnText === status.toLowerCase()) {
                        btn.classList.add('active');
                    } else {
                        btn.classList.remove('active');
                    }
                });
            }

            function updateTransactionDisplay() {
                const allTransactions = Array.from(document.querySelectorAll('.transaction-card'));
                let visibleTransactions = [];

                allTransactions.forEach(txn => {
                    const txnStatus = txn.getAttribute('data-status')?.toLowerCase() || '';
                    if (currentFilter === 'all' || txnStatus === currentFilter) {
                        txn.style.display = 'flex'; // tạm thời hiển thị để tính
                        visibleTransactions.push(txn);
                    } else {
                        txn.style.display = 'none';
                    }
                });

                // Ẩn tất cả trước khi hiển thị theo trang
                visibleTransactions.forEach(txn => txn.style.display = 'none');

                const start = (currentPage - 1) * ITEMS_PER_PAGE;
                const end = start + ITEMS_PER_PAGE;
                visibleTransactions.slice(start, end).forEach(txn => txn.style.display = 'flex');
            }

            function updatePaginationControls() {
                const container = document.getElementById('paginationControls');
                const allTransactions = Array.from(document.querySelectorAll('.transaction-card'));
                let filtered = allTransactions.filter(txn => {
                    const txnStatus = txn.getAttribute('data-status')?.toLowerCase() || '';
                    return currentFilter === 'all' || txnStatus === currentFilter;
                });

                const totalPages = Math.ceil(filtered.length / ITEMS_PER_PAGE);
                container.innerHTML = '';

                if (totalPages <= 1)
                    return;

                for (let i = 1; i <= totalPages; i++) {
                    const btn = document.createElement('button');
                    btn.textContent = i;
                    btn.classList.toggle('active', i === currentPage);
                    btn.onclick = () => {
                        currentPage = i;
                        updateTransactionDisplay();
                        updatePaginationControls();
                    };
                    container.appendChild(btn);
                }
            }

            // Kích hoạt mặc định khi load
            document.addEventListener('DOMContentLoaded', () => {
                filterTransactions('all');
            });

            // === Order Filter & Pagination ===
            const ORDER_ITEMS_PER_PAGE = 8;
            let currentOrderFilter = 'all';
            let currentOrderPage = 1;

            function filterOrders(status) {
                currentOrderFilter = status.toLowerCase();
                currentOrderPage = 1;
                updateOrderDisplay();
                updateOrderPaginationControls();

                // Cập nhật trạng thái active cho nút
                document.querySelectorAll('.filter-btn').forEach(btn => {
                    btn.classList.remove('active');
                });

                document.querySelectorAll('.filter-btn').forEach(btn => {
                    if (btn.textContent.trim().toLowerCase() === status.toLowerCase()) {
                        btn.classList.add('active');
                    }
                });
            }

            function updateOrderDisplay() {
                const allOrders = Array.from(document.querySelectorAll('#orderList .transaction-card'));
                let visibleOrders = [];

                allOrders.forEach(order => {
                    const status = order.getAttribute('data-status')?.toLowerCase() || '';
                    if (currentOrderFilter === 'all' || status === currentOrderFilter) {
                        order.style.display = 'flex';
                        visibleOrders.push(order);
                    } else {
                        order.style.display = 'none';
                    }
                });

                // Ẩn tất cả trước khi hiển thị phân trang
                visibleOrders.forEach(order => order.style.display = 'none');

                const start = (currentOrderPage - 1) * ORDER_ITEMS_PER_PAGE;
                const end = start + ORDER_ITEMS_PER_PAGE;
                visibleOrders.slice(start, end).forEach(order => order.style.display = 'flex');
            }

            function updateOrderPaginationControls() {
                const container = document.getElementById('orderPaginationControls');
                const allOrders = Array.from(document.querySelectorAll('#orderList .transaction-card'));
                const filtered = allOrders.filter(order => {
                    const status = order.getAttribute('data-status')?.toLowerCase() || '';
                    return currentOrderFilter === 'all' || status === currentOrderFilter;
                });

                const totalPages = Math.ceil(filtered.length / ORDER_ITEMS_PER_PAGE);
                container.innerHTML = '';

                if (totalPages <= 1)
                    return;

                for (let i = 1; i <= totalPages; i++) {
                    const btn = document.createElement('button');
                    btn.textContent = i;
                    btn.classList.toggle('active', i === currentOrderPage);
                    btn.onclick = () => {
                        currentOrderPage = i;
                        updateOrderDisplay();
                        updateOrderPaginationControls();
                    };
                    container.appendChild(btn);
                }
            }

            // Tự động load khi đến Orders section
            document.addEventListener('DOMContentLoaded', () => {
                filterOrders('Pending');
            });

            function confirmDelivery(orderId) {
                if (confirm('Bạn xác nhận đã nhận được đơn hàng này?')) {
                    // Chuyển hướng đến servlet để cập nhật trạng thái
                    window.location.href = 'update-order-status?id=' + orderId + '&status=Delivered';
                }
            }

        </script>

    </body>
</html>
