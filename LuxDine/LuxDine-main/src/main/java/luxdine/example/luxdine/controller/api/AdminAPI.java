package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.user.dto.request.CreateEmployeeRequest;
import luxdine.example.luxdine.domain.user.dto.request.UpdateEmployeeRequest;
import luxdine.example.luxdine.service.admin.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/api/admin")
public class AdminAPI {
    
    final AdminService adminService;
    
    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllStaff() {
        try {
            var staff = adminService.getAllStaff();
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            log.error("Error getting all staff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStaffById(@PathVariable Long id) {
        try {
            var staff = adminService.getEmployeeById(id);
            return ResponseEntity.ok(staff);
        } catch (RuntimeException e) {
            log.error("Error getting staff by id", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting staff by id", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createStaff(@Valid @RequestBody CreateEmployeeRequest request) {
        try {
            var staff = adminService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(staff);
        } catch (RuntimeException e) {
            log.error("Error creating staff", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating staff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, 
                                        @Valid @RequestBody UpdateEmployeeRequest request) {
        try {
            var staff = adminService.updateEmployee(id, request);
            return ResponseEntity.ok(staff);
        } catch (RuntimeException e) {
            log.error("Error updating staff", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating staff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/staff/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        try {
            adminService.deleteEmployee(id);
            return ResponseEntity.ok(Map.of("message", "Employee deactivated successfully"));
        } catch (RuntimeException e) {
            log.error("Error deactivating staff", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deactivating staff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

