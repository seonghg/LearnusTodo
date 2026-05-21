package RunA2Do.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record CourseDto(
        @JsonProperty("course_id")
        Long courseId,
        @JsonProperty("external_course_id")
        String externalCourseId,
        @JsonProperty("course_code")
        String courseCode,
        @JsonProperty("course_name")
        String courseName,
        @JsonProperty("professor_name")
        String professorName,
        @JsonProperty("semester_name")
        String semesterName,
        @JsonProperty("course_type")
        String courseType,
        @JsonProperty("course_level")
        String courseLevel,
        @JsonProperty("course_url")
        String courseUrl,
        @JsonProperty("learning_rate")
        Double learningRate,
        @JsonProperty("completed_count")
        Integer completedCount,
        @JsonProperty("total_count")
        Integer totalCount,
        @JsonProperty("attendance_url")
        String attendanceUrl,
        @JsonProperty("is_new")
        Boolean isNew,
        @JsonProperty("synced_at")
        OffsetDateTime syncedAt
) {
}
