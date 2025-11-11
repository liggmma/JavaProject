package luxdine.example.luxdine.config;

import com.vonage.client.VonageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class VonageConfig {
    @Bean
    public VonageClient vonageClient(
            @Value("${vonage.apiKey}") String apiKey,
            @Value("${vonage.apiSecret}") String apiSecret) {
        return VonageClient.builder().apiKey(apiKey).apiSecret(apiSecret).build();
    }
}