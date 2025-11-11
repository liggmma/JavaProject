<%@page import="model.Products"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Chỉnh sửa sản phẩm - ${product.name}</title>
        <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --bg-light: #f9fafb;
                --gray: #6b7280;
                --dark: #111827;
                --blue: #3b82f6;
                --red: #ef4444;
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
            }
            .page-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 25px;
            }
            .page-title {
                font-size: 24px;
                font-weight: 600;
            }
            .action-buttons {
                display: flex;
                gap: 15px;
            }
            .btn {
                padding: 10px 20px;
                border: none;
                border-radius: 8px;
                font-weight: 600;
                cursor: pointer;
                text-decoration: none;
                transition: all 0.3s;
            }
            .btn-primary {
                background: var(--main-pink);
                color: white;
            }
            .btn-primary:hover {
                opacity: 0.9;
            }
            .btn-secondary {
                background: #e5e7eb;
                color: var(--dark);
            }
            .btn-secondary:hover {
                background: #d1d5db;
            }

            .edit-form-layout {
                display: grid;
                grid-template-columns: 2fr 1fr;
                gap: 30px;
            }

            .form-section {
                background: #fff;
                padding: 25px;
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            }
            .form-section h3 {
                font-size: 18px;
                margin-top: 0;
                margin-bottom: 20px;
                padding-bottom: 15px;
                border-bottom: 1px solid #e5e7eb;
            }
            .form-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 20px;
            }
            .form-group {
                display: flex;
                flex-direction: column;
                gap: 8px;
            }
            .form-group.full-width {
                grid-column: 1 / -1;
            }
            .form-group label {
                font-weight: 500;
                font-size: 14px;
            }
            .form-group input, .form-group select, .form-group textarea {
                width: 100%;
                padding: 10px;
                border: 1px solid #d1d5db;
                border-radius: 6px;
                font-size: 14px;
            }
            .form-group textarea {
                min-height: 120px;
                resize: vertical;
            }

            /* Image Manager Styles */
            .image-manager .image-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
                gap: 15px;
            }
            .image-item {
                position: relative;
                aspect-ratio: 1/1;
                border-radius: 8px;
                overflow: hidden;
                cursor: grab;
            }
            .image-item img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
            .image-item .delete-btn {
                position: absolute;
                top: 5px;
                right: 5px;
                width: 24px;
                height: 24px;
                background: rgba(0,0,0,0.6);
                color: white;
                border: none;
                border-radius: 50%;
                cursor: pointer;
                display: flex;
                justify-content: center;
                align-items: center;
                font-size: 14px;
                opacity: 0;
                transition: opacity 0.2s;
            }
            .image-item:hover .delete-btn {
                opacity: 1;
            }
            .image-item .cover-badge {
                position: absolute;
                bottom: 5px;
                left: 5px;
                background: var(--main-pink);
                color: white;
                padding: 2px 8px;
                font-size: 10px;
                font-weight: 600;
                border-radius: 4px;
            }
            .image-item.dragging {
                opacity: 0.5;
                border: 2px dashed var(--main-pink);
            }

            .upload-area {
                margin-top: 20px;
                border: 2px dashed #d1d5db;
                border-radius: 8px;
                padding: 20px;
                text-align: center;
                cursor: pointer;
                color: var(--gray);
            }
            .upload-area:hover {
                border-color: var(--main-pink);
                color: var(--main-pink);
            }
        </style>
    </head>
    <body>

        <div class="page-container">
            <form action="${pageContext.request.contextPath}/shop/manageProducts" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="productId" value="${product.productId}">
                <input type="hidden" name="deletedImageIds" id="deletedImageIds">
                <input type="hidden" name="imageOrder" id="imageOrder">

                <header class="page-header">
                    <h1 class="page-title">Chỉnh sửa sản phẩm</h1>
                    <div class="action-buttons">
                        <a href="${pageContext.request.contextPath}/shop/manageProducts" class="btn btn-secondary">Hủy</a>
                        <button type="submit" class="btn btn-primary">Lưu thay đổi</button>
                    </div>
                </header>

                <main class="edit-form-layout">
                    <div class="left-column">
                        <section class="form-section">
                            <h3>Thông tin cơ bản</h3>
                            <div class="form-grid">
                                <div class="form-group full-width">
                                    <label for="name">Tên sản phẩm</label>
                                    <input type="text" id="name" name="name" value="${product.name}" required>
                                </div>
                                <div class="form-group">
                                    <label for="categoryId">Danh mục</label>
                                    <select id="categoryId" name="categoryId">
                                        <c:forEach var="cat" items="${categories}">
                                            <option value="${cat.categoryId}" ${product.categoryId.categoryId == cat.categoryId ? 'selected' : ''}>${cat.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label for="basePrice">Giá</label>
                                    <input type="number" id="basePrice" name="basePrice" value="${product.basePrice}" step="1000" required>
                                </div>
                                <div class="form-group">
                                    <label for="stockQuantity">Tồn kho</label>
                                    <input type="number" id="stockQuantity" name="stockQuantity" value="${product.stockQuantity}" required>
                                </div>
                                <div class="form-group">
                                    <label for="weight">Cân nặng (kg)</label>
                                    <input type="number" id="weight" name="weight" value="${product.weight}" step="0.01">
                                </div>
                                <div class="form-group">
                                    <label for="status">Trạng thái</label>
                                    <select id="status" name="status">
                                        <option value="active" ${product.status == 'active' ? 'selected' : ''}>Đang bán</option>
                                        <option value="hidden" ${product.status == 'hidden' ? 'selected' : ''}>Bị ẩn</option>
                                    </select>
                                </div>
                            </div>
                        </section>
                        <section class="form-section">
                            <h3>Mô tả sản phẩm</h3>
                            <div class="form-group full-width">
                                <textarea name="description" rows="5">${product.description}</textarea>
                            </div>
                        </section>
                    </div>

                    <div class="right-column">
                        <section class="form-section image-manager">
                            <h3>Quản lý hình ảnh</h3>
                            <div class="image-grid" id="imageGrid">
                                <c:forEach var="img" items="${imgList}" varStatus="loop">
                                    <div class="image-item" draggable="true" data-id="${img.imageId}">
                                        <img src="${img.imageUrl}" alt="Ảnh sản phẩm">
                                        <button type="button" class="delete-btn" onclick="deleteImage(this, ${img.imageId})">&times;</button>
                                        <c:if test="${loop.index == 0}"><div class="cover-badge">Ảnh bìa</div></c:if>
                                        </div>
                                </c:forEach>
                            </div>
                            <div class="upload-area" onclick="document.getElementById('newImages').click();">
                                <i class="fas fa-cloud-upload-alt"></i>
                                <p>Thêm ảnh mới</p>
                            </div>
                            <input type="file" id="newImages" name="newImages" multiple accept="image/*" style="display: none;" onchange="previewNewImages(event)">
                        </section>
                    </div>
                </main>
            </form>
        </div>

        <script>
            const imageGrid = document.getElementById('imageGrid');
            const deletedImagesInput = document.getElementById('deletedImageIds');
            const imageOrderInput = document.getElementById('imageOrder');
            let deletedImageIds = [];

            // --- Image Deletion ---
            function deleteImage(button, imageId) {
                if (confirm('Bạn có chắc muốn xóa ảnh này?')) {
                    const imageItem = button.closest('.image-item');
                    imageItem.remove();
                    deletedImageIds.push(imageId);
                    deletedImagesInput.value = deletedImageIds.join(',');
                    updateImageOrder();
                }
            }

            // --- New Image Preview ---
            function previewNewImages(event) {
                const files = event.target.files;
                for (const file of files) {
                    const reader = new FileReader();
                    reader.onload = function (e) {
                        const newImageItem = document.createElement('div');
                        newImageItem.className = 'image-item';
                        newImageItem.draggable = true;
                        // new images won't have a real ID yet, use a temporary one
                        newImageItem.dataset.id = `new_${file.name}`;
                        newImageItem.innerHTML = `
                            <img src="${e.target.result}" alt="Ảnh mới">
                            <button type="button" class="delete-btn" onclick="this.closest('.image-item').remove()">&times;</button>
                        `;
                        imageGrid.appendChild(newImageItem);
                    };
                    reader.readAsDataURL(file);
                }
                // Add drag events to new items after they are added
                setTimeout(addImageDragEvents, 100);
            }

            // --- Drag and Drop for Sorting ---
            let draggedItem = null;

            function addImageDragEvents() {
                const items = imageGrid.querySelectorAll('.image-item');
                items.forEach(item => {
                    item.addEventListener('dragstart', handleDragStart);
                    item.addEventListener('dragover', handleDragOver);
                    item.addEventListener('drop', handleDrop);
                    item.addEventListener('dragend', handleDragEnd);
                });
            }

            function handleDragStart(e) {
                draggedItem = this;
                setTimeout(() => this.classList.add('dragging'), 0);
            }

            function handleDragOver(e) {
                e.preventDefault();
            }

            function handleDrop(e) {
                e.preventDefault();
                if (this !== draggedItem) {
                    const currentPos = Array.from(imageGrid.children).indexOf(this);
                    const draggedPos = Array.from(imageGrid.children).indexOf(draggedItem);
                    if (currentPos < draggedPos) {
                        imageGrid.insertBefore(draggedItem, this);
                    } else {
                        imageGrid.insertBefore(draggedItem, this.nextSibling);
                    }
                }
            }

            function handleDragEnd() {
                this.classList.remove('dragging');
                draggedItem = null;
                updateImageOrder();
            }

            // --- Update hidden input with image order ---
            function updateImageOrder() {
                const items = imageGrid.querySelectorAll('.image-item');
                const orderIds = Array.from(items).map(item => item.dataset.id);
                imageOrderInput.value = orderIds.join(',');

                // Update cover badge
                items.forEach((item, index) => {
                    let badge = item.querySelector('.cover-badge');
                    if (badge)
                        badge.remove();
                    if (index === 0) {
                        const newBadge = document.createElement('div');
                        newBadge.className = 'cover-badge';
                        newBadge.textContent = 'Ảnh bìa';
                        item.appendChild(newBadge);
                    }
                });
            }

            // Initial setup
            document.addEventListener('DOMContentLoaded', () => {
                addImageDragEvents();
                updateImageOrder(); // Set initial order
            });

        </script>

    </body>
</html>