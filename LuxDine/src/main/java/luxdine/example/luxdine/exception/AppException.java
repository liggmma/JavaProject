/**
 * @code AppException
 *
 * Ngoại lệ tùy chỉnh (custom exception) của ứng dụng LuxDine.
 *
 * <p>Được sử dụng để ném ra khi có lỗi nghiệp vụ hoặc logic
 * được định nghĩa trong {@link ErrorCode}.</p>
 *
 * <p>Khác với {@link GlobalExceptionHandler}, class này KHÔNG xử lý lỗi —
 * mà chỉ dùng để truyền thông tin lỗi (errorCode, message, cause)
 * lên tầng xử lý ngoại lệ toàn cục.</p>
 *
 * <p>Ví dụ sử dụng:</p>
 * <pre>
 * if (user == null) {
 *     throw new AppException(ErrorCode.USER_NOT_FOUND);
 * }
 * </pre>
 *
 * <p>Lợi ích:</p>
 * <ul>
 *   <li>Đảm bảo cấu trúc mã lỗi thống nhất toàn hệ thống.</li>
 *   <li>Giúp {@code GlobalExceptionHandler} dễ dàng nhận biết và format phản hồi.</li>
 * </ul>
 *
 * @see ErrorCode
 * @see GlobalExceptionHandler
 * Author: Kiên Lê Ngọc Minh
 */
package luxdine.example.luxdine.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppException extends RuntimeException {
    
    private ErrorCode errorCode;
    
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}