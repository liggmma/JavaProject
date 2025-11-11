package luxdine.example.luxdine.service.auth;

import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Nếu account social/OTP không có password, gán placeholder không-rỗng
        String pwd = (user.getPassword() == null || user.getPassword().isBlank())
                ? "{noop}N/A"   // không dùng để xác thực form, chỉ để thỏa constructor
                : user.getPassword();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                pwd,
                user.isActive(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
