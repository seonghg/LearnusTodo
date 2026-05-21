package RunA2Do.todo.dto;

import java.util.List;

public record DashboardResponse(
        String userId,
        int courseCount,
        int eventCount,
        List<CourseDto> courses,
        List<CalendarEventDto> events
) {
}
