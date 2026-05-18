package RunA2Do.todo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyPageController {

    @GetMapping("/mypage")
    public String mypage(Authentication authentication) {
        return "로그인 성공. 현재 사용자: " + authentication.getName();
    }
}