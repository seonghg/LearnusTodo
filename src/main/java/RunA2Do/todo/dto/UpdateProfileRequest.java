package RunA2Do.todo.dto;

public record UpdateProfileRequest(
        String userName,
        String department,
        String idNumber,
        String grade,
        String emailAddress,
        String newPassword
) {
}
