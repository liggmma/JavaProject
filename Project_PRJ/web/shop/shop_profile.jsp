<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Shop Profile</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --gray: #6b7280;
                --dark: #111827;
                --green: #10b981;
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
                border-bottom: 1px solid #e5e7eb;
                padding-bottom: 20px;
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

            /* --- Alert Box Style --- */
            .alert-success {
                display: flex;
                align-items: center;
                gap: 12px;
                background-color: #d1fae5;
                color: #065f46;
                padding: 15px 20px;
                border-left: 5px solid var(--green);
                border-radius: 8px;
                margin-bottom: 20px;
                font-weight: 500;
                animation: fadeInDown 0.5s ease-in-out;
                transition: opacity 0.5s ease, transform 0.5s ease;
            }

            @keyframes fadeInDown {
                from {
                    opacity: 0;
                    transform: translateY(-20px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .banner-wrapper {
                position: relative;
                margin-bottom: 20px;
            }

            .banner-preview {
                width: 100%;
                height: 200px;
                object-fit: cover;
                border-radius: 12px;
                border: 2px dashed #d1d5db;
                cursor: pointer;
            }

            .banner-wrapper label {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: rgba(0,0,0,0.5);
                color: white;
                padding: 8px 16px;
                border-radius: 20px;
                cursor: pointer;
                display: none; /* Initially hidden */
                font-size: 14px;
            }

            .shop-header {
                display: flex;
                align-items: center;
                gap: 20px;
                margin-bottom: 30px;
            }

            .avatar-wrapper {
                position: relative;
                width: 90px;
                height: 90px;
                flex-shrink: 0;
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
                display: none; /* Initially hidden */
                white-space: nowrap;
            }

            .shop-name-wrapper {
                display: flex;
                align-items: center;
                gap: 10px;
            }

            /* Style for both h2 and input to be consistent */
            .shop-name-wrapper h2, #shopNameInput {
                margin: 0;
                font-family: 'Poppins', sans-serif;
                font-size: 28px;
                font-weight: 600;
                color: var(--dark);
            }

            #shopNameInput {
                border: 1px solid #d1d5db;
                border-radius: 6px;
                padding: 8px 12px;
                display: none; /* Hidden by default */
            }

            .verified-tick {
                font-size: 24px;
                color: var(--green);
            }

            #avatarUpload, #bannerUpload {
                display: none;
            }

            .shop-info {
                display: grid;
                grid-template-columns: 150px 1fr;
                row-gap: 15px;
                column-gap: 20px;
            }

            .shop-info label {
                font-weight: 500;
                color: var(--gray);
                align-self: center;
            }

            .shop-info input, .shop-info textarea {
                padding: 10px;
                border: 1px solid #d1d5db;
                border-radius: 6px;
                font-size: 14px;
                font-family: 'Poppins', sans-serif;
                background: #f9fafb;
                width: 100%;
                box-sizing: border-box;
            }

            .shop-info textarea {
                height: 80px;
                resize: vertical;
            }

            .shop-info input:read-only, .shop-info textarea:read-only {
                background: #f3f4f6;
                color: #4b5563;
                border-color: #e5e7eb;
                cursor: default;
            }

            .btn-group {
                margin-top: 30px;
                display: flex;
                gap: 15px;
            }

            .edit-btn, .save-btn {
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                border: none;
                padding: 10px 25px;
                font-weight: 600;
                font-size: 15px;
                color: white;
                border-radius: 30px;
                cursor: pointer;
                transition: 0.3s;
            }

            .edit-btn:hover, .save-btn:hover {
                opacity: 0.9;
                transform: translateY(-2px);
                box-shadow: 0 4px 15px rgba(255, 111, 145, 0.4);
            }
        </style>
    </head>
    <body>

        <div class="page-container">
            <div class="page-header">
                <div class="page-title"><i class="fas fa-store"></i> Shop Profile</div>
                <a href="dash-board" class="back-btn"><i class="fas fa-arrow-left"></i> Quay lại Dashboard</a>
            </div>

            <c:if test="${not empty sessionScope.successMessage}">
                <div class="alert-success">
                    <i class="fas fa-check-circle"></i>
                    <span>${sessionScope.successMessage}</span>
                </div>
                <c:remove var="successMessage" scope="session"/>
            </c:if>

            <form method="post" action="profile" enctype="multipart/form-data">
                <c:set var="shop" value="${requestScope.shop}" />

                <div class="banner-wrapper">
                    <label for="bannerUpload" id="changeBannerBtn">Change Banner</label>
                    <img src="<c:choose><c:when test='${not empty shop.shopBannerUrl}'>${shop.shopBannerUrl}</c:when><c:otherwise>../images/banner_default.jpeg</c:otherwise></c:choose>" 
                         alt="Shop Banner" id="bannerPreview" class="banner-preview" onclick="if (!document.getElementById('bannerUpload').disabled)
                                     document.getElementById('bannerUpload').click();">
                            <input type="file" name="banner" id="bannerUpload" accept="image/*" onchange="previewBanner(event)" disabled>
                        </div>

                        <div class="shop-header">
                            <div class="avatar-wrapper">
                                    <img src="<c:choose><c:when test='${not empty shop.shopLogoUrl}'>${shop.shopLogoUrl}</c:when><c:otherwise>../images/shop_default.png</c:otherwise></c:choose>" alt="Shop Logo" id="avatarPreview">
                                <label for="avatarUpload" class="change-avatar-btn" id="changeAvatarBtn">Change</label>
                                <input type="file" name="logo" id="avatarUpload" accept="image/*" onchange="previewAvatar(event)" disabled>
                            </div>
                            <div class="shop-name-wrapper">
                                    <h2 id="shopNameDisplay">${shop.shopName}</h2>
                        <input type="text" name="shopName" id="shopNameInput" value="${shop.shopName}">
                        <c:if test="${shop.shopType == 'verified'}">
                            <i class="fas fa-check-circle verified-tick" title="Verified Shop"></i>
                        </c:if>
                    </div>
                </div>

                <div class="shop-info">
                    <label for="phone">Contact Phone:</label>
                    <input type="text" name="contactPhone" id="phone" value="${shop.contactPhone}" readonly>

                    <label for="email">Contact Email:</label>
                    <input type="text" name="contactEmail" id="email" value="${shop.contactEmail}" readonly>

                    <label for="address">Shop Address:</label>
                    <input type="text" name="shopAddress" id="address" value="${shop.shopAddress}" readonly>

                    <label for="desc">Description:</label>
                    <textarea name="shopDescription" id="desc" readonly>${shop.shopDescription}</textarea>

                    <label>Created At:</label>
                    <input type="text" value="${shop.createdAt}" readonly style="background: #e5e7eb; color: #6b7280; cursor: not-allowed;">
                </div>

                <div class="btn-group">
                    <button type="button" class="edit-btn" id="editBtn" onclick="enableEdit()">Edit Profile</button>
                    <button type="submit" class="save-btn" id="saveBtn" style="display: none;">Save Changes</button>
                </div>
            </form>
        </div>

        <script>
            function enableEdit() {
                // Hiển thị input, ẩn h2 cho tên shop
                document.getElementById('shopNameDisplay').style.display = 'none';
                document.getElementById('shopNameInput').style.display = 'block';

                // Enable các trường input khác
                ['email', 'phone', 'address', 'desc'].forEach(id => {
                    document.getElementById(id).removeAttribute('readonly');
                });

                // Enable file inputs
                document.getElementById('avatarUpload').disabled = false;
                document.getElementById('bannerUpload').disabled = false;

                // Show "Change" buttons
                document.getElementById('changeAvatarBtn').style.display = 'block';
                document.getElementById('changeBannerBtn').style.display = 'block';

                // Swap main action buttons
                document.getElementById('editBtn').style.display = 'none';
                document.getElementById('saveBtn').style.display = 'inline-block';
            }

            function previewAvatar(event) {
                const [file] = event.target.files;
                if (file) {
                    document.getElementById('avatarPreview').src = URL.createObjectURL(file);
                }
            }

            function previewBanner(event) {
                const [file] = event.target.files;
                if (file) {
                    document.getElementById('bannerPreview').src = URL.createObjectURL(file);
                }
            }

            // Tự động ẩn thông báo thành công
            document.addEventListener("DOMContentLoaded", () => {
                const alertBox = document.querySelector(".alert-success");
                if (alertBox) {
                    setTimeout(() => {
                        alertBox.style.opacity = '0';
                        alertBox.style.transform = 'translateY(-20px)';
                        // Đợi hiệu ứng transition hoàn tất rồi mới xóa
                        setTimeout(() => alertBox.remove(), 500);
                    }, 4000); // Biến mất sau 4 giây
                }
            });
        </script>
    </body>
</html>