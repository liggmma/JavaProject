/**
 * {@code ErrorCode}
 *
 * Enum liệt kê toàn bộ mã lỗi (error codes) trong hệ thống LuxDine.
 *
 * <p>Mỗi mã lỗi bao gồm:</p>
 * <ul>
 *   <li><b>code</b>: mã số duy nhất định danh lỗi (ví dụ: 3001)</li>
 *   <li><b>message</b>: thông điệp lỗi hiển thị</li>
 *   <li><b>statusCode</b>: mã HTTP tương ứng (BAD_REQUEST, NOT_FOUND, v.v.)</li>
 * </ul>
 *
 * <p>Được sử dụng trong {@link AppException} để ném lỗi có ngữ nghĩa rõ ràng.</p>
 *
 * <p>Khác với {@link GlobalExceptionHandler}, class này chỉ định nghĩa dữ liệu,
 * không xử lý logic lỗi.</p>
 *
 * <p>Ví dụ:</p>
 * <pre>
 * throw new AppException(ErrorCode.ITEM_NOT_FOUND);
 * </pre>
 *
 * Author: Kiên Lê Ngọc Minh
 */
package luxdine.example.luxdine.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ==================== GENERAL ERRORS ====================
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1003, "You do not have permission", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    
    // ==================== USER ERRORS ====================
    USER_EXISTED(2001, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_FOUND(2002, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(2003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(2004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(2005, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME_OR_PASSWORD(2006, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    
    // ==================== MENU/ITEM ERRORS ====================
    ITEM_NOT_FOUND(3001, "Menu item not found", HttpStatus.NOT_FOUND),
    ITEM_ALREADY_EXISTS(3002, "Menu item with this name already exists", HttpStatus.CONFLICT),
    ITEM_NOT_AVAILABLE(3003, "Menu item is not available", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(3004, "Price must be greater than 0", HttpStatus.BAD_REQUEST),

    
    // ==================== CATEGORY ERRORS ====================
    CATEGORY_NOT_FOUND(4001, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(4002, "Category with this name already exists", HttpStatus.CONFLICT),
    
    // ==================== BUNDLE ERRORS ====================
    BUNDLE_NOT_FOUND(5001, "Bundle not found", HttpStatus.NOT_FOUND),
    BUNDLE_ALREADY_EXISTS(5002, "Bundle with this name already exists", HttpStatus.CONFLICT),
    BUNDLE_NOT_ACTIVE(5003, "Bundle is not active", HttpStatus.BAD_REQUEST),
    
    // ==================== TABLE ERRORS ====================
    TABLE_NOT_FOUND(6001, "Table not found", HttpStatus.NOT_FOUND),
    TABLE_NOT_AVAILABLE(6002, "Table is not available", HttpStatus.BAD_REQUEST),
    TABLE_ALREADY_RESERVED(6003, "Table is already reserved for this time", HttpStatus.CONFLICT),
    
    // ==================== RESERVATION ERRORS ====================
    RESERVATION_NOT_FOUND(7001, "Reservation not found", HttpStatus.NOT_FOUND),
    RESERVATION_CONFLICT(7002, "Reservation time conflict", HttpStatus.CONFLICT),
    RESERVATION_ALREADY_CANCELLED(7003, "Reservation is already cancelled", HttpStatus.BAD_REQUEST),
    RESERVATION_CANNOT_CANCEL(7004, "Cannot cancel reservation less than 2 hours before", HttpStatus.BAD_REQUEST),
    INVALID_RESERVATION_DATE(7005, "Reservation date must be in the future", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER_OF_GUESTS(7006, "Number of guests exceeds table capacity", HttpStatus.BAD_REQUEST),
    
    // ==================== ORDER ERRORS ====================
    ORDER_NOT_FOUND(8001, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_COMPLETED(8002, "Order is already completed", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CANCELLED(8003, "Order is already cancelled", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_FOUND(8004, "Order item not found", HttpStatus.NOT_FOUND),
    INVALID_ORDER_STATUS(8005, "Invalid order status transition", HttpStatus.BAD_REQUEST),
    
    // ==================== PAYMENT ERRORS ====================
    PAYMENT_NOT_FOUND(9001, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_COMPLETED(9002, "Payment is already completed", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(9003, "Payment processing failed", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_AMOUNT(9004, "Insufficient payment amount", HttpStatus.BAD_REQUEST),
    
    // ==================== AREA ERRORS ====================
    AREA_NOT_FOUND(10001, "Area not found", HttpStatus.NOT_FOUND),
    
    // ==================== FILE/UPLOAD ERRORS ====================
    FILE_UPLOAD_FAILED(11001, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_TYPE(11002, "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(11003, "File size exceeds maximum limit", HttpStatus.BAD_REQUEST),
    IMAGE_FILE_NOT_PROVIDED(11004, "Image file not provided", HttpStatus.BAD_REQUEST),
    
    // ==================== VALIDATION ERRORS ====================
    INVALID_INPUT(12001, "Invalid input data", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD(12002, "Missing required field", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(12003, "Invalid date range", HttpStatus.BAD_REQUEST),
    INVALID_TIME_FORMAT(12004, "Invalid time format", HttpStatus.BAD_REQUEST),
    
    // ==================== BUSINESS LOGIC ERRORS ====================
    CANNOT_DELETE_ACTIVE_ITEM(13001, "Cannot delete item that is in active orders", HttpStatus.BAD_REQUEST),
    CANNOT_MODIFY_COMPLETED_ORDER(13002, "Cannot modify completed order", HttpStatus.BAD_REQUEST),
    SCHEDULE_CONFLICT(13003, "Schedule conflict detected", HttpStatus.CONFLICT),
    
    ;
    
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}