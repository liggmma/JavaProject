package errorCode;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class ResourceLeakExample {
    private static final Logger LOGGER = Logger.getLogger(ResourceLeakExample.class.getName());

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String currentLine = line; // make it effectively final
                LOGGER.info(() -> "Read line: " + currentLine);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file data.txt", e);
        }
    }
}
