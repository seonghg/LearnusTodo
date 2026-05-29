package RunA2Do.todo.dto;

import java.util.List;

public record ChatbotRequest(
        List<ChatbotMessage> messages
) {
}
