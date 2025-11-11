package luxdine.example.luxdine.service.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.user.dto.request.CreateEmployeeRequest;
import luxdine.example.luxdine.domain.user.dto.request.UpdateEmployeeRequest;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class AdminService {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    
    private static final int MAX_USERNAME_RETRY = 100;

    @Transactional(readOnly = true)
    public List<StaffSummary> getAllStaff() {
        List<User> staff = userRepository.findAllByRole(Role.STAFF);
        return staff.stream()
                .map(user -> StaffSummary.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole().name())
                        .isActive(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StaffSummary> searchStaffByName(String searchTerm) {
        List<User> staff;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            staff = userRepository.findAllByRole(Role.STAFF);
        } else {
            staff = userRepository.searchStaffByName(Role.STAFF, searchTerm.trim());
        }
        
        return staff.stream()
                .map(user -> StaffSummary.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole().name())
                        .isActive(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffSummary createEmployee(CreateEmployeeRequest request) {
        // Split full name into first and last name (trim whitespace)
        String trimmedFullName = request.getFullName().trim();
        String[] names = trimmedFullName.split("\\s+", 2);
        String firstName = names.length > 0 ? names[0] : "";
        String lastName = names.length > 1 ? names[1] : "";
        
        // Generate username from email or name
        String baseUsername = request.getEmail().split("@")[0];
        
        // Validate baseUsername is not empty (edge case: email like "@example.com")
        if (baseUsername == null || baseUsername.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Invalid email format: cannot generate username from email: " + request.getEmail());
        }
        
        String username = baseUsername;
        
        // Check if username exists, if so, add a number (with retry limit)
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            if (suffix > MAX_USERNAME_RETRY) {
                throw new IllegalStateException(
                    String.format("Failed to generate unique username after %d attempts for email: %s. " +
                                 "Please contact system administrator.", 
                                 MAX_USERNAME_RETRY, request.getEmail()));
            }
            username = baseUsername + suffix;
            suffix++;
        }
        
        User employee = User.builder()
                .username(username)
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .isActive(true)  // Mặc định là true khi tạo mới
                .build();
        
        try {
            User saved = userRepository.save(employee);
            return StaffSummary.builder()
                    .id(saved.getId())
                    .username(saved.getUsername())
                    .email(saved.getEmail())
                    .firstName(saved.getFirstName())
                    .lastName(saved.getLastName())
                    .phoneNumber(saved.getPhoneNumber())
                    .role(saved.getRole().name())
                    .isActive(saved.isActive())
                    .createdAt(saved.getCreatedAt())
                    .build();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Email or username already exists");
        }
    }
    
    @Transactional
    public StaffSummary updateEmployee(Long id, UpdateEmployeeRequest request) {
        User employee = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Update full name if provided (trim whitespace properly)
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            String trimmedName = request.getFullName().trim();
            String[] names = trimmedName.split("\\s+", 2); // Split by any whitespace
            employee.setFirstName(names[0]);
            employee.setLastName(names.length > 1 ? names[1] : "");
        }
        
        // Update email
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            // Check if email is already taken by another user
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new RuntimeException("Email already exists");
                        }
                    });
            employee.setEmail(request.getEmail());
        }
        
        // Update phone number
        if (request.getPhoneNumber() != null) {
            employee.setPhoneNumber(request.getPhoneNumber());
        }
        
        // Update role (with security validation)
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            Role newRole = Role.valueOf(request.getRole());
            Role currentRole = employee.getRole();
            
            // Validate role change for security
            if (currentRole != newRole) {
                // Prevent promoting to ADMIN role (requires higher privilege)
                if (newRole == Role.ADMIN && currentRole != Role.ADMIN) {
                    throw new IllegalArgumentException(
                        "Không thể thay đổi role thành ADMIN. Vui lòng liên hệ quản trị hệ thống.");
                }
                
                // Audit log for security
                log.warn("SECURITY: Role changed for employee {}: {} -> {} by admin user", 
                         id, currentRole, newRole);
            }
            
            employee.setRole(newRole);
        }
        
        // Update password
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // Update active status
        if (request.getIsActive() != null) {
            employee.setActive(request.getIsActive());
        }
        
        User updated = userRepository.save(employee);
        
        return StaffSummary.builder()
                .id(updated.getId())
                .username(updated.getUsername())
                .email(updated.getEmail())
                .firstName(updated.getFirstName())
                .lastName(updated.getLastName())
                .phoneNumber(updated.getPhoneNumber())
                .role(updated.getRole().name())
                .isActive(updated.isActive())
                .createdAt(updated.getCreatedAt())
                .build();
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        User employee = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Inactive employee instead of deleting
        employee.setActive(false);
        userRepository.save(employee);
    }
    
    @Transactional(readOnly = true)
    public StaffSummary getEmployeeById(Long id) {
        User employee = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        return StaffSummary.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .email(employee.getEmail())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .phoneNumber(employee.getPhoneNumber())
                .role(employee.getRole().name())
                .isActive(employee.isActive())
                .createdAt(employee.getCreatedAt())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StaffSummary {
        Long id;
        String username;
        String email;
        String firstName;
        String lastName;
        String phoneNumber;
        String role;
        boolean isActive;
        java.time.Instant createdAt;
    }
}

