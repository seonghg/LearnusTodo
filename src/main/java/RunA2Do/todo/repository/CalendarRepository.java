package RunA2Do.todo.repository;

import RunA2Do.todo.dto.CalendarEventDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

import static RunA2Do.todo.repository.JdbcColumns.nullableBoolean;
import static RunA2Do.todo.repository.JdbcColumns.nullableLong;
import static RunA2Do.todo.repository.JdbcColumns.nullableOffsetDateTime;

@Repository
public class CalendarRepository {

    private final JdbcTemplate jdbcTemplate;

    public CalendarRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CalendarEventDto> findEventsByUserId(String userId) {
        return jdbcTemplate.query(
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
                (rs, rowNum) -> new CalendarEventDto(
                        nullableLong(rs, "event_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        nullableOffsetDateTime(rs, "start_time"),
                        nullableOffsetDateTime(rs, "end_time"),
                        nullableBoolean(rs, "is_all_day"),
                        rs.getString("location"),
                        rs.getString("source_type"),
                        rs.getString("external_event_id"),
                        rs.getString("course_name"),
                        rs.getString("course_code")
                ),
                userId
        );
    }

    public Long insertTodoEvent(
            String userId,
            String title,
            String description,
            OffsetDateTime startTime,
            Long courseId,
            String sourceType,
            String externalEventId
    ) {
        return jdbcTemplate.queryForObject(
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
                courseId,
                sourceType,
                externalEventId
        );
    }

    public Integer countEvents(String userId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendar WHERE user_id = ?",
                Integer.class,
                userId
        );
    }

    public Integer countTodayEvents(String userId) {
        return jdbcTemplate.queryForObject(
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
    }

    public Integer countUpcomingEvents(String userId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM calendar
                WHERE user_id = ?
                  AND start_time >= now()
                """,
                Integer.class,
                userId
        );
    }

    public List<CalendarEventDto> findNextEvents(String userId, int limit) {
        return jdbcTemplate.query(
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
                  AND cal.start_time >= now()
                ORDER BY cal.start_time ASC
                LIMIT ?
                """,
                (rs, rowNum) -> new CalendarEventDto(
                        nullableLong(rs, "event_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        nullableOffsetDateTime(rs, "start_time"),
                        nullableOffsetDateTime(rs, "end_time"),
                        nullableBoolean(rs, "is_all_day"),
                        rs.getString("location"),
                        rs.getString("source_type"),
                        rs.getString("external_event_id"),
                        rs.getString("course_name"),
                        rs.getString("course_code")
                ),
                userId,
                limit
        );
    }

    public void deleteEventsByUserId(String userId) {
        jdbcTemplate.update("DELETE FROM calendar WHERE user_id = ?", userId);
    }

    public int deleteTodoEvent(String userId, Long eventId, String sourceType) {
        return jdbcTemplate.update(
                """
                DELETE FROM calendar
                WHERE user_id = ?
                  AND event_id = ?
                  AND source_type = ?
                """,
                userId,
                eventId,
                sourceType
        );
    }
}
