package RunA2Do.todo.dto;

public record LearnUsSyncResponse(
        boolean success,
        String message,
        int courseCount,
        int eventCount
) {
}
