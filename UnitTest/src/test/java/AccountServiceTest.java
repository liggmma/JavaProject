

import HuuTaiDE190451.example.AccountService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterAll;

import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceTest {

    private static final String OUTPUT_FILE = "UnitTestResult.csv";
    private static FileWriter writer;

    static {
        try {
            writer = new FileWriter(OUTPUT_FILE);
            writer.write("┌────────────────┬────────────────┬───────────────────────────┬────────────┬────────────┬────────┬────────────────────────────────────────────┐\n");
            writer.write(String.format("│ %-14s │ %-14s │ %-25s │ %-10s │ %-10s │ %-6s │ %-40s │%n",
                    "Username", "Password", "Email", "Expected", "Actual", "Result", "Log Message"));
            writer.write("├────────────────┼────────────────┼───────────────────────────┼────────────┼────────────┼────────┼────────────────────────────────────────────┤\n");
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo file kết quả test", e);
        }
    }



    private final AccountService service = new AccountService();

    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    @DisplayName("Kiểm thử chức năng đăng ký tài khoản từ file CSV")
    void testRegisterAccount(String username, String password, String email, boolean expected) throws IOException {
        boolean actual = service.registerAccount(username, password, email);
        boolean passed = (actual == expected);
        String log = service.getLastErrorMessage();

        writer.write(String.format("│ %-14s │ %-14s │ %-25s │ %-10s │ %-10s │ %-6s │ %-40s │%n",
                username, password, email, expected, actual, passed ? "PASS" : "FAIL", log));

        writer.flush();

        assertEquals(expected, actual);
    }

    @AfterAll
    static void closeFile() throws IOException {
        if (writer != null)
            writer.write("└────────────────┴────────────────┴───────────────────────────┴────────────┴────────────┴────────┴────────────────────────────────────────────┘\n");
            writer.close();

    }
}
