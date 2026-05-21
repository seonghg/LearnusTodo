package RunA2Do.todo.controller;

import RunA2Do.todo.dto.ApiResponse;
import RunA2Do.todo.dto.TodoCreateResponse;
import RunA2Do.todo.dto.TodoRequest;
import RunA2Do.todo.security.AuthenticatedUsers;
import RunA2Do.todo.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping("/api/todos")
    public ResponseEntity<?> createTodo(
            @RequestBody TodoRequest request,
            Authentication authentication
    ) {
        String userId = AuthenticatedUsers.requireUserId(authentication);

        try {
            Long eventId = todoService.createTodo(userId, request);
            return ResponseEntity.ok(new TodoCreateResponse(
                    true,
                    "ToDo 일정이 추가되었습니다.",
                    eventId
            ));
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(
                        "선택한 과목에 접근할 수 없습니다."
                ));
            }
            return ResponseEntity.badRequest().body(ApiResponse.fail(
                    "입력값이 올바르지 않습니다."
            ));
        }
    }

    @DeleteMapping("/api/todos/{eventId}")
    public ResponseEntity<ApiResponse> deleteTodo(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        boolean deleted = todoService.deleteTodo(userId, eventId);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(
                    "삭제할 ToDo 일정을 찾을 수 없습니다."
            ));
        }

        return ResponseEntity.ok(ApiResponse.ok("ToDo 일정이 삭제되었습니다."));
    }
}
