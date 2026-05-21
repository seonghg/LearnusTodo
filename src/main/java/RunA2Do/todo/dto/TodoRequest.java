package RunA2Do.todo.dto;

public record TodoRequest(
        String title,
        String courseId,
        String dueDate,
        String priority
) {
}
