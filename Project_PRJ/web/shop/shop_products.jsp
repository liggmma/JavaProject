<%@page import="java.util.ArrayList"%>
<%@page import="model.Categories"%>
<%@page import="java.util.List"%>
<%@page import="service.CategoryService"%>
<%@page import="model.Products"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quản Lý Sản Phẩm</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --bg-light: #f9fafb;
                --gray: #6b7280;
                --dark: #111827;
                --green: #10b981;
                --red: #ef4444;
                --blue: #3b82f6;
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
            }
            body {
                font-family: 'Poppins', sans-serif;
                background: var(--bg-light);
                margin: 0;
                padding: 30px;
            }
            .page-container {
                max-width: 1200px;
                margin: auto;
                background: #fff;
                border-radius: 16px;
                padding: 30px;
                box-shadow: 0 10px 20px rgba(0,0,0,0.06);
            }
            .page-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 25px;
                padding-bottom: 20px;
                border-bottom: 1px solid #e5e7eb;
            }
            .page-title {
                font-size: 24px;
                font-weight: 600;
                display: flex;
                align-items: center;
                gap: 12px;
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

            /* --- Filter Bar Styles --- */
            .filter-bar {
                display: flex;
                gap: 15px;
                margin-bottom: 25px;
                flex-wrap: wrap;
            }
            .filter-bar input, .filter-bar select {
                padding: 10px 14px;
                border: 1px solid #d1d5db;
                border-radius: 8px;
                font-size: 14px;
                font-family: 'Poppins', sans-serif;
            }
            .filter-bar input[type="text"] {
                flex-grow: 1;
                min-width: 250px;
            }
            .filter-bar select {
                background-color: #fff;
                cursor: pointer;
            }

            /* --- Table Styles --- */
            .product-table {
                width: 100%;
                border-collapse: collapse;
            }
            .product-table th {
                background-color: #f9fafb;
                font-size: 14px;
                text-align: left;
                color: var(--gray);
                font-weight: 600;
                padding: 12px 16px;
                border-bottom: 2px solid #e5e7eb;
                user-select: none;
            }
            .product-table th.sortable {
                cursor: pointer;
            }
            .product-table th.sortable:hover {
                background-color: #f1f1f1;
            }
            .product-table th .sort-icon {
                margin-left: 8px;
                color: #9ca3af;
            }
            .product-table td {
                padding: 12px 16px;
                border-bottom: 1px solid #f1f1f1;
                vertical-align: middle;
                font-size: 14px;
            }
            .product-table tbody tr:hover {
                background-color: #fef2f2;
            }
            .product-cell {
                display: flex;
                align-items: center;
                gap: 12px;
            }
            .product-cell img {
                width: 50px;
                height: 50px;
                object-fit: cover;
                border-radius: 8px;
                flex-shrink: 0;
            }
            .product-cell .product-name {
                font-weight: 500;
                color: var(--dark);
            }
            .status-badge {
                padding: 4px 10px;
                border-radius: 16px;
                font-size: 12px;
                font-weight: 500;
                color: white;
            }
            .status-badge.active {
                background-color: var(--green);
            }
            .status-badge.hidden {
                background-color: var(--gray);
            }
            .action-buttons {
                display: flex;
                gap: 10px;
            }
            .action-btn {
                background: none;
                border: none;
                cursor: pointer;
                font-size: 18px;
                color: var(--gray);
                transition: color 0.2s;
            }
            .action-btn.edit:hover {
                color: var(--blue);
            }
            .action-btn.delete:hover {
                color: var(--red);
            }

            .pagination {
                display: flex;
                justify-content: center;
                gap: 8px;
                margin-top: 25px;
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
            .empty-message {
                text-align: center;
                padding: 40px;
                color: var(--gray);
            }
        </style>
    </head>
    <body>

        <div class="page-container">
            <header class="page-header">
                <h1 class="page-title"><i class="fas fa-boxes-stacked"></i> Quản Lý Sản Phẩm</h1>
                <a href="dash-board" class="back-btn"><i class="fas fa-arrow-left"></i> Quay lại Dashboard</a>
            </header>

            <main class="content-box">
                <div class="filter-bar">
                    <input type="text" id="searchInput" placeholder="Tìm kiếm theo tên sản phẩm...">
                    <select id="categoryFilter">
                        <option value="all">Tất cả danh mục</option>
                        <c:forEach var="category" items="${categories}">
                            <option value="${category.categoryId}">${category.name}</option>
                        </c:forEach>
                    </select>
                    <select id="statusFilter">
                        <option value="all">Tất cả trạng thái</option>
                        <option value="active">Đang bán</option>
                        <option value="hidden">Bị ẩn</option>
                    </select>
                </div>

                <c:choose>
                    <c:when test="${not empty totalProducts}">
                        <table class="product-table">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th>Danh mục</th>
                                    <th class="sortable" data-sort-by="price">Giá <i class="fas fa-sort sort-icon"></i></th>
                                    <th class="sortable" data-sort-by="stock">Tồn kho <i class="fas fa-sort sort-icon"></i></th>
                                    <th>Trạng thái</th>
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody id="productTableBody">
                                <c:forEach var="product" items="${totalProducts}">
                                    <tr data-name="${product.name}" 
                                        data-category-id="${product.categoryId.categoryId}" 
                                        data-status="${product.status}"
                                        data-price="${product.basePrice}"
                                        data-stock="${product.stockQuantity}">
                                        <td>
                                            <div class="product-cell">
                                                <img src="${product.productImagesList.get(0).imageUrl}" alt="${product.name}">
                                                <span class="product-name">${product.name}</span>
                                            </div>
                                        </td>
                                        <td>${product.categoryId.name}</td>
                                        <td><fmt:formatNumber value="${product.basePrice}" type="currency" currencySymbol="₫" pattern="#,##0₫"/></td>
                                        <td>${product.stockQuantity}</td>
                                        <td>
                                            <span class="status-badge ${product.status == 'active' ? 'active' : 'hidden'}">
                                                ${product.status == 'active' ? 'Đang bán' : 'Bị ẩn'}
                                            </span>
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <button class="action-btn edit" onclick="window.location.href = 'manageProducts?action=edit&id=${product.productId}'" title="Sửa sản phẩm">
                                                    <i class="fas fa-edit"></i>
                                                </button>
                                                <button class="action-btn delete" onclick="confirmDelete(${product.productId})" title="Xóa sản phẩm">
                                                    <i class="fas fa-trash"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <div class="pagination" id="pagination"></div>
                    </c:when>
                    <c:otherwise>
                        <p class="empty-message">Bạn chưa có sản phẩm nào trong cửa hàng.</p>
                    </c:otherwise>
                </c:choose>
            </main>
        </div>

        <script>
            function confirmDelete(productId) {
                if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này không? Hành động này không thể hoàn tác.')) {
                    window.location.href = 'manageProducts?action=delete&id=' + productId;
                }
            }

            document.addEventListener('DOMContentLoaded', function () {
                const productManager = {
                    // DOM Elements
                    searchInput: document.getElementById('searchInput'),
                    categoryFilter: document.getElementById('categoryFilter'),
                    statusFilter: document.getElementById('statusFilter'),
                    tableBody: document.getElementById('productTableBody'),
                    paginationContainer: document.getElementById('pagination'),
                    sortableHeaders: document.querySelectorAll('.product-table th.sortable'),

                    // State
                    allRows: [],
                    filteredRows: [],
                    rowsPerPage: 8,
                    currentPage: 1,
                    sort: {
                        by: 'name',
                        direction: 'asc'
                    },

                    init() {
                        if (!this.tableBody)
                            return;
                        this.allRows = Array.from(this.tableBody.querySelectorAll("tr"));
                        this.addEventListeners();
                        this.applyFiltersAndSort();
                    },

                    addEventListeners() {
                        this.searchInput.addEventListener('keyup', () => this.applyFiltersAndSort());
                        this.categoryFilter.addEventListener('change', () => this.applyFiltersAndSort());
                        this.statusFilter.addEventListener('change', () => this.applyFiltersAndSort());

                        this.sortableHeaders.forEach(header => {
                            header.addEventListener('click', () => {
                                const sortBy = header.dataset.sortBy;
                                if (this.sort.by === sortBy) {
                                    this.sort.direction = this.sort.direction === 'asc' ? 'desc' : 'asc';
                                } else {
                                    this.sort.by = sortBy;
                                    this.sort.direction = 'desc';
                                }
                                this.applyFiltersAndSort();
                                this.updateSortIcons();
                            });
                        });
                    },

                    applyFiltersAndSort() {
                        const searchTerm = this.searchInput.value.toLowerCase();
                        const categoryId = this.categoryFilter.value;
                        const status = this.statusFilter.value;

                        // 1. Filtering
                        this.filteredRows = this.allRows.filter(row => {
                            const name = row.dataset.name.toLowerCase();
                            const rowCategoryId = row.dataset.categoryId;
                            const rowStatus = row.dataset.status;

                            const matchesSearch = name.includes(searchTerm);
                            const matchesCategory = (categoryId === 'all' || rowCategoryId === categoryId);
                            const matchesStatus = (status === 'all' || rowStatus === status);

                            return matchesSearch && matchesCategory && matchesStatus;
                        });

                        // 2. Sorting
                        this.filteredRows.sort((a, b) => {
                            const valA = a.dataset[this.sort.by];
                            const valB = b.dataset[this.sort.by];

                            const numA = parseFloat(valA);
                            const numB = parseFloat(valB);

                            let comparison = 0;
                            if (!isNaN(numA) && !isNaN(numB)) {
                                comparison = numA > numB ? 1 : -1;
                            } else {
                                comparison = valA.localeCompare(valB);
                            }

                            return this.sort.direction === 'asc' ? comparison : -comparison;
                        });

                        // 3. Pagination
                        this.displayPage(1);
                    },

                    displayPage(page) {
                        this.currentPage = page;
                        this.tableBody.innerHTML = ''; // Clear table body

                        const start = (page - 1) * this.rowsPerPage;
                        const end = start + this.rowsPerPage;

                        const pageRows = this.filteredRows.slice(start, end);
                        pageRows.forEach(row => this.tableBody.appendChild(row));

                        this.updatePaginationButtons();
                    },

                    updatePaginationButtons() {
                        const totalPages = Math.ceil(this.filteredRows.length / this.rowsPerPage);
                        this.paginationContainer.innerHTML = "";
                        if (totalPages <= 1)
                            return;

                        for (let i = 1; i <= totalPages; i++) {
                            const btn = document.createElement("button");
                            btn.textContent = i;
                            btn.classList.toggle("active", i === this.currentPage);
                            btn.addEventListener('click', () => this.displayPage(i));
                            this.paginationContainer.appendChild(btn);
                        }
                    },

                    updateSortIcons() {
                        this.sortableHeaders.forEach(th => {
                            const icon = th.querySelector('.sort-icon');
                            if (th.dataset.sortBy === this.sort.by) {
                                icon.className = this.sort.direction === 'asc' ? 'fas fa-sort-up sort-icon' : 'fas fa-sort-down sort-icon';
                            } else {
                                icon.className = 'fas fa-sort sort-icon';
                            }
                        });
                    }
                };

                productManager.init();
            });
        </script>

    </body>
</html>