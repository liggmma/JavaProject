package errorCode;
import java.io.*;

class PathTraversalExample {
    public static void main(String[] args) throws IOException {
        String userInput = "../secret.txt";
        File file = new File(userInput);
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            System.out.println("Reading file: " + file.getPath());
            reader.close();
        }
    }
}
