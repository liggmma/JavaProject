// src/main/java/.../config/OAuth2BeansConfig.java
package luxdine.example.luxdine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Configuration
public class OAuth2BeansConfig {

    @Bean
    public OidcUserService customOidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                return delegate.loadUser(userRequest);
            }
        };
    }
}
