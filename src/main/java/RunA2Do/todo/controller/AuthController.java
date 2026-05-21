package RunA2Do.todo.controller;

import RunA2Do.todo.dto.RegisterRequest;
import RunA2Do.todo.dto.RegisterResponse;
import RunA2Do.todo.service.AuthService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            return ResponseEntity.ok(new RegisterResponse(
                    true,
                    "회원가입이 완료되었습니다.",
                    request.userId()
            ));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new RegisterResponse(
                    false,
                    "이미 존재하는 사용자입니다.",
                    request.userId()
            ));
        }
    }
}
