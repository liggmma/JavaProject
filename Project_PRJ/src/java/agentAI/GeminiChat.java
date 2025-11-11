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

import java.util.List;

import service.ProductService;
import model.Products;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import model.Users;
import service.UserService;

public class GeminiChat {

    static ProductService productService = new ProductService();

    private static final String API_KEY = "AIzaSyDUiiJrW-bXWD94Jzx4n7BxVPZ7aNVnBKU"; // 🔐 Thay bằng API key thật của bạn
    private static final String API_URL
            = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String callGeminiAPI(List<ChatTurn> chatHistory, String userInfoString) throws Exception {
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

        String dataBase = getFormattedProductListFromSummary(GeminiSummarize.summarizeUserRequest(chatHistory), productService);

        String instructionText = "Đoạn trên là đoạn chat giữa khách hàng và trợ lý AI. "
                + "Nếu khách hàng muốn tìm sản phẩm từ cửa hàng ShopGo và bạn là trợ lý bán hàng, đây là những sản phẩm có thể gợi ý:\n"
                + dataBase
                + "Nếu khách hàng muốn thay đổi password, số điện thoại hay email hãy hỏi khách hàng và yêu cầu khách hàng nhập theo cú pháp \"mật khẩu mới(số điện thoại, email) mới của tôi là\"  "
                + "Nếu khách hàng muốn thêm sản phẩm này vào giỏ hàng thì hãy kêu cầu khách hàng nhập theo cú pháp \"tôi muốn thêm sản phẩm... vào giỏ hàng\""
                + "Và đây là thông tin của người dùng nếu khách hàng muốn biết thông tin của mình. Nếu khách hàng quên mật khẩu thì hãy show cho khách hàng" + userInfoString;

        JsonObject systemInstruction = new JsonObject();
        systemInstruction.addProperty("role", "system");

        JsonObject part = new JsonObject();
        part.addProperty("text", instructionText);

        JsonArray parts = new JsonArray();
        parts.add(part);

        systemInstruction.add("parts", parts);

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
            System.err.println("❌ Lỗi gọi API Gemini: " + response.statusCode());
            System.err.println("Nội dung trả về: " + response.body());
            return null;
        }
    }

    public static String getSimilarProductsFromFlask(String summary) {
        try {
            if (summary == null || summary.trim().isEmpty() || summary.equalsIgnoreCase("blank")) {
                System.out.println("⚠️ Không có yêu cầu cụ thể => bỏ qua tìm kiếm sản phẩm.");
                return "[]";
            }

            // Escape JSON an toàn bằng Gson
            JsonObject json = new JsonObject();
            json.addProperty("summary", summary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5000/similar-products"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Flask trả về mã lỗi HTTP " + response.statusCode());
                System.err.println(" Nội dung lỗi: " + response.body());
                return "[]";
            }

        } catch (Exception e) {
            System.err.println(" Lỗi khi gọi Flask:");
            e.printStackTrace();
            return "[]";
        }
    }

    public static String getFormattedProductListFromSummary(String summary, ProductService productService) {
        StringBuilder result = new StringBuilder();

        try {
            // Gọi Flask để lấy JSON kết quả tương đồng
            String json = getSimilarProductsFromFlask(summary);
            JSONObject obj = new JSONObject(json);
            JSONArray results = obj.getJSONArray("results");

            if (results.length() == 0) {
                return "️ Không tìm thấy sản phẩm nào tương đồng.";
            }

            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);

                if (!item.has("product_id")) {
                    result.append(i + 1).append(".️ Không có product_id trong kết quả.\n");
                    continue;
                }

                int productId = item.getInt("product_id");
                Products product = productService.findById(productId);

                if (product != null) {
                    result.append(i + 1).append(". ✅ ").append(product.toString()).append("\n");
                } else {
                    result.append(i + 1).append(".  Sản phẩm ID ").append(productId).append(" không tồn tại trong CSDL.\n");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý danh sách sản phẩm từ Flask:");
            e.printStackTrace();
            return "❌ Đã xảy ra lỗi khi lấy danh sách sản phẩm từ Flask.";
        }

        return result.toString().trim();
    }

    private static final UserService userService = new UserService();

    public static boolean updatePassword(Users user, String newPassword) {
        try {
            userService.editUser(user.getUserId(),
                    user.getUsername(),
                    newPassword,
                    user.getEmail(),
                    user.getPhone(), (java.sql.Date) user.getDateOfBirth(),
                    user.getAvatarUrl(),
                    user.getRole()
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updatePhone(Users user, String newPhone) {
        try {
            userService.editUser(user.getUserId(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.getEmail(),
                    newPhone, (java.sql.Date) user.getDateOfBirth(),
                    user.getAvatarUrl(),
                    user.getRole()
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateEmail(Users user, String newEmail) {
        try {
            userService.editUser(user.getUserId(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    newEmail,
                    user.getPhone(), (java.sql.Date) user.getDateOfBirth(),
                    user.getAvatarUrl(),
                    user.getRole()
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
