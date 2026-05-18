package RunA2Do.todo.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String register(
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
                        user_password
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
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

            return "회원가입 성공: " + userId;

        } catch (DuplicateKeyException e) {
            return "이미 존재하는 사용자입니다: " + userId;
        }
    }
}