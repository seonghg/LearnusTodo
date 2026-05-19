package RunA2Do.todo.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DashboardApiController {

    private final JdbcTemplate jdbcTemplate;

    public DashboardApiController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/courses")
    public List<Map<String, Object>> courses(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = resolveUserId(userId, authentication);

        return jdbcTemplate.queryForList(
                """
                SELECT
                    c.course_id,
                    c.external_course_id,
                    c.course_code,
                    c.course_name,
                    c.professor_name,
                    c.semester_name,
                    c.course_type,
                    c.course_level,
                    c.course_url,
                    uc.learning_rate,
                    uc.completed_count,
                    uc.total_count,
                    uc.attendance_url,
                    uc.is_new,
                    uc.synced_at
                FROM user_courses uc
                JOIN courses c ON c.course_id = uc.course_id
                WHERE uc.user_id = ?
                  AND uc.is_active = TRUE
                ORDER BY c.course_name
                """,
                resolvedUserId
        );
    }

    @GetMapping("/api/calendar")
    public List<Map<String, Object>> calendar(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = resolveUserId(userId, authentication);

        return jdbcTemplate.queryForList(
                """
                SELECT
                    cal.event_id,
                    cal.title,
                    cal.description,
                    cal.start_time,
                    cal.end_time,
                    cal.is_all_day,
                    cal.location,
                    cal.source_type,
                    cal.external_event_id,
                    c.course_name,
                    c.course_code
                FROM calendar cal
                LEFT JOIN courses c ON c.course_id = cal.course_id
                WHERE cal.user_id = ?
                ORDER BY cal.start_time ASC, cal.title ASC
                """,
                resolvedUserId
        );
    }

    @GetMapping("/api/dashboard")
    public Map<String, Object> dashboard(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = resolveUserId(userId, authentication);
        List<Map<String, Object>> courses = courses(resolvedUserId, authentication);
        List<Map<String, Object>> events = calendar(resolvedUserId, authentication);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", resolvedUserId);
        result.put("courseCount", courses.size());
        result.put("eventCount", events.size());
        result.put("courses", courses);
        result.put("events", events);

        return result;
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", authentication == null ? null : authentication.getName());
        return result;
    }

    private String resolveUserId(String requestedUserId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return requestedUserId;
    }
}
