package RunA2Do.todo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@RestController
public class TodoController {

    private static final String SOURCE_TYPE = "USER_TODO";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcTemplate jdbcTemplate;

    public TodoController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/api/todos")
    public ResponseEntity<Map<String, Object>> createTodo(
            @RequestParam String title,
            @RequestParam String courseId,
            @RequestParam String dueDate,
            @RequestParam String priority,
            Authentication authentication
    ) {
        String userId = requireUserId(authentication);

        if (!isValidPriority(priority)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "우선순위 값이 올바르지 않습니다."
            ));
        }

        Long resolvedCourseId = resolveCourseId(courseId);

        if (resolvedCourseId != null) {
            Integer courseCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM user_courses
                    WHERE user_id = ?
                      AND course_id = ?
                      AND is_active = TRUE
                    """,
                    Integer.class,
                    userId,
                    resolvedCourseId
            );

            if (courseCount == null || courseCount == 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "선택한 과목에 접근할 수 없습니다."
                ));
            }
        }

        OffsetDateTime startTime = LocalDateTime.parse(dueDate).atZone(KOREA_ZONE).toOffsetDateTime();
        String externalEventId = "todo-" + UUID.randomUUID();
        String description = "priority=" + priority
                + (resolvedCourseId == null ? ";category=개인 일정" : "");

        Long eventId = jdbcTemplate.queryForObject(
                """
                INSERT INTO calendar (
                    user_id,
                    title,
                    description,
                    start_time,
                    end_time,
                    is_all_day,
                    location,
                    course_id,
                    source_type,
                    external_event_id,
                    updated_at
                )
                VALUES (?, ?, ?, ?, NULL, FALSE, NULL, ?, ?, ?, now())
                RETURNING event_id
                """,
                Long.class,
                userId,
                title,
                description,
                startTime,
                resolvedCourseId,
                SOURCE_TYPE,
                externalEventId
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ToDo 일정이 추가되었습니다.",
                "eventId", eventId
        ));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required.");
        }
        return authentication.getName();
    }

    private boolean isValidPriority(String priority) {
        return "빠름".equals(priority)
                || "보통".equals(priority)
                || "느림".equals(priority);
    }

    private Long resolveCourseId(String courseId) {
        if (courseId == null || courseId.isBlank() || "PERSONAL".equals(courseId)) {
            return null;
        }
        return Long.valueOf(courseId);
    }
}
