/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package agentAI;

/**
 *
 * @author ASUS
 */
import dev.langchain4j.service.UserMessage;

public interface ProductAssistant {

    @UserMessage("Customer: {{it}}")
    String answer(String message);
}
