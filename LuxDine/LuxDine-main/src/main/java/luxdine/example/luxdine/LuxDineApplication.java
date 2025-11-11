package luxdine.example.luxdine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LuxDineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuxDineApplication.class, args);
    }

}
