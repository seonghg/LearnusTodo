package RunA2Do.todo.service;

import RunA2Do.todo.dto.TodoRequest;
import RunA2Do.todo.repository.CalendarRepository;
import RunA2Do.todo.repository.CourseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class TodoService {

    private static final String SOURCE_TYPE = "USER_TODO";
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final CourseRepository courseRepository;
    private final CalendarRepository calendarRepository;

    public TodoService(CourseRepository courseRepository, CalendarRepository calendarRepository) {
        this.courseRepository = courseRepository;
        this.calendarRepository = calendarRepository;
    }

    public Long createTodo(String userId, TodoRequest request) {
        if (!isValidPriority(request.priority())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority.");
        }

        Long resolvedCourseId = resolveCourseId(request.courseId());
        if (resolvedCourseId != null && !courseRepository.existsActiveCourseForUser(userId, resolvedCourseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Course is not available for this user.");
        }

        OffsetDateTime startTime = LocalDateTime.parse(request.dueDate()).atZone(KOREA_ZONE).toOffsetDateTime();
        String externalEventId = "todo-" + UUID.randomUUID();
        String description = "priority=" + request.priority()
                + (resolvedCourseId == null ? ";category=개인 일정" : "");

        return calendarRepository.insertTodoEvent(
                userId,
                request.title(),
                description,
                startTime,
                resolvedCourseId,
                SOURCE_TYPE,
                externalEventId
        );
    }

    public boolean deleteTodo(String userId, Long eventId) {
        return calendarRepository.deleteTodoEvent(userId, eventId, SOURCE_TYPE) > 0;
    }

    private boolean isValidPriority(String priority) {
        return priority != null && !priority.isBlank();
    }

    private Long resolveCourseId(String courseId) {
        if (courseId == null || courseId.isBlank() || "PERSONAL".equals(courseId)) {
            return null;
        }
        return Long.valueOf(courseId);
    }
}
