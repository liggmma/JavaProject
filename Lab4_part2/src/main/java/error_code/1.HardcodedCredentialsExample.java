package error_code;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HardcodedCredentialsExample {
    private static final Logger logger = LoggerFactory.getLogger(HardcodedCredentialsExample.class);

    public static void main(String[] args) {
        String username = "admin";
        String password = "123456"; // hardcoded password

        if (authenticate(username, password)) {
            logger.info("Access granted");
        } else {
            logger.warn("Access denied");
        }
    }

    private static boolean authenticate(String user, String pass) {
        return user.equals("admin") && pass.equals("123456");
    }
}
