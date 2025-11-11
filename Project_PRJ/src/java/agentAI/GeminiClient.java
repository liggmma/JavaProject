/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agentAI;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeminiClient {

    private static final String API_KEY = "AIzaSyDUiiJrW-bXWD94Jzx4n7BxVPZ7aNVnBKU"; // 🔐 Thay bằng API key thật của bạn
    private static final String API_URL
            = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String callGeminiAPI(String prompt) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject payload = new JsonObject();
        payload.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject obj = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray candidates = obj.getAsJsonArray("candidates");

            if (candidates != null && candidates.size() > 0) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                JsonArray contentArr = firstCandidate.getAsJsonObject("content").getAsJsonArray("parts");

                if (contentArr != null && contentArr.size() > 0) {
                    String result = contentArr.get(0).getAsJsonObject().get("text").getAsString();
                    if (result.startsWith("```")) {
                        result = result.replaceAll("(?s)```json|```", "").trim();
                    }
                    return result;
                }
            }
            return "🤖 Không có phản hồi từ AI.";
        } else {
            System.err.println("❌ Lỗi gọi API Gemini: " + response.statusCode());
            System.err.println("Nội dung trả về: " + response.body());
            return null;
        }
    }
}
