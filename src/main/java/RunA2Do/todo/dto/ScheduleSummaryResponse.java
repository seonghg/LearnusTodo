package RunA2Do.todo.dto;

import java.util.List;

public record ScheduleSummaryResponse(
        Integer totalEvents,
        Integer todayEvents,
        Integer upcomingEvents,
        List<CalendarEventDto> nextEvents
) {
}
