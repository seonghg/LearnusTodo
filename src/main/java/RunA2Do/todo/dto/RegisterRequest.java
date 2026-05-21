package RunA2Do.todo.dto;

public record RegisterRequest(
        String userName,
        String department,
        String idNumber,
        String grade,
        String emailAddress,
        String userId,
        String password
) {
}
