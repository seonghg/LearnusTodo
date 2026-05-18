package RunA2Do.todo.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestParam String userName,
            @RequestParam String department,
            @RequestParam String idNumber,
            @RequestParam String grade,
            @RequestParam(required = false) String emailAddress,
            @RequestParam String userId,
            @RequestParam String password
    ) {
        String encodedPassword = passwordEncoder.encode(password);

        try {
            jdbcTemplate.update(
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

            jdbcTemplate.update(
                    "INSERT INTO user_authorities (user_id, authority) VALUES (?, ?)",
                    userId,
                    "ROLE_USER"
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원가입이 완료되었습니다.",
                    "userId", userId
            ));

        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false,
                    "message", "이미 존재하는 사용자입니다.",
                    "userId", userId
            ));
        }
    }
}
