package luxdine.example.luxdine.config;

import lombok.RequiredArgsConstructor;
import luxdine.example.luxdine.domain.user.dto.request.UserCreationRequest;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.service.auth.AuthenticationService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String ROLE_STAFF = "ROLE_STAFF";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String STAFF_RESERVATIONS_URL = "/staff/reservations";
    private static final String ADMIN_HOME_URL = "/admin/staff";
    private static final String CUSTOMER_HOME_URL = "/home";

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // Nếu là đăng nhập Google, chuyển sang UserDetails nội bộ
        if (authentication instanceof OAuth2AuthenticationToken oauth
                && oauth.getPrincipal() instanceof OidcUser oidc) {

            String email = oidc.getEmail();
            String displayName = (String) oidc.getClaims().getOrDefault("name", email);

            String[] vn = splitVietnameseName(displayName, email);
            String firstName = vn[0];
            String lastName = vn[1];

            // Upsert DB (idempotent)
            UserCreationRequest req = UserCreationRequest.builder()
                    .email(email)
                    .username(null)
                    .password(null)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Role.CUSTOMER)
                    .build();
            String username = userService.registerVerified(req); // trả về username đã tạo/đã tồn tại
            // Thay Authentication OIDC bằng UserDetails nội bộ
            authenticationService.loginAfterRegisterWithoutPassword(username, request, response);
            // Lúc này SecurityContext đã là UserDetails -> xác định URL theo role nội bộ
            response.sendRedirect(determineRedirectUrl(
                    (Collection<? extends GrantedAuthority>)
                            org.springframework.security.core.context.SecurityContextHolder.getContext()
                                    .getAuthentication().getAuthorities()
            ));
            return;
        }

        // Form-login truyền thống
        response.sendRedirect(determineRedirectUrl(authentication.getAuthorities()));
    }

    private String determineRedirectUrl(Collection<? extends GrantedAuthority> authorities) {
        boolean isAdmin = authorities.stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
        boolean isStaff = authorities.stream().anyMatch(a -> ROLE_STAFF.equals(a.getAuthority()));
        if (isAdmin) return ADMIN_HOME_URL;
        if (isStaff) return STAFF_RESERVATIONS_URL;
        return CUSTOMER_HOME_URL;
    }

    private static String[] splitVietnameseName(String name, String email) {
        // fallback: lấy local-part trước @ nếu name trống
        if (name == null || name.isBlank()) {
            String local = (email == null) ? "user" : email.trim();
            int at = local.indexOf('@');
            if (at > 0) local = local.substring(0, at);
            return new String[]{ local, "" }; // firstName, lastName
        }
        name = name.trim();
        int i = name.lastIndexOf(' ');
        if (i < 0) {
            return new String[]{ name, "" };
        }
        String firstName = name.substring(i + 1).trim();
        String lastName  = name.substring(0, i).trim();
        return new String[]{ firstName, lastName };
    }

}
