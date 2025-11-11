/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agentAI;

import java.util.*;
import model.Products;
import service.ProductService;

public class RecommendAI {

    public static String extractKeywords(String text) throws Exception {
        String prompt = """
            Văn bản dưới đây là mô tả sản phẩm, có thể bao gồm nhiều phần như hướng dẫn sử dụng, chính sách bảo hành, cam kết của shop, v.v.

            Nhiệm vụ của bạn:
            1. Chỉ tập trung vào phần mô tả sản phẩm chính. **Bỏ qua mọi thông tin không liên quan đến đặc điểm hoặc tính năng sản phẩm**.
            2. Nếu mô tả không phải là tiếng anh thì dịch sang tiếng anh, giữ nguyên sắc thái và ngữ cảnh (không cần trả về đoạn dịch).
            3. Từ bản dịch tiếng anh đó, **trích xuất danh sách các từ khóa chính**, cụ thể:
               - Chỉ bao gồm **danh từ (nouns)** liên quan đến sản phẩm.
               - Mỗi từ khóa phải là **một từ đơn**, không phải cụm từ.
               - Tất cả từ khóa phải được **lemmatized** (dạng gốc).
               - Ghi thường, cách nhau bằng **dấu phẩy**, không xuống dòng, không định dạng.

            ❗️**Chỉ trả về một chuỗi chứa danh sách các từ khóa** sau bước 3. Không thêm chú thích, không in lại đoạn văn, không liệt kê bước nào.

            📦 Nội dung sản phẩm:
            "%s"
            """.formatted(text);

        String response = GeminiClient.callGeminiAPI(prompt);

        if (response == null || response.isBlank()) {
            return "";
        }

        // Làm sạch kết quả nếu cần (xóa khoảng trắng dư thừa)
        return response.trim().toLowerCase();
    }

    public static double calculateSimilarity(String keywords1, String keywords2) {
        if (keywords1 == null || keywords2 == null || keywords1.isBlank() || keywords2.isBlank()) {
            return 0.0;
        }

        Set<String> set1 = new HashSet<>(Arrays.asList(keywords1.split("\\s*,\\s*")));
        Set<String> set2 = new HashSet<>(Arrays.asList(keywords2.split("\\s*,\\s*")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2); // giao

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2); // hợp

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    public static List<Products> getRecommendations(List<Products> likedProducts, List<Products> allProducts) {
        Map<Products, Double> scores = new HashMap<>();
        for (Products liked : likedProducts) {
            for (Products product : allProducts) {
                if (!liked.getProductId().equals(product.getProductId())) {
                    double score = calculateSimilarity(liked.getTags(), product.getTags());
                    scores.put(product, scores.getOrDefault(product, 0.0) + score);
                }
            }
        }
        List<Products> recommendations = new ArrayList<>(scores.keySet());
        recommendations.sort((p1, p2) -> Double.compare(scores.get(p2), scores.get(p1)));
        return recommendations;
    }

    public static void main(String[] args) throws Exception {
        ProductService psv = new ProductService();

        for (Products p : psv.getAll()) {
            p.setTags(extractKeywords(p.getDescription()));
            psv.update(p);

            try {
                Thread.sleep(5000); // Delay 1 giây
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Khôi phục trạng thái interrupt
                System.out.println("Thread bị gián đoạn: " + e.getMessage());
            }
        }

    }

}
