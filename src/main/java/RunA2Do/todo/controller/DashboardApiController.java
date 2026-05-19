package RunA2Do.todo.controller;

import RunA2Do.todo.security.AuthenticatedUsers;
import RunA2Do.todo.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/courses")
    public List<Map<String, Object>> courses(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = AuthenticatedUsers.resolveUserId(userId, authentication);
        return dashboardService.courses(resolvedUserId);
    }

    @GetMapping("/api/calendar")
    public List<Map<String, Object>> calendar(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = AuthenticatedUsers.resolveUserId(userId, authentication);
        return dashboardService.calendar(resolvedUserId);
    }

    @GetMapping("/api/dashboard")
    public Map<String, Object> dashboard(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = AuthenticatedUsers.resolveUserId(userId, authentication);
        return dashboardService.dashboard(resolvedUserId);
    }

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", authentication == null ? null : authentication.getName());
        return result;
    }
}
