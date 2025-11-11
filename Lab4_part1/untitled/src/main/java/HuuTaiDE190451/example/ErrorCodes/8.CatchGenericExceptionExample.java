package HuuTaiDE190451.example.ErrorCodes;
public class CatchGenericExceptionExample {
    public static void main(String[] args) {
        try {
            String s = null;
            System.out.println(s.length());
        } catch (Exception e) {
            System.out.println("Something went wrong"); 
        }
    }
}
