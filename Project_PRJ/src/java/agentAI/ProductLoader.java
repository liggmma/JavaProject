/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package agentAI;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ProductLoader {

    private final EmbeddingStore<TextSegment> store;

    public ProductLoader(OllamaEmbeddingModel embeddingModel) throws Exception {
        store = new InMemoryEmbeddingStore<>();

        // Đọc file CSV từ classpath (nằm trong src/main/resources hoặc WEB-INF/classes)
        InputStream is = getClass().getClassLoader().getResourceAsStream("products.csv");

        if (is == null) {
            throw new RuntimeException("❌ Không tìm thấy file products.csv trong classpath");
        }

        // Đọc nội dung file thành chuỗi
        String content;
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A"); // Đọc toàn bộ nội dung
            content = scanner.hasNext() ? scanner.next() : "";
        }

        // Phân tách các đoạn dữ liệu sản phẩm (cách nhau bởi 2 dòng trống)
        String[] entries = content.split("\\R{2,}");

        for (String entry : entries) {
            if (entry.isBlank()) {
                continue;
            }
            TextSegment segment = TextSegment.from(entry.trim());
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            store.add(embedding, segment);
        }
    }

    public EmbeddingStore<TextSegment> getStore() {
        return store;
    }
}
