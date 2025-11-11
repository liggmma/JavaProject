package HuuTaiDE190451.example.ErrorCodes;
public class NullPointerExample {
    public static void main(String[] args) {
        String text = null;
        if (text.length() > 0) { 
            System.out.println("Text is not empty");
        }
    }
}
