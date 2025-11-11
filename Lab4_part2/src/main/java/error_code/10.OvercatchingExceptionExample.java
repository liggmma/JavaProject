package errorCode;
public class OvercatchingExceptionExample {
    public static void main(String[] args) {
        try {
            int[] arr = new int[5];
            System.out.println(arr[10]); 
        } catch (RuntimeException e) {
            System.out.println("Runtime error"); 
        }
    }
}
