package HuuTaiDE190451.example.ErrorCodes;
interface Constants {
    int MAX_USERS = 100;
}

public class InterfaceFieldModificationExample {
    public static void main(String[] args) {
        // Constants.MAX_USERS = 200; // Compile-time error
    }
}
