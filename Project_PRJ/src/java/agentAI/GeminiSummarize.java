/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agentAI;

import static agentAI.GeminiChat.getSimilarProductsFromFlask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeminiSummarize {

    private static final String API_KEY = "AIzaSyCHKi8NkIo9MhldZy3AoYlAOqeSsOcYJrI"; // 🔐 Thay bằng API key thật của bạn
    private static final String API_URL
            = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String summarizeUserRequest(List<ChatTurn> chatHistory) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        JsonArray contents = new JsonArray();

        for (ChatTurn turn : chatHistory) {
            JsonObject part = new JsonObject();
            part.addProperty("text", turn.getText());

            JsonArray parts = new JsonArray();
            parts.add(part);

            JsonObject content = new JsonObject();
            content.addProperty("role", turn.getRole());
            content.add("parts", parts);

            contents.add(content);
        }

        JsonObject payload = new JsonObject();
        payload.add("contents", contents);

        String baseKnowledge = """
            Hãy tóm tắt yêu cầu mua sắm của người dùng từ đoạn hội thoại trong dưới contents.
                        Hãy trả về tóm tắt yêu cầu của khách hàng.
                        Ví dụ như " tôi muốn mua..."
                        
                        Nếu không có thông tin cụ thể, bạn hãy trả về kết quả phản hồi duy nhất một từ là "blank"
                        Nếu khách hàng muốn đổi thông tin gì thì hãy trả về chỉ thông tin mà khách hàng mới vừa thay đổi. Ví dụ như khi khách
                        thay đổi password thì chỉ trả về password mới "new pass". Số điện thoại và Email cũng như vậy.
                        
                        Nếu khách hàng muốn thêm sản phẩm nào đó vào giỏ hàng thì chỉ trả về "product_id" của sản phẩm đó.
        """;

        JsonObject instructionPart = new JsonObject();
        instructionPart.addProperty("text", baseKnowledge);

        JsonArray instructionParts = new JsonArray();
        instructionParts.add(instructionPart);

        JsonObject systemInstruction = new JsonObject();
        systemInstruction.addProperty("role", "system");
        systemInstruction.add("parts", instructionParts);

        payload.add("systemInstruction", systemInstruction);

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
                    return contentArr.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
            return "Không có phản hồi từ AI.";
        } else {
            System.err.println("❌ Lỗi gọi API Gemini (summarize): " + response.statusCode());
            System.err.println("Nội dung trả về: " + response.body());
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            List<ChatTurn> history = new ArrayList<>();
            history.add(new ChatTurn("user", "Mình muốn đổi mật khẩu."));
            history.add(new ChatTurn("assistant", "Bạn muốn mật khẩu mới là gì?"));
            history.add(new ChatTurn("user", "mật khẩu mới của mình là 123456"));

            String summary = summarizeUserRequest(history);
            System.out.println("Tóm tắt từ Gemini:");
            System.out.println(summary);
            //String result = getSimilarProductsFromFlask(summary);
            //System.out.println(result);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi chạy main GeminiSummarize:");
            e.printStackTrace();
        }
    }
}
