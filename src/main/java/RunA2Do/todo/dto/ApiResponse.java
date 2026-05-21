package RunA2Do.todo.dto;

public record ApiResponse(
        boolean success,
        String message
) {
    public static ApiResponse ok(String message) {
        return new ApiResponse(true, message);
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse(false, message);
    }
}
