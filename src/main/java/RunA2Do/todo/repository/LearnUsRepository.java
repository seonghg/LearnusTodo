package RunA2Do.todo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class LearnUsRepository {

    private final JdbcTemplate jdbcTemplate;

    public LearnUsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long upsertCourse(
            String sourceType,
            String externalCourseId,
            String courseCode,
            String courseName,
            String professorName,
            String semesterName,
            String courseType,
            String courseLevel,
            String courseUrl
    ) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO courses (
                    external_course_id,
                    course_code,
                    course_name,
                    professor_name,
                    semester_name,
                    course_type,
                    course_level,
                    course_url,
                    source_type,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                ON CONFLICT (source_type, external_course_id)
                DO UPDATE SET
                    course_code = EXCLUDED.course_code,
                    course_name = EXCLUDED.course_name,
                    professor_name = EXCLUDED.professor_name,
                    semester_name = EXCLUDED.semester_name,
                    course_type = EXCLUDED.course_type,
                    course_level = EXCLUDED.course_level,
                    course_url = EXCLUDED.course_url,
                    updated_at = now()
                RETURNING course_id
                """,
                Long.class,
                externalCourseId,
                courseCode,
                courseName,
                professorName,
                semesterName,
                courseType,
                courseLevel,
                courseUrl,
                sourceType
        );
    }

    public void upsertUserCourse(
            String sourceType,
            String userId,
            Long courseId,
            Double learningRate,
            Integer completedCount,
            Integer totalCount,
            String attendanceUrl,
            boolean isNew
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO user_courses (
                    user_id,
                    course_id,
                    learning_rate,
                    completed_count,
                    total_count,
                    attendance_url,
                    is_new,
                    is_active,
                    source_type,
                    synced_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?, now(), now())
                ON CONFLICT (user_id, course_id)
                DO UPDATE SET
                    learning_rate = EXCLUDED.learning_rate,
                    completed_count = EXCLUDED.completed_count,
                    total_count = EXCLUDED.total_count,
                    attendance_url = EXCLUDED.attendance_url,
                    is_new = EXCLUDED.is_new,
                    is_active = TRUE,
                    source_type = EXCLUDED.source_type,
                    synced_at = now(),
                    updated_at = now()
                """,
                userId,
                courseId,
                learningRate,
                completedCount,
                totalCount,
                attendanceUrl,
                isNew,
                sourceType
        );
    }

    public Long findCourseIdByCode(String sourceType, String userId, String courseCode) {
        List<Long> result = jdbcTemplate.queryForList(
                """
                SELECT c.course_id
                FROM courses c
                JOIN user_courses uc ON uc.course_id = c.course_id
                WHERE uc.user_id = ?
                  AND c.source_type = ?
                  AND c.course_code = ?
                LIMIT 1
                """,
                Long.class,
                userId,
                sourceType,
                courseCode
        );

        return result.isEmpty() ? null : result.get(0);
    }

    public void upsertCalendarEvent(
            String sourceType,
            String userId,
            String title,
            String description,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            boolean isAllDay,
            String location,
            Long courseId,
            String externalEventId
    ) {
        jdbcTemplate.update(
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
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                ON CONFLICT (user_id, source_type, external_event_id)
                DO UPDATE SET
                    title = EXCLUDED.title,
                    description = EXCLUDED.description,
                    start_time = EXCLUDED.start_time,
                    end_time = EXCLUDED.end_time,
                    is_all_day = EXCLUDED.is_all_day,
                    location = EXCLUDED.location,
                    course_id = EXCLUDED.course_id,
                    updated_at = now()
                """,
                userId,
                title,
                description,
                startTime,
                endTime,
                isAllDay,
                location,
                courseId,
                sourceType,
                externalEventId
        );
    }
}
