package RunA2Do.todo.dto;

public record TodoCreateResponse(
        boolean success,
        String message,
        Long eventId
) {
}
