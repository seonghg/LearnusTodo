package RunA2Do.todo.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

final class JdbcColumns {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private JdbcColumns() {
    }

    static Long nullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    static Integer nullableInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    static Double nullableDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    static Boolean nullableBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    static OffsetDateTime nullableOffsetDateTime(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);

        if (value == null) {
            return null;
        }

        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant().atZone(KOREA_ZONE).toOffsetDateTime();
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.atZone(KOREA_ZONE).toOffsetDateTime();
        }

        return OffsetDateTime.parse(String.valueOf(value));
    }
}
