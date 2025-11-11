/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 *
 * @author ADMIN
 */
public class SearchServer {

    public static void checkAndStartFlaskServer() throws IOException {
        int port = 5000;
        if (isPortAvailable(port)) {
            System.out.println("🔧 Flask server chưa chạy. Đang khởi động search_server.py...");
            startPythonServer();
            boolean ready = waitForServer("http://localhost:5000/search", 60);
            if (!ready) {
                System.err.println("❌ Flask server không phản hồi sau 60 giây.");
                System.exit(1);
            }
            System.out.println("✅ Flask server đã sẵn sàng.");
        } else {
            System.out.println("✅ Flask server đã chạy.");
        }
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void startPythonServer() throws IOException {
        String command = "python"; // hoặc "python3" trên Linux/Mac
        String script = "search_server.py"; // tên file server Python

        ProcessBuilder pb = new ProcessBuilder(command, script);
        pb.directory(new File(".")); // thư mục hiện tại
        pb.inheritIO(); // hiển thị log Flask server trong console Java
        pb.start();
    }

    private static boolean waitForServer(String url, int timeoutSeconds) {
        HttpClient client = HttpClient.newHttpClient();
        long endTime = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (System.currentTimeMillis() < endTime) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(java.time.Duration.ofSeconds(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"ping\"}"))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 405) {
                    return true;
                }
            } catch (Exception ignored) {
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        checkAndStartFlaskServer();
    }
}
