package RunA2Do.todo.controller;

import RunA2Do.todo.security.AuthenticatedUsers;
import RunA2Do.todo.service.MyPageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/api/mypage/profile")
    public ResponseEntity<Map<String, Object>> profile(Authentication authentication) {
        String userId = AuthenticatedUsers.requireUserId(authentication);

        return myPageService.profile(userId)
                .map((profile) -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("success", true);
                    result.put("profile", profile);
                    return ResponseEntity.ok(result);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "사용자 정보를 찾을 수 없습니다."
                )));
    }

    @PutMapping("/api/mypage/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam String userName,
            @RequestParam String department,
            @RequestParam String idNumber,
            @RequestParam String grade,
            @RequestParam(required = false) String emailAddress,
            @RequestParam(required = false) String newPassword,
            Authentication authentication
    ) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        boolean updated = myPageService.updateProfile(
                userId,
                userName,
                department,
                idNumber,
                grade,
                emailAddress,
                newPassword
        );

        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "수정할 사용자 정보를 찾을 수 없습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "사용자 정보가 수정되었습니다."
        ));
    }

    @GetMapping("/api/mypage/schedule-summary")
    public Map<String, Object> scheduleSummary(Authentication authentication) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        return myPageService.scheduleSummary(userId);
    }

    @DeleteMapping("/api/mypage/profile")
    public ResponseEntity<Map<String, Object>> deleteProfile(
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        boolean deleted = myPageService.deleteProfile(userId);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "삭제할 사용자 정보를 찾을 수 없습니다."
            ));
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "계정이 삭제되었습니다."
        ));
    }
}
