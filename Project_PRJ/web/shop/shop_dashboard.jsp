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
                height: 100px;
                background: #fff;
                border-radius: 16px;
                padding: 20px;
                box-shadow: 0 4px 16px rgba(0,0,0,0.05);
                min-height: fit-content; /* Allow sidebar to grow with content */
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

            /* --- Table Section Styles --- */
            .table-section {
                padding: 20px;
                border: 1px solid #e5e7eb;
                border-radius: 12px;
            }
            .table-title {
                font-size: 18px;
                font-weight: 600;
                margin-bottom: 20px;
            }
            .product-list table {
                width: 100%;
                border-collapse: collapse;
            }
            .product-list th {
                background-color: #f9fafb;
                font-size: 14px;
                text-align: left;
                color: var(--gray);
                font-weight: 500;
                position: relative;
            }
            .product-list th.sortable {
                cursor: pointer;
                user-select: none;
            }
            .product-list th.sortable:hover {
                background-color: #f1f1f1;
            }
            .product-list th .sort-icon {
                margin-left: 8px;
                color: #9ca3af;
            }
            .product-list td, .product-list th {
                padding: 12px 16px;
                border-bottom: 1px solid #f1f1f1;
            }
            .product-list tr:last-child td {
                border-bottom: none;
            }
            .product-list .product-cell {
                display: flex;
                align-items: center;
                gap: 12px;
            }
            .product-list img {
                width: 40px;
                height: 40px;
                object-fit: cover;
                border-radius: 6px;
            }

            /* --- Pagination Styles --- */
            .pagination {
                display: flex;
                justify-content: center;
                gap: 8px;
                margin-top: 20px;
            }
            .pagination button {
                padding: 8px 14px;
                background: white;
                color: var(--dark);
                border: 1px solid #d1d5db;
                border-radius: 8px;
                cursor: pointer;
                font-weight: 500;
                transition: all 0.3s;
            }
            .pagination button:hover {
                border-color: var(--main-pink);
            }
            .pagination button.active {
                background: var(--main-pink);
                color: white;
                border-color: var(--main-pink);
            }

            /* Responsive */
            @media (max-width: 992px) {
                .dashboard-wrapper {
                    flex-direction: column;
                }
                .sidebar {
                    width: 100%;
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
                        <a href="manageProducts">Tất cả sản phẩm</a>
                        <a href="manageProducts?action=add">Thêm sản phẩm</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-file-invoice"></i> Quản lý đơn hàng</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="manageOrders">Xem tất cả đơn hàng</a>
                        <a href="manageOrders?status=pending">Đơn chờ xác nhận</a>
                        <a href="manageOrders?status=processing">Đơn đang xử lý</a>
                        <a href="manageOrders?status=pending">Đơn đang giao</a>
                        <a href="manageOrders?status=delivered">Đơn giao thành công</a>
                        <a href="manageOrders?status=cancelled">Đơn hủy và trả hàng</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-headset"></i> Chăm sóc khách hàng</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="chat">Quản lý chat</a>
                        <a href="reviewManager">Quản lý đánh giá</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-wallet"></i> Tài chính</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="wallet">Số dư Wallet</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-chart-line"></i> Dữ liệu</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="analytics">Phân tích bán hàng</a>
                        <a href="performance">Hiệu quả hoạt động</a>
                    </div>
                </div>

                <div class="dropdown">
                    <button class="dropdown-toggle">
                        <span><i class="fas fa-store"></i> Quản lý shop</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="profile">Hồ sơ shop</a>
                    </div>
                </div>

            </div>

            <main class="dashboard-container">
                <h1 class="dashboard-title"><i class="fas fa-chart-pie"></i> Phân Tích Bán Hàng</h1>

                <section class="stats-grid">
                    <div class="stat-card">
                        <div class="title">Tổng Doanh Thu</div>
                        <div class="value"><fmt:formatNumber value="${totalRevenue}" type="currency" currencySymbol="₫" pattern="#,##0₫"/></div>
                        <div class="change positive">+${revenueChangePercent}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">Tổng Đơn Hàng</div>
                        <div class="value">${totalOrders}</div>
                        <div class="change positive">+${ordersChangePercent}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">TB. Giá trị Đơn hàng</div>
                        <div class="value"><fmt:formatNumber value="${avgOrderValue}" type="currency" currencySymbol="₫" pattern="#,##0₫"/></div>
                        <div class="change negative">-${avgOrderValueChangePercent}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">Sản phẩm đã bán</div>
                        <div class="value">${totalProductsSold}</div>
                        <div class="change positive">+${productsSoldCount}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">Đơn hàng hôm nay</div>
                        <div class="value">${todayOrders}</div>
                        <div class="change positive">+${revenueChangePercent}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">Đơn hàng chờ xác nhận</div>
                        <div class="value">${pendingOrders}</div>
                        <div class="change positive">+${revenueChangePercent}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">Đơn hàng đang xử lí</div>
                        <div class="value">${processingOrders}</div>
                        <div class="change positive">+${revenueChangePercent}% so với kỳ trước</div>
                    </div>
                    <div class="stat-card">
                        <div class="title">Số lượng view</div>
                        <div class="value">${numberOfView}</div>
                        <div class="change positive">+${revenueChangePercent}% so với kỳ trước</div>
                    </div>
                </section>

                <section class="chart-section">
                    <div class="chart-header">
                        <div class="chart-title">Doanh Thu Theo Thời Gian</div>
                        <select id="timeFilter">
                            <option value="7">7 ngày qua</option>
                            <option value="30" selected>30 ngày qua</option>
                            <option value="365">1 năm qua</option>
                        </select>
                    </div>
                    <canvas id="revenueChart"></canvas>
                </section>

                <section class="table-section">
                    <div class="table-title">Top sản phẩm bán chạy</div>
                    <div class="product-list">
                        <table id="productTable">
                            <thead>
                                <tr>
                                    <th style="width: 60px;">#</th>
                                    <th>Sản phẩm</th>
                                    <th class="sortable" data-sort-by="quantity">Đã bán<i class="fas fa-sort sort-icon"></i></th>
                                    <th class="sortable" data-sort-by="revenue">Doanh thu<i class="fas fa-sort sort-icon"></i></th>
                                </tr>
                            </thead>
                            <tbody id="productTableBody">
                                <c:forEach var="item" items="${revenueByProduct}" varStatus="loop">
                                    <tr data-quantity="${item.quantitySold}" data-revenue="${item.revenue}">
                                        <td>${loop.index + 1}</td>
                                        <td>
                                            <div class="product-cell">
                                                <img src="${item.imageUrl}" alt="${item.productName}">
                                                <span>${item.productName}</span>
                                            </div>
                                        </td>
                                        <td>${item.quantitySold}</td>
                                        <td><fmt:formatNumber value="${item.revenue}" type="currency" currencySymbol="₫" pattern="#,##0₫"/></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <div class="pagination" id="pagination"></div>
                    </div>
                </section>
            </main>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
            document.addEventListener('DOMContentLoaded', function () {
                // Define toggleDropdown function
                function toggleDropdown(button) {
                    const parentDropdown = button.closest('.dropdown');
                    const dropdownMenu = parentDropdown.querySelector('.dropdown-menu');
                    button.classList.toggle('active');
                    dropdownMenu.classList.toggle('active');
                }

                // Attach event listeners to dropdown buttons
                document.querySelectorAll('.sidebar .dropdown-toggle').forEach(button => {
                    button.addEventListener('click', () => toggleDropdown(button));
                });

                // --- Chart Section ---
                let revenueJsonData;
                try {
                    revenueJsonData = {
                        "7": JSON.parse('${revenueJson7}'),
                        "30": JSON.parse('${revenueJson30}'),
                        "365": JSON.parse('${revenueJson365}')
                    };
                } catch (e) {
                    console.error('Error parsing revenue JSON data:', e);
                    revenueJsonData = {"7": {}, "30": {}, "365": {}}; // Fallback empty data
                }

                const ctx = document.getElementById('revenueChart').getContext('2d');
                let myChart;
                function renderChart(data) {
                    const gradient = ctx.createLinearGradient(0, 0, 0, 350);
                    gradient.addColorStop(0, 'rgba(255, 111, 145, 0.5)');
                    gradient.addColorStop(1, 'rgba(255, 111, 145, 0)');
                    const chartData = {
                        labels: Object.keys(data),
                        datasets: [{
                                label: 'Doanh Thu',
                                data: Object.values(data),
                                fill: true,
                                backgroundColor: gradient,
                                borderColor: 'rgba(255, 111, 145, 1)',
                                pointBackgroundColor: 'rgba(255, 111, 145, 1)',
                                pointBorderColor: '#fff',
                                pointHoverRadius: 7,
                                tension: 0.4
                            }]
                    };
                    if (myChart) {
                        myChart.data = chartData;
                        myChart.update();
                    } else {
                        myChart = new Chart(ctx, {
                            type: 'line',
                            data: chartData,
                            options: {
                                responsive: true,
                                maintainAspectRatio: false,
                                plugins: {
                                    legend: {display: false},
                                    tooltip: {
                                        callbacks: {
                                            label: function (context) {
                                                let label = context.dataset.label || '';
                                                if (label) {
                                                    label += ': ';
                                                }
                                                if (context.parsed.y !== null) {
                                                    label += new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(context.parsed.y);
                                                }
                                                return label;
                                            }
                                        }
                                    }
                                },
                                scales: {
                                    y: {
                                        beginAtZero: true,
                                        ticks: {
                                            callback: function (value) {
                                                return new Intl.NumberFormat('vi-VN').format(value / 1000) + 'k';
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }

                document.getElementById('timeFilter').addEventListener('change', function () {
                    renderChart(revenueJsonData[this.value]);
                });

                if (document.getElementById('revenueChart')) {
                    renderChart(revenueJsonData['30']);
                }

                // --- Table Sorting & Pagination ---
                const tablePaginator = {
                    rowsPerPage: 5,
                    currentPage: 1,
                    tableBody: document.getElementById("productTableBody"),
                    paginationContainer: document.getElementById("pagination"),
                    rows: [],
                    init() {
                        if (!this.tableBody)
                            return;
                        this.rows = Array.from(this.tableBody.querySelectorAll("tr"));
                        if (this.rows.length === 0)
                            return;
                        this.displayPage(1);
                    },
                    displayPage(page) {
                        this.currentPage = page;
                        this.rows.forEach(row => row.style.display = "none");
                        const start = (page - 1) * this.rowsPerPage;
                        const end = start + this.rowsPerPage;
                        this.rows.slice(start, end).forEach(row => row.style.display = "");
                        this.updatePaginationButtons();
                    },
                    updatePaginationButtons() {
                        const totalPages = Math.ceil(this.rows.length / this.rowsPerPage);
                        this.paginationContainer.innerHTML = "";
                        if (totalPages <= 1)
                            return;
                        for (let i = 1; i <= totalPages; i++) {
                            const btn = document.createElement("button");
                            btn.textContent = i;
                            btn.classList.toggle("active", i === this.currentPage);
                            btn.onclick = () => this.displayPage(i);
                            this.paginationContainer.appendChild(btn);
                        }
                    },
                    updateRows(newRows) {
                        this.rows = newRows;
                        this.tableBody.innerHTML = "";
                        this.rows.forEach(row => this.tableBody.appendChild(row));
                        this.displayPage(1);
                    }
                };
                tablePaginator.init();

                document.querySelectorAll('th.sortable').forEach(header => {
                    header.addEventListener('click', () => {
                        const sortBy = header.dataset.sortBy;
                        const currentDirection = header.dataset.direction || 'desc';
                        const newDirection = currentDirection === 'desc' ? 'asc' : 'desc';
                        const sortedRows = Array.from(tablePaginator.rows).sort((a, b) => {
                            const valA = parseFloat(a.dataset[sortBy]);
                            const valB = parseFloat(b.dataset[sortBy]);
                            return newDirection === 'asc' ? valA - valB : valB - valA;
                        });
                        tablePaginator.updateRows(sortedRows);
                        document.querySelectorAll('th.sortable').forEach(th => {
                            th.dataset.direction = '';
                            th.querySelector('.sort-icon').className = 'fas fa-sort sort-icon';
                        });
                        header.dataset.direction = newDirection;
                        header.querySelector('.sort-icon').className = newDirection === 'asc' ? 'fas fa-sort-up sort-icon' : 'fas fa-sort-down sort-icon';
                    });
                });
            });
        </script>
    </body>
</html>