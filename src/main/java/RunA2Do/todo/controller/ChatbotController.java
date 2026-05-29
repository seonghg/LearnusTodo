package RunA2Do.todo.controller;

import RunA2Do.todo.dto.ChatbotMessage;
import RunA2Do.todo.dto.ChatbotRequest;
import RunA2Do.todo.dto.ChatbotResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final URI MISTRAL_CHAT_COMPLETIONS =
            URI.create("https://api.mistral.ai/v1/chat/completions");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public ChatbotController(
            ObjectMapper objectMapper,
            @Value("${mistral.api-key:}") String apiKey,
            @Value("${mistral.model:mistral-small-latest}") String model
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatbotResponse> message(@RequestBody ChatbotRequest request)
            throws IOException, InterruptedException {
        if (isBlank(apiKey)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatbotResponse("Mistral API key가 서버에 설정되지 않았습니다."));
        }

        if (request.messages() == null || request.messages().isEmpty()) {
            return ResponseEntity.badRequest().body(new ChatbotResponse("Message is required."));
        }

        HttpResponse<String> response = callMistral(request.messages());

        if (!isSuccess(response)) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ChatbotResponse(mistralErrorMessage(response)));
        }

        JsonNode root = objectMapper.readTree(response.body());
        String answer = root.path("choices").path(0).path("message").path("content").asText();
        if (isBlank(answer)) {
            answer = "응답을 불러오지 못했습니다.";
        }

        return ResponseEntity.ok(new ChatbotResponse(answer));
    }

    private HttpResponse<String> callMistral(List<ChatbotMessage> messages)
            throws IOException, InterruptedException {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages
        );

        HttpRequest request = HttpRequest.newBuilder(MISTRAL_CHAT_COMPLETIONS)
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean isSuccess(HttpResponse<String> response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    private String mistralErrorMessage(HttpResponse<String> response) {
        try {
            JsonNode error = objectMapper.readTree(response.body()).path("error");
            String message = error.path("message").asText();
            String code = error.path("code").asText();
            String type = error.path("type").asText();

            if (!isBlank(message)) {
                String detail = !isBlank(code) ? code : type;
                return !isBlank(detail)
                        ? "Mistral 오류: " + message + " (" + detail + ")"
                        : "Mistral 오류: " + message;
            }
        } catch (RuntimeException ignored) {
            // Fall back to the HTTP status when Mistral returns a non-JSON error body.
        }

        return "Mistral 연결 실패: HTTP " + response.statusCode();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
