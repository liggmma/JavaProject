<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Quên mật khẩu</title>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap" rel="stylesheet">
        <style>
            :root {
                --main-pink: #ff6f91;
                --main-orange: #ff9671;
                --gray: #6b7280;
                --text-dark: #111827;
                --bg-light: #f3f4f6;
            }

            * {
                box-sizing: border-box;
            }

            body {
                font-family: 'Poppins', sans-serif;
                background-color: var(--bg-light);
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
            }

            .form-container {
                background: #fff;
                padding: 40px 30px;
                border-radius: 20px;
                box-shadow: 0 12px 30px rgba(0,0,0,0.08);
                width: 100%;
                max-width: 420px;
                animation: fadeIn 0.4s ease-in-out;
            }

            @keyframes fadeIn {
                from {
                    opacity: 0;
                    transform: translateY(20px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            h1 {
                text-align: center;
                margin-bottom: 28px;
                font-size: 24px;
                color: var(--text-dark);
            }

            .input-wrapper {
                position: relative;
                margin-bottom: 20px;
            }

            .input-wrapper input {
                width: 100%;
                padding: 12px 16px 12px 42px; /* icon space left */
                border: 1px solid #d1d5db;
                border-radius: 50px;
                background-color: #f9fafb;
                font-size: 14px;
                transition: border-color 0.2s;
                display: block;
            }

            .input-wrapper input:focus {
                border-color: var(--main-pink);
                background-color: #fff;
                outline: none;
            }

            .input-wrapper .input-icon {
                position: absolute;
                top: 50%;
                left: 14px;
                transform: translateY(-50%);
                font-size: 16px;
                color: #9ca3af;
                pointer-events: none;
            }

            .submit-btn {
                width: 100%;
                padding: 12px;
                border: none;
                border-radius: 30px;
                background: linear-gradient(135deg, var(--main-pink), var(--main-orange));
                color: white;
                font-weight: 600;
                font-size: 16px;
                cursor: pointer;
                transition: background 0.3s ease;
            }

            .submit-btn:hover {
                opacity: 0.95;
            }

            .message {
                text-align: center;
                font-size: 14px;
                color: green;
                margin-top: 15px;
            }

            .error {
                color: red;
                font-weight: 500;
                text-align: center;
                margin-top: 15px;
            }

            .back-link {
                text-align: center;
                margin-top: 20px;
                font-size: 14px;
            }

            .back-link a {
                color: var(--main-pink);
                text-decoration: none;
            }

            .back-link a:hover {
                text-decoration: underline;
            }
        </style>
    </head>
    <body>

        <div class="form-container">
            <h1>Quên mật khẩu?</h1>
            <form method="post" action="forgot-password">
                <div class="input-wrapper">
                    <i class="fas fa-envelope input-icon"></i>
                    <input type="email" name="email" id="email" placeholder="Nhập email của bạn" required />
                </div>

                <input type="submit" class="submit-btn" value="Gửi liên kết khôi phục" />

                <c:if test="${not empty success}">
                    <p class="message">${success}</p>
                </c:if>

                <c:if test="${not empty error}">
                    <p class="error">${error}</p>
                </c:if>

                <div class="back-link">
                    <a href="login">&larr; Quay lại đăng nhập</a>
                </div>
            </form>
        </div>

    </body>
</html>
