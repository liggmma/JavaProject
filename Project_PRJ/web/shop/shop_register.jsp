<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đăng ký mở shop</title>
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">


        <style>
            body {
                font-family: 'Poppins', sans-serif;
                background: #f3f4f6;
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                padding: 40px 0;
            }

            .form-container {
                background: #fff;
                border-radius: 16px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.08);
                padding: 40px;
                width: 100%;
                max-width: 600px;
                position: relative;
            }

            h1 {
                text-align: center;
                margin-bottom: 30px;
                font-size: 24px;
                color: #111827;
            }

            .form-group {
                margin-bottom: 18px;
                position: relative;
            }

            label {
                display: block;
                margin-bottom: 6px;
                color: #374151;
                font-weight: 500;
            }

            input[type="text"],
            input[type="email"],
            textarea,
            input[type="file"] {
                width: 100%;
                padding: 10px 14px;
                border: 1px solid #ccc;
                border-radius: 8px;
                background-color: #f9fafb;
                font-size: 14px;
                transition: border-color 0.2s;
            }

            input:focus, textarea:focus {
                border-color: #ff6f91;
                outline: none;
                background-color: #fff;
            }

            textarea {
                resize: vertical;
                min-height: 100px;
            }

            .image-preview {
                display: block;
                max-width: 100%;
                max-height: 200px;
                margin-top: 8px;
                border-radius: 8px;
                object-fit: contain;
                border: 1px dashed #ccc;
                padding: 4px;
                background-color: #f9fafb;
            }

            .submit-btn {
                display: block;
                width: 100%;
                padding: 12px;
                background: linear-gradient(135deg, #ff6f91, #ff9671);
                border: none;
                border-radius: 30px;
                color: white;
                font-weight: 600;
                font-size: 16px;
                cursor: pointer;
                transition: opacity 0.3s ease;
                margin-top: 10px;
            }

            .submit-btn:hover {
                opacity: 0.95;
            }

            .back-link {
                text-align: center;
                margin-top: 18px;
                font-size: 14px;
            }

            .back-link a {
                color: #ff6f91;
                text-decoration: none;
            }

            .back-link a:hover {
                text-decoration: underline;
            }

            /* --- Autocomplete gợi ý đẹp --- */
            .pac-container {
                z-index: 10000 !important;
                white-space: normal !important;
                max-width: 100% !important;
            }
            .pac-item {
                white-space: normal !important;
            }
        </style>
    </head>
    <body>

        <div class="form-container">
            <h1>Đăng ký mở Shop</h1>
            <form action="./register" method="post" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="shopName">Tên shop *</label>
                    <input type="text" name="shopName" id="shopName" required />
                </div>

                <div class="form-group">
                    <label for="contactPhone">Số điện thoại</label>
                    <input type="text" name="contactPhone" id="contactPhone" />
                </div>

                <div class="form-group">
                    <label for="contactEmail">Email liên hệ</label>
                    <input type="email" name="contactEmail" id="contactEmail" />
                </div>

                <div class="form-group">
                    <label for="shopAddress">Địa chỉ shop</label>
                    <input type="text" name="shopAddress" id="shopAddress" />
                </div>

                <div class="form-group">
                    <label for="shopDescription">Mô tả shop</label>
                    <textarea name="shopDescription" id="shopDescription"></textarea>
                </div>

                <div class="form-group">
                    <label for="shopLogoUpload">Logo Shop</label>
                    <input type="file" name="shopLogo" id="shopLogoUpload" accept="image/*" onchange="previewImage(event, 'logoPreview')" />
                    <img id="logoPreview" class="image-preview" src="#" alt="Xem trước logo" style="display: none;" />
                </div>

                <div class="form-group">
                    <label for="shopBannerUpload">Banner Shop</label>
                    <input type="file" name="shopBanner" id="shopBannerUpload" accept="image/*" onchange="previewImage(event, 'bannerPreview')" />
                    <img id="bannerPreview" class="image-preview" src="#" alt="Xem trước banner" style="display: none;" />
                </div>

                <button type="submit" class="submit-btn">Gửi yêu cầu mở Shop</button>
            </form>

            <div class="back-link">
                <a href="../profile">&larr; Quay lại trang cá nhân</a>
            </div>
        </div>


        <script>
            function previewImage(event, previewId) {
                const fileInput = event.target;
                const [file] = fileInput.files;
                const preview = document.getElementById(previewId);
                if (file) {
                    preview.src = URL.createObjectURL(file);
                    preview.style.display = "block";
                }
            }

        </script>

    </body>
</html>
