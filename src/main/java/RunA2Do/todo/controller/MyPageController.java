package RunA2Do.todo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyPageController {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public MyPageController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/api/mypage/profile")
    public ResponseEntity<Map<String, Object>> profile(Authentication authentication) {
        String userId = requireUserId(authentication);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                SELECT
                    user_id,
                    user_name,
                    department,
                    id_number,
                    grade,
                    email_address,
                    create_time,
                    enabled
                FROM users
                WHERE user_id = ?
                """,
                userId
        );

        if (rows.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "사용자 정보를 찾을 수 없습니다."
            ));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("profile", rows.get(0));

        return ResponseEntity.ok(result);
    }

    @PutMapping("/api/mypage/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam String userName,
            @RequestParam String department,
            @RequestParam String idNumber,
            @RequestParam String grade,
            @RequestParam(required = false) String emailAddress,
            @RequestParam(required = false) String newPassword,
            Authentication authentication
    ) {
        String userId = requireUserId(authentication);

        int updated;
        if (newPassword != null && !newPassword.isBlank()) {
            updated = jdbcTemplate.update(
                    """
                    UPDATE users
                    SET
                        user_name = ?,
                        department = ?,
                        id_number = ?,
                        grade = ?,
                        email_address = ?,
                        user_password = ?
                    WHERE user_id = ?
                    """,
                    userName,
                    department,
                    idNumber,
                    grade,
                    blankToNull(emailAddress),
                    passwordEncoder.encode(newPassword),
                    userId
            );
        } else {
            updated = jdbcTemplate.update(
                    """
                    UPDATE users
                    SET
                        user_name = ?,
                        department = ?,
                        id_number = ?,
                        grade = ?,
                        email_address = ?
                    WHERE user_id = ?
                    """,
                    userName,
                    department,
                    idNumber,
                    grade,
                    blankToNull(emailAddress),
                    userId
            );
        }

        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "수정할 사용자 정보를 찾을 수 없습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자 정보가 수정되었습니다."
        ));
    }

    @GetMapping("/api/mypage/schedule-summary")
    public Map<String, Object> scheduleSummary(Authentication authentication) {
        String userId = requireUserId(authentication);

        Integer totalEvents = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendar WHERE user_id = ?",
                Integer.class,
                userId
        );

        Integer todayEvents = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM calendar
                WHERE user_id = ?
                  AND start_time >= CURRENT_DATE
                  AND start_time < CURRENT_DATE + INTERVAL '1 day'
                """,
                Integer.class,
                userId
        );

        Integer upcomingEvents = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM calendar
                WHERE user_id = ?
                  AND start_time >= now()
                """,
                Integer.class,
                userId
        );

        List<Map<String, Object>> nextEvents = jdbcTemplate.queryForList(
                """
                SELECT
                    cal.event_id,
                    cal.title,
                    cal.start_time,
                    cal.end_time,
                    cal.is_all_day,
                    c.course_name,
                    c.course_code
                FROM calendar cal
                LEFT JOIN courses c ON c.course_id = cal.course_id
                WHERE cal.user_id = ?
                  AND cal.start_time >= now()
                ORDER BY cal.start_time ASC
                LIMIT 5
                """,
                userId
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEvents", totalEvents);
        result.put("todayEvents", todayEvents);
        result.put("upcomingEvents", upcomingEvents);
        result.put("nextEvents", nextEvents);

        return result;
    }

    @Transactional
    @DeleteMapping("/api/mypage/profile")
    public ResponseEntity<Map<String, Object>> deleteProfile(
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = requireUserId(authentication);

        jdbcTemplate.update("DELETE FROM user_authorities WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM calendar WHERE user_id = ?", userId);
        jdbcTemplate.update("DELETE FROM user_courses WHERE user_id = ?", userId);
        int deleted = jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", userId);

        if (deleted == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "삭제할 사용자 정보를 찾을 수 없습니다."
            ));
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "계정이 삭제되었습니다."
        ));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required.");
        }
        return authentication.getName();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
