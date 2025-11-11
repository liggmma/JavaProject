<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Thêm sản phẩm mới</title>
        <link rel="stylesheet" href="<c:url value='/assets/css/style.css'/>">
    </head>
    <body>
        <h1>Thêm sản phẩm mới</h1>

        <form action="manageProducts" method="post">
            <input type="hidden" name="action" value="addSubmit" />

            <label>SKU:</label>
            <input type="text" name="sku" required /><br/>

            <label>Tên sản phẩm:</label>
            <input type="text" name="name" required /><br/>

            <label>Mô tả:</label>
            <textarea name="description"></textarea><br/>

            <label>Giá cơ bản:</label>
            <input type="number" step="0.01" name="basePrice" required /><br/>

            <label>Trọng lượng (kg):</label>
            <input type="number" step="0.01" name="weight" /><br/>

            <label>Số lượng trong kho:</label>
            <input type="number" name="stockQuantity" required /><br/>

            <label>Trạng thái:</label>
            <select name="status">
                <option value="active">Đang hoạt động</option>
                <option value="hidden">Không hoạt động</option>
            </select><br/>

            <label>Danh mục:</label>
            <select name="categoryId">
                <c:forEach var="category" items="${categoryList}">
                    <option value="${category.categoryId}">${category.name}</option>
                </c:forEach>
            </select><br/>

            <button type="submit">Thêm sản phẩm</button>
        </form>

        <br/>
        <a href="manageProducts?action=view">Quay lại danh sách sản phẩm</a>
    </body>
</html>
