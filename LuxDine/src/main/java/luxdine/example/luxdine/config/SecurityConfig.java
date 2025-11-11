package luxdine.example.luxdine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfig(@Lazy CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    private static final String[] PUBLIC = {
            "/", "/login", "/register", "/home",
            "/layout", "/menu/**", "/browse/**",
            "/css/**", "/js/**", "/images/**"
    };
    
    private static final String[] STAFF_ENDPOINTS = {
            "/api/kitchen/**",      // Kitchen API
            "/api/tables/**",       // Floor Plan API
            "/staff/**"
    };

    private static final String[] ADMIN_ENDPOINTS = {
            "/admin/**",
            "/api/work-schedule/**",
            "/api/attendance/**"
    };

    private static final String[] API_ENDPOINTS = {
            "/api/reservations/**", // Reservation Management API
            "/api/kitchen/**",      // Kitchen API
            "/api/tables/**",       // Floor Plan API
            "/api/admin/**",        // Admin API
            "/api/staff/**"         // Staff read-only API
    };
    
    private static final String[] CHAT_API_ENDPOINTS = {
            "/api/chat/**"          // Chat API - accessible to all authenticated users
    };

    private static final String[] AI_CHATBOT_API_ENDPOINTS = {
            "/api/ai-chatbot/sessions",
            "/api/ai-chatbot/messages",
            "/api/ai-chatbot/sessions/*/messages",
            "/api/ai-chatbot/stats"
    };

    private static final String[] AI_CHATBOT_AUTH_ENDPOINTS = {
            "/api/ai-chatbot/reservation/**"  // Reservation booking requires authentication
    };

    private static final String[] PAYMENT_ENDPOINTS = {
            "/payment/**"           // Payment endpoints including checkout
    };

    private static final String[] OPEN = {
            "/webhook/**",          // webhook từ SePay
            "/ws/**", "/sockjs/**", // SockJS/WS handshake & transports
            "/api/public/**"        // Public API for customers (layout, features, tables)
    };

    private static final String[] OTP_ENDPOINTS = {
            "/auth/otp/**",
            "/auth/verify/**",
            "/auth/otp-login/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OidcUserService customOidcUserService) throws Exception {
        http
                .cors(c -> {}) // << bật CORS integration với Security (dựa vào bean CorsConfigurationSource)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(OTP_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC).permitAll()
                        .requestMatchers(OPEN).permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                        .requestMatchers("/staff/chat/**").hasAnyRole("STAFF", "ADMIN") // Chat for STAFF and ADMIN
                        .requestMatchers(STAFF_ENDPOINTS).hasAnyRole("STAFF","ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin API requires ADMIN role
                        .requestMatchers(API_ENDPOINTS).permitAll()
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF","ADMIN") // Staff APIs readable by STAFF/ADMIN
                        .requestMatchers(API_ENDPOINTS).hasAnyRole("STAFF","ADMIN")
                        .requestMatchers(CHAT_API_ENDPOINTS).authenticated() // Chat API for all authenticated users
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(OPEN)
                        .ignoringRequestMatchers(API_ENDPOINTS) // Disable CSRF for API endpoints (kitchen/tables/admin/staff)
                        .ignoringRequestMatchers("/api/admin/**") // Disable CSRF for Admin API
                        .ignoringRequestMatchers("/api/public/**") // Disable CSRF for Public API
                        .ignoringRequestMatchers("/api/attendance/**") // Disable CSRF for Attendance API
                        .ignoringRequestMatchers(CHAT_API_ENDPOINTS) // Disable CSRF for Chat API
                )
                .formLogin(form -> form
                        .loginPage("/login") // custom login page
                        .successHandler(this.customAuthenticationSuccessHandler) // custom success handler
                )
                .oauth2Login(o -> o
                        .loginPage("/login")
                        .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                        .successHandler(this.customAuthenticationSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                );
                // Temporarily disabled for testing chat feature
                // .sessionManagement(session -> session
                //         .maximumSessions(1) // Prevent multiple sessions
                //         .maxSessionsPreventsLogin(false) // Allow new login to invalidate old session
                // );

        return http.build();
    }

    /** CORS cho ngrok + localhost (Security sẽ tự lấy cấu hình này) */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
                "https://*.ngrok-free.dev",
                "https://*.ngrok-free.app",
                "http://localhost:*", "https://localhost:*"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

}