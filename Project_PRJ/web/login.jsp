<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <title>Đăng nhập</title>
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

            body {
                font-family: 'Poppins', sans-serif;
                display: flex;
                justify-content: center;
                align-items: center;
                height: 100vh;
                margin: 0;
            }
            #bgVideo {
                position: fixed;
                top: 0;
                left: 0;
                width: 100vw;
                height: 100vh;
                object-fit: cover;
                z-index: -1;
            }

            .login-container {
                .login-container {
                    background: rgba(255, 255, 255, 0.2);
                    backdrop-filter: blur(10px);
                    -webkit-backdrop-filter: blur(10px);
                    border: 1px solid rgba(255, 255, 255, 0.3);
                    border-radius: 16px;
                    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
                    padding: 40px 30px;
                    width: 100%;
                    max-width: 420px;
                    animation: fadeIn 0.4s ease-in;
                }

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
                margin-bottom: 22px;
            }

            .input-wrapper label {
                display: block;
                margin-bottom: 6px;
                font-weight: 500;
                color: var(--gray);
            }

            .input-wrapper input {
                width: 100%;
                padding: 12px 44px;
                border: 1px solid #d1d5db;
                border-radius: 50px;
                background-color: #f9fafb;
                font-size: 14px;
                transition: border-color 0.2s ease, background-color 0.2s ease;
                box-sizing: border-box;
            }

            .input-wrapper input:focus {
                border-color: var(--main-pink);
                background-color: #fff;
                outline: none;
            }

            .input-icon {
                position: absolute;
                top: 70%;
                transform: translateY(-50%);
                left: 16px;
                font-size: 16px;
                color: #9ca3af;
                line-height: 1; /* fix lỗi lệch vertical alignment */
                height: 16px;
            }

            .toggle-password {
                position: absolute;
                top: 70%;
                transform: translateY(-50%);
                right: 16px;
                font-size: 16px;
                color: #9ca3af;
                background: none;
                border: none;
                cursor: pointer;
                line-height: 1;
            }

            .remember-me {
                display: flex;
                align-items: center;
                margin-bottom: 18px;
                font-size: 14px;
                color: var(--gray);
            }

            .remember-me input {
                margin-right: 8px;
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

            .error {
                text-align: center;
                color: red;
                margin-top: 10px;
                font-weight: 500;
            }

            .actions {
                margin-top: 18px;
                text-align: center;
                font-size: 14px;
            }

            .actions a {
                color: var(--main-pink);
                text-decoration: none;
                margin: 0 6px;
                font-weight: 500;
            }

            .actions a:hover {
                text-decoration: underline;
            }

            /* Ẩn biểu tượng con mắt mặc định của trình duyệt */
            input[type="password"]::-ms-reveal,
            input[type="password"]::-ms-clear {
                display: none;
            }

            input[type="password"]::-webkit-credentials-auto-fill-button,
            input[type="password"]::-webkit-clear-button,
            input[type="password"]::-webkit-inner-spin-button,
            input[type="password"]::-webkit-calendar-picker-indicator {
                display: none !important;
            }
        </style>
    </head>
    <body>
        <video autoplay muted loop id="bgVideo">
            <source src="videos/loginBG.mp4" type="video/mp4">
            Trình duyệt của bạn không hỗ trợ video nền.
        </video>

        <div class="login-container">
            <h1>Đăng nhập</h1>
            <form method="post" action="login">

                <!-- Username -->
                <div class="input-wrapper">
                    <label for="username " style="color: darkblue;">Tên đăng nhập</label>
                    <i class="fas fa-user input-icon"></i>
                    <input type="text" name="username" id="username" value="${cookie.username.value}" required />
                </div>

                <!-- Password -->
                <div class="input-wrapper">
                    <label for="password" style="color: darkblue;">Mật khẩu</label>
                    <i class="fas fa-lock input-icon"></i>
                    <input type="password" name="password" id="password" value="${cookie.password.value}" required />
                    <button type="button" class="toggle-password" onclick="togglePassword(this)">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>

                <!-- Remember Me -->
                <div class="remember-me">
                    <input type="checkbox" id="rememberMe" name="rememberMe" value="true"
                           ${cookie.rememberMe.value == 'true' ? 'checked' : ''} />
                    <label for="rememberMe" style="color: black;">Ghi nhớ tôi</label>
                </div>

                <!-- Submit -->
                <input type="submit" value="Đăng nhập" class="submit-btn" />

                <!-- Error message -->
                <c:if test="${not empty error}">
                    <p class="error">${error}</p>
                </c:if>

                <!-- Links -->
                <div class="actions">
                    <a href="forgot-password.jsp">Quên mật khẩu?</a> |
                    <a href="register">Đăng ký</a>
                </div>

            </form>
        </div>

        <script>
            function togglePassword(btn) {
                const input = document.getElementById('password');
                const icon = btn.querySelector('i');

                if (input.type === 'password') {
                    input.type = 'text';
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                } else {
                    input.type = 'password';
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                }
            }
        </script>

    </body>
</html>
