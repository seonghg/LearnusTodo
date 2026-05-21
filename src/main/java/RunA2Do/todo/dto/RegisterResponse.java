package RunA2Do.todo.dto;

public record RegisterResponse(
        boolean success,
        String message,
        String userId
) {
}
