package RunA2Do.todo.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findProfile(String userId) {
        return jdbcTemplate.queryForList(
                """
                SELECT
                    user_id,
                    user_name,
                    department,
                    id_number,
                    grade,
                    email_address,
                    create_time,
                    enabled
                FROM users
                WHERE user_id = ?
                """,
                userId
        );
    }

    public int updateProfile(
            String userId,
            String userName,
            String department,
            String idNumber,
            String grade,
            String emailAddress
    ) {
        return jdbcTemplate.update(
                """
                UPDATE users
                SET
                    user_name = ?,
                    department = ?,
                    id_number = ?,
                    grade = ?,
                    email_address = ?
                WHERE user_id = ?
                """,
                userName,
                department,
                idNumber,
                grade,
                emailAddress,
                userId
        );
    }

    public int updateProfileWithPassword(
            String userId,
            String userName,
            String department,
            String idNumber,
            String grade,
            String emailAddress,
            String encodedPassword
    ) {
        return jdbcTemplate.update(
                """
                UPDATE users
                SET
                    user_name = ?,
                    department = ?,
                    id_number = ?,
                    grade = ?,
                    email_address = ?,
                    user_password = ?
                WHERE user_id = ?
                """,
                userName,
                department,
                idNumber,
                grade,
                emailAddress,
                encodedPassword,
                userId
        );
    }

    public int insertUser(
            String userName,
            String department,
            String idNumber,
            String grade,
            String emailAddress,
            String userId,
            String encodedPassword
    ) {
        return jdbcTemplate.update(
                """
                INSERT INTO users (
                    user_name,
                    department,
                    id_number,
                    grade,
                    email_address,
                    user_id,
                    user_password,
                    enabled
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
                """,
                userName,
                department,
                idNumber,
                grade,
                emailAddress,
                userId,
                encodedPassword
        );
    }

    public void insertAuthority(String userId, String authority) {
        jdbcTemplate.update(
                "INSERT INTO user_authorities (user_id, authority) VALUES (?, ?)",
                userId,
                authority
        );
    }

    public void deleteAuthorities(String userId) {
        jdbcTemplate.update("DELETE FROM user_authorities WHERE user_id = ?", userId);
    }

    public void deleteCourseLinks(String userId) {
        jdbcTemplate.update("DELETE FROM user_courses WHERE user_id = ?", userId);
    }

    public int deleteUser(String userId) {
        return jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", userId);
    }
}
