package errorCode;
class CatchGenericExceptionExample {
    public static void main(String[] args) {
        try {
            String s = null;
            System.out.println(s.length());
        } catch (Exception _) {
            System.out.println("Something went wrong"); 
        }
    }
}
