<!DOCTYPE html>
<html lang="vi">
    <head>
        <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ShopGo - Gia Dụng Bình An 369 //(Gia dinh binh an is shop name by database) </title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Arial', sans-serif;
                background-color: #f5f5f5;
            }

            /* Header */
            .header {
                background: linear-gradient(135deg, #ee4d2d, #ff6b35);
                color: white;
                padding: 8px 0;
                position: sticky;
                top: 0;
                z-index: 100;
            }

            .header-top {
                display: flex;
                justify-content: space-between;
                align-items: center;
                max-width: 1200px;
                margin: 0 auto;
                padding: 0 20px;
                font-size: 13px;
            }

            .header-left {
                display: flex;
                gap: 20px;
            }

            .header-right {
                display: flex;
                gap: 20px;
                align-items: center;
            }

            .header-main {
                max-width: 1200px;
                margin: 0 auto;
                padding: 15px 20px;
                display: flex;
                align-items: center;
                gap: 30px;
            }

            .logo {
                font-size: 32px;
                font-weight: bold;
                color: white;
                text-decoration: none;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .search-container {
                flex: 1;
                display: flex;
                background: white;
                border-radius: 2px;
                overflow: hidden;
            }

            .search-input {
                flex: 1;
                padding: 12px 16px;
                border: none;
                outline: none;
                font-size: 14px;
            }

            .search-btn {
                background: #ee4d2d;
                border: none;
                color: white;
                padding: 12px 24px;
                cursor: pointer;
                font-size: 16px;
            }

            .cart-icon {
                font-size: 24px;
                position: relative;
                cursor: pointer;
            }

            .cart-count {
                position: absolute;
                top: -8px;
                right: -8px;
                background: #ffbf00;
                color: #ee4d2d;
                border-radius: 50%;
                width: 20px;
                height: 20px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 12px;
                font-weight: bold;
            }

            /* Shop Info */
            .shop-info {
                background: white;
                margin: 20px auto;
                max-width: 1200px;
                border-radius: 8px;
                padding: 30px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }

            .shop-header {
                display: flex;
                gap: 30px;
                margin-bottom: 30px;
            }

            .shop-avatar {
                width: 120px;
                height: 120px;
                border-radius: 50%;
                background: linear-gradient(45deg, #6c5ce7, #a29bfe);
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 40px;
                color: white;
                font-weight: bold;
            }

            .shop-details h1 {
                font-size: 28px;
                margin-bottom: 8px;
                color: #333;
            }

            .shop-status {
                color: #26aa99;
                font-size: 14px;
                margin-bottom: 15px;
            }

            .shop-badge {
                background: #ee4d2d;
                color: white;
                padding: 4px 12px;
                border-radius: 12px;
                font-size: 12px;
                font-weight: bold;
            }

            .shop-actions {
                display: flex;
                gap: 15px;
                margin-top: 20px;
            }

            .btn {
                padding: 12px 24px;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 14px;
                font-weight: bold;
                transition: all 0.3s ease;
            }

            .btn-follow {
                background: #ee4d2d;
                color: white;
            }

            .btn-chat {
                background: white;
                color: #ee4d2d;
                border: 1px solid #ee4d2d;
            }

            .btn:hover {
                opacity: 0.9;
                transform: translateY(-1px);
            }

            .shop-stats {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 30px;
                margin-top: 30px;
            }

            .stat-item {
                text-align: center;
            }

            .stat-number {
                font-size: 20px;
                font-weight: bold;
                color: #ee4d2d;
            }

            .stat-label {
                font-size: 14px;
                color: #666;
                margin-top: 5px;
            }

            /* Navigation */
            .nav-tabs {
                display: flex;
                gap: 40px;
                margin-top: 30px;
                border-bottom: 2px solid #f0f0f0;
            }

            .nav-tab {
                padding: 15px 0;
                cursor: pointer;
                font-size: 16px;
                color: #666;
                border-bottom: 2px solid transparent;
                transition: all 0.3s ease;
            }

            .nav-tab.active {
                color: #ee4d2d;
                border-bottom-color: #ee4d2d;
            }

            /* Vouchers */
            .vouchers {
                background: white;
                margin: 20px auto;
                max-width: 1200px;
                border-radius: 8px;
                padding: 30px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }

            .voucher-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                gap: 20px;
            }

            .voucher-card {
                background: linear-gradient(135deg, #fff5f5, #fff0f0);
                border: 2px dashed #ee4d2d;
                border-radius: 8px;
                padding: 20px;
                position: relative;
                overflow: hidden;
            }

            .voucher-card::before {
                content: '';
                position: absolute;
                top: 0;
                right: 0;
                background: #ee4d2d;
                color: white;
                padding: 5px 15px;
                font-size: 12px;
                transform: rotate(45deg);
                transform-origin: center;
            }

            .voucher-discount {
                font-size: 24px;
                font-weight: bold;
                color: #ee4d2d;
                margin-bottom: 8px;
            }

            .voucher-condition {
                font-size: 14px;
                color: #666;
                margin-bottom: 15px;
            }

            .voucher-code {
                background: #fff;
                border: 1px solid #ee4d2d;
                padding: 8px 12px;
                border-radius: 4px;
                font-size: 12px;
                color: #ee4d2d;
                display: inline-block;
                margin-bottom: 15px;
            }

            .voucher-btn {
                background: #ee4d2d;
                color: white;
                border: none;
                padding: 10px 20px;
                border-radius: 4px;
                cursor: pointer;
                float: right;
                font-weight: bold;
            }

            .voucher-expiry {
                font-size: 12px;
                color: #999;
                margin-top: 10px;
            }

            /* Products */
            .products {
                background: white;
                margin: 20px auto;
                max-width: 1200px;
                border-radius: 8px;
                padding: 30px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }

            .products-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 30px;
            }

            .products-title {
                font-size: 24px;
                font-weight: bold;
                color: #333;
            }

            .view-all {
                color: #ee4d2d;
                text-decoration: none;
                font-size: 14px;
            }

            .product-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 20px;
            }

            .product-card {
                border: 1px solid #f0f0f0;
                border-radius: 8px;
                overflow: hidden;
                transition: all 0.3s ease;
                cursor: pointer;
            }

            .product-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 8px 20px rgba(0,0,0,0.1);
            }

            .product-image {
                width: 100%;
                height: 200px;
                background: linear-gradient(45deg, #f8f9fa, #e9ecef);
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 60px;
                color: #dee2e6;
            }

            .product-info {
                padding: 15px;
            }

            .product-title {
                font-size: 14px;
                color: #333;
                margin-bottom: 8px;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
            }

            .product-price {
                font-size: 18px;
                font-weight: bold;
                color: #ee4d2d;
                margin-bottom: 5px;
            }

            .product-sold {
                font-size: 12px;
                color: #666;
            }

            .product-rating {
                display: flex;
                align-items: center;
                gap: 5px;
                margin-top: 8px;
            }

            .stars {
                color: #ffc107;
            }

            .rating-text {
                font-size: 12px;
                color: #666;
            }

            /* Responsive */
            @media (max-width: 768px) {
                .header-main {
                    flex-direction: column;
                    gap: 15px;
                }

                .shop-header {
                    flex-direction: column;
                    text-align: center;
                }

                .shop-stats {
                    grid-template-columns: repeat(2, 1fr);
                }

                .nav-tabs {
                    overflow-x: auto;
                    white-space: nowrap;
                }

                .product-grid {
                    grid-template-columns: repeat(2, 1fr);
                }
            }
        </style>
    </head>
    <body>
        <!-- Header -->
        <header class="header">
            <div class="header-top">
                <div class="header-left">
                    <span>Kênh Người Bán</span>
                    <span>Tải ứng dụng</span>
                    <span>Kết nối</span>
                </div>
                <div class="header-right">
                    <span>Thông Báo</span>
                    <span>Hỗ Trợ</span>
                    <span>Tiếng Việt</span>
                    <span>nanakashi</span>
                </div>
            </div>
            <div class="header-main">
                <a href="#" class="logo">
                    🛒 ShopGo
                </a>
                <div class="search-container">
                    <input type="text" class="search-input" placeholder="Tìm trong Shop này">
                    <button class="search-btn">🔍</button>
                </div>
                <div class="cart-icon">
                    🛒
                    <span class="cart-count">53</span>
                </div>
            </div>
        </header>

        <!-- Shop Info -->
        <div class="shop-info">
            <div class="shop-header">
                <div class="shop-avatar">
                    GD
                </div>
                <div class="shop-details">
                    <h1>Gia Dụng Bình An 369</h1>
                    <div class="shop-status">Online 57 phút trước</div>
                    <div class="shop-badge">Yêu Thích</div>
                    <div class="shop-actions">
                        <button class="btn btn-follow">+ Theo Dõi</button>
                        <button class="btn btn-chat">💬 Chat</button>
                    </div>
                </div>
            </div>

            <div class="shop-stats">
                <div class="stat-item">
                    <div class="stat-number">263</div>
                    <div class="stat-label">Sản Phẩm</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">9.5k</div>
                    <div class="stat-label">Người Theo Dõi</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">3</div>
                    <div class="stat-label">Đang Theo</div>
                </div>
                <div class="stat-item">
                    <div class="stat-number">4.7</div>
                    <div class="stat-label">Đánh Giá</div>
                </div>
            </div>

            <div class="nav-tabs">
                <div class="nav-tab active">Dạo</div>
                <div class="nav-tab">Sản phẩm</div>
                <div class="nav-tab">Hàng mới về</div>
            </div>
        </div>

        <!-- Vouchers -->
        <div class="vouchers">
            <div class="voucher-grid">
                <div class="voucher-card">
                    <div class="voucher-discount">Giảm ₫1k</div>
                    <div class="voucher-condition">Đơn Tối Thiểu ₫99k</div>
                    <div class="voucher-code">Voucher khách hàng mới</div>
                    <button class="voucher-btn">Lưu</button>
                    <div class="voucher-expiry">HSD: 05.09.2025</div>
                </div>
                <div class="voucher-card">
                    <div class="voucher-discount">Giảm ₫2k</div>
                    <div class="voucher-condition">Đơn Tối Thiểu ₫199k</div>
                    <button class="voucher-btn">Lưu</button>
                    <div class="voucher-expiry">HSD: 05.09.2025</div>
                </div>
                <div class="voucher-card">
                    <div class="voucher-discount">Giảm ₫3k</div>
                    <div class="voucher-condition">Đơn Tối Thiểu ₫299k</div>
                    <button class="voucher-btn">Lưu</button>
                    <div class="voucher-expiry">HSD: 05.09.2025</div>
                </div>
            </div>
        </div>

        <!-- Products -->
        <div class="products">
            <div class="products-header">
                <h2 class="products-title">GỢI Ý CHO BẠN</h2>
                <a href="#" class="view-all">Xem Tất Cả ></a>
            </div>
            <div class="product-grid" id="productGrid">
                <!-- Products will be generated by JavaScript -->
            </div>
        </div>

        <script>
            // Sample product data
            const products = [
                {
                    title: "Combo 5 chai xịt khử mùi Geto",
                    price: "₫159.000",
                    originalPrice: "₫199.000",
                    sold: "Đã bán 1.2k",
                    rating: 4.8,
                    image: "🧴"
                },
                {
                    title: "Bình xịt khử mùi giày chuyên dụng",
                    price: "₫89.000",
                    originalPrice: "₫120.000",
                    sold: "Đã bán 856",
                    rating: 4.6,
                    image: "👟"
                },
                {
                    title: "Nước rửa tay khô kháng khuẩn",
                    price: "₫45.000",
                    originalPrice: "₫60.000",
                    sold: "Đã bán 2.1k",
                    rating: 4.9,
                    image: "🧼"
                },
                {
                    title: "Bộ dụng cụ vệ sinh nhà cửa",
                    price: "₫299.000",
                    originalPrice: "₫399.000",
                    sold: "Đã bán 678",
                    rating: 4.7,
                    image: "🧽"
                },
                {
                    title: "Túi hút chân không bảo quản thực phẩm",
                    price: "₫129.000",
                    originalPrice: "₫180.000",
                    sold: "Đã bán 945",
                    rating: 4.5,
                    image: "🛍️"
                },
                {
                    title: "Kệ để đồ đa năng 3 tầng",
                    price: "₫199.000",
                    originalPrice: "₫250.000",
                    sold: "Đã bán 432",
                    rating: 4.8,
                    image: "📚"
                },
                {
                    title: "Bình đựng nước thủy tinh cao cấp",
                    price: "₫79.000",
                    originalPrice: "₫100.000",
                    sold: "Đã bán 1.5k",
                    rating: 4.6,
                    image: "🍶"
                },
                {
                    title: "Bộ hộp đựng thực phẩm tiện lợi",
                    price: "₫159.000",
                    originalPrice: "₫200.000",
                    sold: "Đã bán 823",
                    rating: 4.9,
                    image: "🥡"
                }
            ];

            // Generate product cards
            function generateProducts() {
                const productGrid = document.getElementById('productGrid');
                productGrid.innerHTML = '';

                products.forEach(product => {
                    const productCard = document.createElement('div');
                    productCard.className = 'product-card';
                    productCard.innerHTML = `
                        <div class="product-image">
            ${product.image}
                        </div>
                        <div class="product-info">
                            <div class="product-title">${product.title}</div>
                            <div class="product-price">
            ${product.price}
                                <span style="text-decoration: line-through; color: #999; font-size: 14px; margin-left: 8px;">${product.originalPrice}</span>
                            </div>
                            <div class="product-rating">
                                <span class="stars">${'★'.repeat(Math.floor(product.rating))}${'☆'.repeat(5 - Math.floor(product.rating))}</span>
                                <span class="rating-text">(${product.rating})</span>
                            </div>
                            <div class="product-sold">${product.sold}</div>
                        </div>
                    `;

                    productCard.addEventListener('click', function () {
                        alert(`Bạn đã click vào sản phẩm: ${product.title}`);
                    });

                    productGrid.appendChild(productCard);
                });
            }

            // Tab switching functionality
            document.querySelectorAll('.nav-tab').forEach(tab => {
                tab.addEventListener('click', function () {
                    document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
                    this.classList.add('active');
                });
            });

            // Voucher save functionality
            document.querySelectorAll('.voucher-btn').forEach(btn => {
                btn.addEventListener('click', function () {
                    this.textContent = 'Đã lưu';
                    this.style.background = '#26aa99';
                    setTimeout(() => {
                        this.textContent = 'Lưu';
                        this.style.background = '#ee4d2d';
                    }, 2000);
                });
            });

            // Search functionality
            document.querySelector('.search-btn').addEventListener('click', function () {
                const searchTerm = document.querySelector('.search-input').value;
                if (searchTerm.trim()) {
                    alert(`Tìm kiếm: ${searchTerm}`);
                }
            });

            // Follow button functionality
            document.querySelector('.btn-follow').addEventListener('click', function () {
                if (this.textContent.includes('Theo Dõi')) {
                    this.textContent = '✓ Đã Theo Dõi';
                    this.style.background = '#26aa99';
                } else {
                    this.textContent = '+ Theo Dõi';
                    this.style.background = '#ee4d2d';
                }
            });

            // Chat button functionality
            document.querySelector('.btn-chat').addEventListener('click', function () {
                alert('Mở cửa sổ chat với shop...');
            });

            // Initialize products
            generateProducts();
        </script>
    </body>
</html>

//Backend nho thay doi thuoc tinh theo nhu cau cua project nhe 
// cai nay la timeplate cua shop. khi user dang nhap vao thi day se là màn hình quản lí shop của họ, hoặc guest vào shop này sẽ thấy những thông đó
