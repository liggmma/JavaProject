/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author ASUS
 */
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.service.AiServices;
import agentAI.*;
import java.util.List;

public class AgentService {

    private final ProductAssistant assistant;
    private final Retriever retriever;
    private final ChatMemory memory;

    public AgentService() throws Exception {
        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("mxbai-embed-large")
                .build();

        OllamaChatModel chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .build();

        ProductLoader loader = new ProductLoader(embeddingModel);

        this.retriever = new EmbeddingStoreRetriever(loader.getStore(), embeddingModel, 5, 0.5);
        this.memory = MessageWindowChatMemory.withMaxMessages(30);

        this.assistant = AiServices.builder(ProductAssistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(memory)
                .build();
    }

    public String chat(String userMessage) {
        if ("quit".equalsIgnoreCase(userMessage)) {
            memory.clear();
            return "Tạm biệt! Hẹn gặp lại bạn.";
        }

        List<TextSegment> relevantDocs = retriever.findRelevant(userMessage);
        StringBuilder context = new StringBuilder();

        for (TextSegment doc : relevantDocs) {
            context.append(doc.text()).append("\n");
        }

        String prompt = """
                Bạn luôn trả lời bằng tiếng việt
                Bạn là một trợ lý bán hàng thông minh. Dưới đây là dữ liệu về các sản phẩm trong cửa hàng, bao gồm: tên sản phẩm, loại sản phẩm, xuất xứ, giá tiền, đánh giá (trên 10), số lượng đã bán, và mô tả.

                Tôi là khách hàng, tôi có thể hỏi theo nhiều dạng như:
                - "Tôi muốn tìm sản phẩm thuộc loại Gaming"
                - "Các loại sản phẩm trong cửa hàng là gì?"
                - "Có sản phẩm nào đánh giá cao không?"
                - "Tôi ở Đà Nẵng, thời gian giao khoảng bao lâu?"

                Yêu cầu của bạn:
                1. Nếu tôi yêu cầu tìm theo loại sản phẩm (Category), hãy liệt kê các sản phẩm thuộc loại đó có điểm đánh giá (Rating) cao nhất.
                2. Nếu tôi muốn xem các loại sản phẩm đang bán, hãy liệt kê tất cả các loại duy nhất xuất hiện trong dữ liệu.
                3. Nếu tôi hàng cần đề xuất, hãy ưu tiên các sản phẩm có Rating trên 9 hoặc số lượng Sold cao nhất.
                4. Nếu tôi cung cấp địa chỉ (ví dụ: Hà Nội, TP.HCM, Đà Nẵng), hãy ước lượng thời gian giao hàng dựa trên nguồn gốc (Origin) của sản phẩm:
                    - Nếu Origin cùng khu vực (Việt Nam): 1–2 ngày
                    - Cùng châu lục (châu Á): 3–5 ngày
                    - Khác châu lục: 7–14 ngày
                5. Nếu sản phẩm tôi quan tâm không còn trong kho thì hãy thông báo với tôi là hàng đã hết hoặc nếu hàng tồn kho rất thấp mà hàng bán được lại rất nhiều thì hãy đề xuất nên mua sớm kẻo hết.

                Dưới đây là dữ liệu sản phẩm:

                """ + context + "\n\nCâu hỏi của khách hàng: " + userMessage;

        return assistant.answer(prompt);
    }
}
