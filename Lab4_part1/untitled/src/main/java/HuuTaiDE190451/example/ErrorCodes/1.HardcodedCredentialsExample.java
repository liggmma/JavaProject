package HuuTaiDE190451.example.ErrorCodes;

public class HardcodedCredentialsExample {
    public static void main(String[] args) {
        String username = "admin";
        String password = "123456"; // hardcoded password
        if(authenticate(username, password)) {
            System.out.println("Access granted");
        } else {
            System.out.println("Access denied");
        }
    }

    private static boolean authenticate(String user, String pass) {
        // Dummy authentication logic
        return user.equals("admin") && pass.equals("123456");
    }
}
