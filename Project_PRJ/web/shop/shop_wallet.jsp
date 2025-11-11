<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Ví của tôi</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --gray: #6b7280;
                --dark: #111827;
                --green: #10b981;
                --yellow: #f59e0b;
                --red: #ef4444;
                --bg-light: #f9fafb;
            }

            body {
                font-family: 'Poppins', sans-serif;
                margin: 0;
                background-color: var(--bg-light);
                padding: 40px 20px;
            }

            .page-container {
                max-width: 900px;
                margin: auto;
                background: white;
                padding: 40px;
                border-radius: 16px;
                box-shadow: 0 10px 20px rgba(0,0,0,0.05);
            }

            .page-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 30px;
            }

            .page-title {
                font-size: 24px;
                font-weight: 600;
                color: var(--dark);
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .back-btn {
                padding: 10px 18px;
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: #fff;
                border: none;
                border-radius: 30px;
                font-weight: 500;
                cursor: pointer;
                text-decoration: none;
                transition: all 0.3s;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }

            .back-btn:hover {
                opacity: 0.9;
                transform: translateY(-2px);
            }

            /* --- Wallet Specific Styles --- */
            .balance-card {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: white;
                padding: 25px;
                border-radius: 12px;
                margin-bottom: 30px;
                box-shadow: 0 8px 20px rgba(255, 111, 145, 0.3);
            }

            .balance-card .label {
                font-size: 16px;
                opacity: 0.9;
                margin-bottom: 5px;
            }

            .balance-card .amount {
                font-size: 32px;
                font-weight: 700;
            }

            .filter-btn-group {
                display: flex;
                gap: 12px;
                margin-bottom: 25px;
            }

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

            .filter-btn:hover:not(.active) {
                background: #e5e7eb;
            }

            .filter-btn.active {
                background: var(--dark);
                color: white;
                border-color: var(--dark);
            }

            /* Transaction List */
            .transaction-list {
                display: flex;
                flex-direction: column;
                gap: 12px;
            }

            .transaction-card {
                display: flex;
                justify-content: space-between;
                align-items: center;
                background: #fff;
                border: 1px solid #e5e7eb;
                border-left: 5px solid var(--main-pink);
                border-radius: 10px;
                padding: 16px 20px;
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
                color: var(--gray);
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
                align-items: flex-end;
            }

            .txn-amount {
                font-size: 16px;
                font-weight: 700;
                color: var(--main-orange);
            }

            .txn-amount.positive {
                color: var(--green);
            }

            .txn-status {
                font-size: 12px;
                font-weight: 600;
                padding: 4px 10px;
                border-radius: 16px;
                color: white;
                width: fit-content;
            }

            .txn-status.completed {
                background: var(--green);
            }
            .txn-status.pending {
                background: var(--yellow);
            }
            .txn-status.failed {
                background: var(--red);
            }

            .empty-state {
                text-align: center;
                padding: 40px;
                background: var(--bg-light);
                border-radius: 10px;
                color: var(--gray);
            }

            /* Pagination */
            .pagination {
                display: flex;
                justify-content: center;
                margin-top: 25px;
                gap: 6px;
            }

            .pagination button {
                padding: 8px 14px;
                border: 1px solid #d1d5db;
                border-radius: 6px;
                background: white;
                color: #374151;
                cursor: pointer;
                transition: 0.3s;
                font-size: 14px;
                font-weight: 500;
            }

            .pagination button:hover {
                background: #f3f4f6;
            }

            .pagination button.active {
                background: var(--dark);
                color: white;
                border-color: var(--dark);
            }

        </style>
    </head>
    <body>
        <%-- Giả sử bạn đã có đối tượng user trong session --%>
        <c:set var="user" value="${sessionScope.user}" />

        <div class="page-container">
            <div class="page-header">
                <div class="page-title"><i class="fas fa-wallet"></i> Ví của tôi</div>
                <a href="dash-board" class="back-btn"><i class="fas fa-arrow-left"></i> Quay lại Dashboard</a>
            </div>

            <div class="balance-card">
                <div class="label">Số dư hiện tại</div>
                <div class="amount">
                    <fmt:formatNumber value="${user.wallets.balance}" type="currency" currencySymbol="₫" pattern="#,##0 ₫"/>
                </div>
            </div>

            <div class="filter-btn-group">
                <button class="filter-btn active" onclick="filterTransactions('all', this)">Tất cả</button>
                <button class="filter-btn" onclick="filterTransactions('completed', this)">Hoàn thành</button>
                <button class="filter-btn" onclick="filterTransactions('pending', this)">Đang chờ</button>
                <button class="filter-btn" onclick="filterTransactions('failed', this)">Thất bại</button>
            </div>

            <div class="transaction-list" id="transactionList">
                <c:choose>
                    <c:when test="${not empty user.wallets.walletTransactionsList}">
                        <c:forEach var="txn" items="${user.wallets.walletTransactionsList}">
                            <div class="transaction-card" data-status="${txn.status}">
                                <div class="txn-left">
                                    <div class="txn-date"><fmt:formatDate value="${txn.transactionDate}" pattern="HH:mm, dd/MM/yyyy"/></div>
                                    <div class="txn-type">${txn.transactionType}</div>
                                    <div class="txn-desc">${empty txn.description ? 'Không có mô tả' : txn.description}</div>
                                </div>
                                <div class="txn-right">
                                    <div class="txn-amount ${txn.amount > 0 ? 'positive' : ''}">
                                        <fmt:formatNumber value="${txn.amount}" type="currency" currencySymbol="₫" pattern="#,##0 ₫"/>
                                    </div>
                                    <div class="txn-status ${txn.status}">${txn.status}</div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state">
                            <p><i class="fas fa-exchange-alt"></i> Bạn chưa có giao dịch nào.</p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="pagination" id="paginationControls"></div>
        </div>

        <script>
            const ITEMS_PER_PAGE = 5;
            let currentFilter = 'all';
            let currentPage = 1;

            function filterTransactions(status, clickedButton) {
                currentFilter = status;
                currentPage = 1;
                updateTransactionDisplay();
                updatePaginationControls();

                // Update active class on filter buttons
                document.querySelectorAll('.filter-btn').forEach(btn => btn.classList.remove('active'));
                clickedButton.classList.add('active');
            }

            function updateTransactionDisplay() {
                const allCards = document.querySelectorAll('.transaction-card');
                if (!allCards.length)
                    return;

                const visibleCards = Array.from(allCards).filter(card => {
                    return currentFilter === 'all' || card.dataset.status === currentFilter;
                });

                // Hide all cards first
                allCards.forEach(card => card.style.display = 'none');

                const start = (currentPage - 1) * ITEMS_PER_PAGE;
                const end = start + ITEMS_PER_PAGE;

                visibleCards.slice(start, end).forEach(card => card.style.display = 'flex');
            }

            function updatePaginationControls() {
                const paginationContainer = document.getElementById('paginationControls');
                const allCards = document.querySelectorAll('.transaction-card');
                if (!allCards.length) {
                    paginationContainer.innerHTML = '';
                    return;
                }

                const filteredCards = Array.from(allCards).filter(card => {
                    return currentFilter === 'all' || card.dataset.status === currentFilter;
                });

                const totalPages = Math.ceil(filteredCards.length / ITEMS_PER_PAGE);
                paginationContainer.innerHTML = ''; // Clear old buttons

                if (totalPages <= 1)
                    return;

                for (let i = 1; i <= totalPages; i++) {
                    const btn = document.createElement('button');
                    btn.textContent = i;
                    if (i === currentPage) {
                        btn.classList.add('active');
                    }
                    btn.onclick = () => {
                        currentPage = i;
                        updateTransactionDisplay();

                        // Update active state on pagination buttons
                        paginationContainer.querySelectorAll('button').forEach(pBtn => pBtn.classList.remove('active'));
                        btn.classList.add('active');
                    };
                    paginationContainer.appendChild(btn);
                }
            }

            // Initial load
            document.addEventListener('DOMContentLoaded', () => {
                updateTransactionDisplay();
                updatePaginationControls();
            });
        </script>
    </body>
</html>