package RunA2Do.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record CalendarEventDto(
        @JsonProperty("event_id")
        Long eventId,
        String title,
        String description,
        @JsonProperty("start_time")
        OffsetDateTime startTime,
        @JsonProperty("end_time")
        OffsetDateTime endTime,
        @JsonProperty("is_all_day")
        Boolean isAllDay,
        String location,
        @JsonProperty("source_type")
        String sourceType,
        @JsonProperty("external_event_id")
        String externalEventId,
        @JsonProperty("course_name")
        String courseName,
        @JsonProperty("course_code")
        String courseCode
) {
}
