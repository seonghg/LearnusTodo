package RunA2Do.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record ProfileDto(
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("user_name")
        String userName,
        String department,
        @JsonProperty("id_number")
        String idNumber,
        String grade,
        @JsonProperty("email_address")
        String emailAddress,
        @JsonProperty("create_time")
        OffsetDateTime createTime,
        Boolean enabled
) {
}
