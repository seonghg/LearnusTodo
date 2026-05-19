package RunA2Do.todo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CourseRepository {

    private final JdbcTemplate jdbcTemplate;

    public CourseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findActiveCoursesByUserId(String userId) {
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
                userId
        );
    }

    public boolean existsActiveCourseForUser(String userId, Long courseId) {
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
                courseId
        );

        return courseCount != null && courseCount > 0;
    }
}
