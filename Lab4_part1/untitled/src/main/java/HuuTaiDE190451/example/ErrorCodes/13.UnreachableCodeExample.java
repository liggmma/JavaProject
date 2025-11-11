package HuuTaiDE190451.example.ErrorCodes;

public class UnreachableCodeExample {
    public static int getNumber() {
        return 42;
        System.out.println("This will never execute");
    }

    public static void main(String[] args) {
        System.out.println(getNumber());
    }
}
