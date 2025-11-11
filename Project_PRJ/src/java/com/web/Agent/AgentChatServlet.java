///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
// */
//package com.web.Agent;
//
//
//
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.ollama.OllamaChatModel;
//import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
//import dev.langchain4j.retriever.EmbeddingStoreRetriever;
//import dev.langchain4j.retriever.Retriever;
//import dev.langchain4j.service.AiServices;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.json.JSONObject;
//import agentAI.*;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.List;
//
//@WebServlet(name = "chat", urlPatterns = {"/chat"})
//public class AgentChatServlet extends HttpServlet {
//
//    private ProductAssistant assistant;
//    private Retriever retriever;
//    private ChatMemory memory;
//
//    @Override
//    public void init() throws ServletException {
//        try {
//            OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
//                    .baseUrl("http://localhost:11434")
//                    .modelName("mxbai-embed-large")
//                    .build();
//
//            OllamaChatModel chatModel = OllamaChatModel.builder()
//                    .baseUrl("http://localhost:11434")
//                    .modelName("llama3")
//                    .build();
//
//            ProductLoader loader = new ProductLoader(embeddingModel);
//
//            retriever = new EmbeddingStoreRetriever(loader.getStore(), embeddingModel, 5, 0.5);
//            memory = MessageWindowChatMemory.withMaxMessages(30);
//
//            assistant = AiServices.builder(ProductAssistant.class)
//                    .chatLanguageModel(chatModel)
//                    .chatMemory(memory)
//                    .build();
//
//        } catch (Exception e) {
//            throw new ServletException("Failed to initialize AI agent", e);
//        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        // Đọc JSON từ frontend
//        BufferedReader reader = req.getReader();
//        StringBuilder jsonBuilder = new StringBuilder();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            jsonBuilder.append(line);
//        }
//
//        JSONObject json = new JSONObject(jsonBuilder.toString());
//        String query = json.getString("message").trim();
//
//        if (query.equalsIgnoreCase("quit")) {
//            memory.clear();
//            respond(resp, "Tạm biệt! Hẹn gặp lại bạn.");
//            return;
//        }
//
//        // Truy xuất các văn bản liên quan
//        List<TextSegment> relevantDocs = retriever.findRelevant(query);
//        StringBuilder context = new StringBuilder();
//
//        for (TextSegment doc : relevantDocs) {
//            context.append(doc.text()).append("\n");
//        }
//
//        String finalPrompt = """
//                Bạn là một trợ lý bán hàng thông minh. Dưới đây là dữ liệu về các sản phẩm trong cửa hàng, bao gồm: tên sản phẩm, loại sản phẩm, xuất xứ, giá tiền, đánh giá (trên 10), số lượng đã bán, và mô tả.
//
//                Tôi là khách hàng, tôi có thể hỏi theo nhiều dạng như:
//                - "Tôi muốn tìm sản phẩm thuộc loại Gaming"
//                - "Các loại sản phẩm trong cửa hàng là gì?"
//                - "Có sản phẩm nào đánh giá cao không?"
//                - "Tôi ở Đà Nẵng, thời gian giao khoảng bao lâu?"
//
//                Yêu cầu của bạn:
//                1. Nếu tôi yêu cầu tìm theo loại sản phẩm (Category), hãy liệt kê các sản phẩm thuộc loại đó có điểm đánh giá (Rating) cao nhất.
//                2. Nếu tôi muốn xem các loại sản phẩm đang bán, hãy liệt kê tất cả các loại duy nhất xuất hiện trong dữ liệu.
//                3. Nếu tôi hàng cần đề xuất, hãy ưu tiên các sản phẩm có Rating trên 9 hoặc số lượng Sold cao nhất.
//                4. Nếu tôi cung cấp địa chỉ (ví dụ: Hà Nội, TP.HCM, Đà Nẵng), hãy ước lượng thời gian giao hàng dựa trên nguồn gốc (Origin) của sản phẩm:
//                    - Nếu Origin cùng khu vực (Việt Nam): 1–2 ngày
//                    - Cùng châu lục (châu Á): 3–5 ngày
//                    - Khác châu lục: 7–14 ngày
//                5. Nếu sản phẩm tôi quan tâm không còn trong kho thì hãy thông báo với tôi là hàng đã hết hoặc nếu hàng tồn kho rất thấp mà hàng bán được lại rất nhiều thì hãy đề xuất nên mua sớm kẻo hết.
//                
//                Dưới đây là dữ liệu sản phẩm:
//
//                """ + context + "\n\nCâu hỏi của khách hàng: " + query;
//
//        String response = assistant.answer(finalPrompt);
//        respond(resp, response);
//    }
//
//    private void respond(HttpServletResponse resp, String message) throws IOException {
//        resp.setContentType("application/json");
//        resp.setCharacterEncoding("UTF-8");
//        String json = new JSONObject().put("reply", message).toString();
//        resp.getWriter().write(json);
//    }
//}
