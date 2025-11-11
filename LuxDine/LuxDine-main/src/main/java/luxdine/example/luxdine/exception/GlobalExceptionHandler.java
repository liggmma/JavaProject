package luxdine.example.luxdine.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {
        log.error("Validation failed: ", exception);
        
        String errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest request) {
        log.error("IllegalArgumentException: ", exception);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeNotFoundException(EmployeeNotFoundException exception, WebRequest request) {
        log.error("EmployeeNotFoundException: ", exception);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Employee Not Found");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {DuplicateEmailException.class, DuplicateUsernameException.class})
    public ResponseEntity<Map<String, Object>> handleDuplicateException(RuntimeException exception, WebRequest request) {
        log.error("DuplicateException: ", exception);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Duplicate Resource");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = InvalidRoleException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRoleException(InvalidRoleException exception, WebRequest request) {
        log.error("InvalidRoleException: ", exception);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Invalid Role");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = EmployeeHasDependenciesException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeHasDependenciesException(EmployeeHasDependenciesException exception, WebRequest request) {
        log.error("EmployeeHasDependenciesException: ", exception);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "Employee Has Dependencies");
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException exception, WebRequest request) {
        log.error("RuntimeException: ", exception);
        
        // Check if this is a reservation-related error
        String path = request.getDescription(false).replace("uri=", "");
        boolean isReservationError = path.contains("/reservations") || 
                                   exception.getMessage().contains("Cannot book") ||
                                   exception.getMessage().contains("outside operating hours") ||
                                   exception.getMessage().contains("exceeds table capacity") ||
                                   exception.getMessage().contains("not available") ||
                                   exception.getMessage().contains("already reserved");
        
        HttpStatus status = isReservationError ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        String errorType = isReservationError ? "Bad Request" : "Internal Server Error";
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", errorType);
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("path", path);
        
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException exception, Model model) {
        log.error("AccessDeniedException: ", exception);

        model.addAttribute("errorTitle", "Access Denied");
        model.addAttribute("errorMessage", "Bạn không có quyền truy cập tài nguyên này.");
        model.addAttribute("errorCode", "403");

        return "error/error";
    }

    @ExceptionHandler(value = Exception.class)
    public String handleException(Exception exception, Model model) {
        log.error("Exception: ", exception);

        model.addAttribute("errorTitle", "System Error");
        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("errorCode", "999");

        return "error/error";
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public String handle404Exception(NoResourceFoundException exception, Model model) {
        log.error("Exception: ", exception);

        model.addAttribute("errorTitle", "404 Error");
        model.addAttribute("errorMessage", "Resource not found");
        model.addAttribute("errorCode", "404");

        return "error/error";
    }
}

