package RunA2Do.todo.controller;

import RunA2Do.todo.dto.ApiResponse;
import RunA2Do.todo.dto.ProfileResponse;
import RunA2Do.todo.dto.ScheduleSummaryResponse;
import RunA2Do.todo.dto.UpdateProfileRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @GetMapping("/api/mypage/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        String userId = AuthenticatedUsers.requireUserId(authentication);

        return myPageService.profile(userId)
                .<ResponseEntity<?>>map((profile) -> ResponseEntity.ok(new ProfileResponse(true, profile)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.fail("사용자 정보를 찾을 수 없습니다.")
                ));
    }

    @PutMapping("/api/mypage/profile")
    public ResponseEntity<ApiResponse> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        boolean updated = myPageService.updateProfile(userId, request);

        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.fail("수정할 사용자 정보를 찾을 수 없습니다.")
            );
        }

        return ResponseEntity.ok(ApiResponse.ok("사용자 정보가 수정되었습니다."));
    }

    @GetMapping("/api/mypage/schedule-summary")
    public ScheduleSummaryResponse scheduleSummary(Authentication authentication) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        return myPageService.scheduleSummary(userId);
    }

    @DeleteMapping("/api/mypage/profile")
    public ResponseEntity<ApiResponse> deleteProfile(
            Authentication authentication,
            HttpServletRequest request
    ) {
        String userId = AuthenticatedUsers.requireUserId(authentication);
        boolean deleted = myPageService.deleteProfile(userId);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.fail("삭제할 사용자 정보를 찾을 수 없습니다.")
            );
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return ResponseEntity.ok(ApiResponse.ok("계정이 삭제되었습니다."));
    }
}
