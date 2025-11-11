/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agentAI;

import service.ProductService;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Products;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class SemanticTextImageSearch {

    static final String SERVER_URL = "http://localhost:5000";
    static final double MIN_SCORE_SEARCH_TEXT = -9.0;
    static final double MAX_SCORE_SEARCH_IMAGE = 0.6;

    static class ScoredProduct {

        int product_id;
        double score;
    }

    public static String extractPriceFilterInfo(String query) throws Exception {
        String prompt = """
            Hãy phân tích câu truy vấn sau. Nếu truy vấn có chứa điều kiện về giá, hãy trích xuất theo định dạng JSON sau:

            {
              "cleaned_query": "<phần còn lại của truy vấn không chứa giá>",
              "price_filter": {
                 "type": "BELOW" | "ABOVE" | "EQUAL",
                 "value": <số tiền tính bằng đồng>
              },
              "price_range": {
                 "min": <giá thấp nhất>,
                 "max": <giá cao nhất>
              }
            }

            Lưu ý:
            - Nếu truy vấn chứa khoảng giá, hãy dùng `price_range`.
            - Nếu chỉ có 1 giá (ví dụ: "khoảng 500k", "giá trên 2 triệu"), hãy dùng `price_filter`.
            - Nếu không có điều kiện về giá thì `price_filter` và `price_range` không được thêm vào.
            - Chỉ trả về kết quả dạng JSON, không chú thích, không markdown.

            Truy vấn: "%s"
            """.formatted(query);

        return GeminiClient.callGeminiAPI(prompt);
    }

    public List<Products> searchProductsByText(String query) {
        ProductService ps = new ProductService();
        List<Products> allProducts = ps.getAll();
        List<ScoredProduct> results = new ArrayList<>();

        String geminiJson = null;
        try {
            geminiJson = extractPriceFilterInfo(query); // Gọi API Gemini phân tích query
        } catch (Exception ex) {
            Logger.getLogger(SemanticTextImageSearch.class.getName()).log(Level.SEVERE, null, ex);
        }

        String cleanedQuery = query; // Mặc định nếu Gemini fail

        // Lấy cleaned_query từ Gemini nếu có
        try {
            JsonObject geminiObj = JsonParser.parseString(geminiJson).getAsJsonObject();
            if (geminiObj.has("cleaned_query")) {
                cleanedQuery = geminiObj.get("cleaned_query").getAsString();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi phân tích Gemini JSON, dùng query gốc.");
        }

        try {
            // Gửi cleaned_query sang Flask server
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("query", cleanedQuery);

            String json = new Gson().toJson(jsonMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/search"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Type listType = new TypeToken<List<ScoredProduct>>() {
            }.getType();
            results = new Gson().fromJson(response.body(), listType);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tăng điểm nếu từ khóa xuất hiện trong name/description
        String keyword = cleanedQuery.toLowerCase();
        Map<Integer, Double> boostedScores = new HashMap<>();
        for (ScoredProduct r : results) {
            double baseScore = r.score;
            for (Products p : allProducts) {
                if (p.getProductId() == r.product_id) {
                    if (p.getName().toLowerCase().contains(keyword) || p.getCategoryId().getName().toLowerCase().contains(keyword)) {
                        baseScore += 9.0;
                    } else if (p.getDescription().toLowerCase().contains(keyword)) {
                        baseScore += 5.0;
                    }
                    boostedScores.put(p.getProductId(), baseScore);
                    break;
                }
            }
        }

        // Bước lọc theo điểm
        List<Products> matchedProducts = new ArrayList<>();
        for (Products p : allProducts) {
            Double score = boostedScores.get(p.getProductId());
            if (score != null && score >= MIN_SCORE_SEARCH_TEXT) {
                matchedProducts.add(p);
            }
        }

        // Nếu ko có sản phẩm nào thì tìm kiếm theo tên thông thường
        if (matchedProducts.isEmpty()) {
            for (Products p : allProducts) {
                if (p.getName().contains(cleanedQuery)) {
                    matchedProducts.add(p);
                }
            }
        }

        // 👉 Lọc theo điều kiện giá nếu Gemini trả về
        try {
            JsonObject obj = JsonParser.parseString(geminiJson).getAsJsonObject();
            if (obj.has("price_range")) {
                JsonObject range = obj.getAsJsonObject("price_range");
                double min = range.get("min").getAsDouble();
                double max = range.get("max").getAsDouble();

                matchedProducts.removeIf(p -> {
                    double price = p.getBasePrice().doubleValue();
                    return price < min || price > max;
                });

            } else if (obj.has("price_filter")) {
                JsonObject pf = obj.getAsJsonObject("price_filter");
                String type = pf.get("type").getAsString().toUpperCase();
                double value = pf.get("value").getAsDouble();
                double tolerance = 50000;
                matchedProducts.removeIf(p -> {
                    double price = p.getBasePrice().doubleValue();
                    return switch (type) {
                        case "BELOW" ->
                            price >= value;
                        case "ABOVE" ->
                            price <= value;
                        case "EQUAL" ->
                            Math.abs(price - value) > tolerance;
                        default ->
                            false;
                    };
                });
            }
        } catch (Exception e) {
            System.err.println("⚠️ Không thể lọc theo giá từ kết quả Gemini.");
        }

        // Sắp xếp giảm dần theo boostedScore
        matchedProducts.sort((p1, p2)
                -> Double.compare(boostedScores.get(p2.getProductId()), boostedScores.get(p1.getProductId())));

        return matchedProducts;
    }

    public List<Products> searchProductsByImage(String imagePath) throws ParseException {
        ProductService ps = new ProductService();
        List<Products> allProducts = ps.getAll();
        List<ScoredProduct> scoredResults = new ArrayList<>();

        File file = new File(imagePath);
        if (!file.exists()) {
            System.err.println("❌ File không tồn tại: " + imagePath);
            return Collections.emptyList();
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(SERVER_URL + "/search-image");

            HttpEntity multipart = MultipartEntityBuilder.create()
                    .addBinaryBody("image", file, ContentType.create("image/jpeg"), file.getName())
                    .build();

            post.setEntity(multipart);

            try (ClassicHttpResponse response = client.execute(post)) {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    String responseJson = EntityUtils.toString(response.getEntity());
                    Type listType = new TypeToken<List<ScoredProduct>>() {
                    }.getType();
                    scoredResults = new Gson().fromJson(responseJson, listType);
                } else {
                    System.err.println("❌ Lỗi khi gửi ảnh: " + statusCode);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        // Lọc kết quả như cũ...
        Map<Integer, Double> scoreMap = new HashMap<>();
        for (ScoredProduct r : scoredResults) {
            if (r.score <= MAX_SCORE_SEARCH_IMAGE) {
                if (!scoreMap.containsKey(r.product_id) || r.score < scoreMap.get(r.product_id)) {
                    scoreMap.put(r.product_id, r.score);
                }
            }
        }
        List<Products> filtered = new ArrayList<>();
        for (Products p : allProducts) {
            if (scoreMap.containsKey(p.getProductId())) {
                filtered.add(p);
            }
        }
        filtered.sort(Comparator.comparingDouble(p -> scoreMap.get(p.getProductId())));
        return filtered;
    }

    static void printProducts(List<Products> products) {
        for (Products p : products) {
            System.out.println("\n-------------------------");
            System.out.println("Id: " + p.getProductId());
            System.out.println("📦 " + p.getName());
            System.out.println("Mô tả: " + p.getDescription());
            System.out.println("Giá: " + p.getBasePrice());
            System.out.println("Danh mục: " + p.getCategoryId().getName());
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        SemanticTextImageSearch main = new SemanticTextImageSearch();
        ProductService ps = new ProductService();
//        exportProductData(ps.getList());

        while (true) {
            System.out.print("\nTìm kiếm (1: Text, 2: Image, 0: Exit): ");
            String choice = scanner.nextLine();
            if (choice.equals("0")) {
                break;
            }

            switch (choice) {
                case "1" -> {
                    System.out.print("Nhập truy vấn: ");
                    String query = scanner.nextLine();
                    List<Products> result = main.searchProductsByText(query);
//                    printProducts(result);
                }
                case "2" -> {
                    System.out.print("Nhập đường dẫn ảnh hoặc URL: ");
                    String path = scanner.nextLine();
                    List<Products> result = main.searchProductsByImage(path);
                    printProducts(result);
                }
                default ->
                    System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }
}
