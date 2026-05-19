package RunA2Do.todo.controller;

import RunA2Do.todo.service.AuthService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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
        try {
            authService.register(userName, department, idNumber, grade, emailAddress, userId, password);
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
