///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package agentAI;
//
///**
// *
// * @author ASUS
// */
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.model.ollama.OllamaChatModel;
//import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.retriever.EmbeddingStoreRetriever;
//import dev.langchain4j.retriever.Retriever;
//import dev.langchain4j.service.AiServices;
//
//import java.util.List;
//import java.util.Scanner;
//
//public class AgentRunner {
//
//    public static void main(String[] args) throws Exception {
//
//        OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
//                .baseUrl("http://localhost:11434")
//                .modelName("mxbai-embed-large")
//                .build();
//
//        OllamaChatModel chatModel = OllamaChatModel.builder()
//                .baseUrl("http://localhost:11434")
//                .modelName("llama3")
//                .build();
//
//        ProductLoader loader = new ProductLoader(embeddingModel);
//
//        Retriever retriever = new EmbeddingStoreRetriever(loader.getStore(), embeddingModel, 5, 0.5);
//
//        ChatMemory memory = MessageWindowChatMemory.withMaxMessages(30);
//
//        ProductAssistant assistant = AiServices.builder(ProductAssistant.class)
//                .chatLanguageModel(chatModel)
//                .chatMemory(memory)
//                .build();
//
//        Scanner scanner = new Scanner(System.in);
//
//        while (true) {
//            System.out.print("======= Can I help you? (Input \"quit\" to out.) >>> ");
//            String query = scanner.nextLine().trim();
//            System.out.println("============================================================================\n");
//            System.out.println("Waiting a second...\n");
//
//            if (query.isBlank()) {
//                System.out.println("❗Bạn chưa nhập gì cả. Vui lòng thử lại.\n");
//                continue; 
//            }
//
//            if (query.equalsIgnoreCase("quit")) {
//                memory.clear();
//                System.out.println("See you next time!\n");
//                break;
//            }
//
//            List<TextSegment> relevantDocs = retriever.findRelevant(query);
//            StringBuilder context = new StringBuilder();
//
//            for (TextSegment doc : relevantDocs) {
//                context.append(doc.text()).append("\n");
//            }
//            String finalPrompt = "String finalPrompt = \"\"\"\n"
//                    + "Bạn là một trợ lý bán hàng thông minh. Dưới đây là dữ liệu về các sản phẩm trong cửa hàng, bao gồm: tên sản phẩm, loại sản phẩm, xuất xứ, giá tiền, đánh giá (trên 10), số lượng đã bán, và mô tả.\n"
//                    + "\n"
//                    + "Tôi là khách hàng, tôi có thể hỏi theo nhiều dạng như:\n"
//                    + "- \"Tôi muốn tìm sản phẩm thuộc loại Gaming\"\n"
//                    + "- \"Các loại sản phẩm trong cửa hàng là gì?\"\n"
//                    + "- \"Có sản phẩm nào đánh giá cao không?\"\n"
//                    + "- \"Tôi ở Đà Nẵng, thời gian giao khoảng bao lâu?\"\n"
//                    + "\n"
//                    + "Yêu cầu của bạn:\n"
//                    + "1. Nếu tôi yêu cầu tìm theo loại sản phẩm (Category), hãy liệt kê các sản phẩm thuộc loại đó có điểm đánh giá (Rating) cao nhất.\n"
//                    + "2. Nếu tôi muốn xem các loại sản phẩm đang bán, hãy liệt kê tất cả các loại duy nhất xuất hiện trong dữ liệu.\n"
//                    + "3. Nếu tôi hàng cần đề xuất, hãy ưu tiên các sản phẩm có Rating trên 9 hoặc số lượng Sold cao nhất.\n"
//                    + "4. Nếu tôi cung cấp địa chỉ (ví dụ: Hà Nội, TP.HCM, Đà Nẵng), hãy ước lượng thời gian giao hàng dựa trên nguồn gốc (Origin) của sản phẩm:\n"
//                    + "    - Nếu Origin cùng khu vực (Việt Nam): 1–2 ngày\n"
//                    + "    - Cùng châu lục (châu Á): 3–5 ngày\n"
//                    + "    - Khác châu lục: 7–14 ngày\n"
//                    + "\n"
//                    + "5.Nếu tôi hỏi về sản phẩm rẻ nhất hoặc so sánh giá, hãy phân tích các giá tiền có trong dữ liệu và đưa ra đề xuất sản phẩm phù hợp.\n\n"
//                    + "6.Nếu sản phẩm tôi quan tâm không còn trong kho thì hãy thông báo với tôi là hàng đã hết hoặc nếu hàng tồn kho rất thấp mà hàng bán được lại rất nhiều thì hãy đề xuất nên mua sớm kẻo hết.\n\n"
//                    + "Dưới đây là dữ liệu sản phẩm:\n"
//                    + "\n"
//                    + context + "\n\nCâu hỏi của khách hàng: " + query;
//
//            String response = assistant.answer(finalPrompt);
//            System.out.println("===== STORE'S ASSISTANT ===>\n" + response);
//            System.out.println("============================================================================\n");
//        }
//    }
//}
//
