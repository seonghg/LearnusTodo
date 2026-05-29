package RunA2Do.todo.controller;

import RunA2Do.todo.dto.CalendarEventDto;
import RunA2Do.todo.dto.CourseDto;
import RunA2Do.todo.dto.DashboardResponse;
import RunA2Do.todo.security.AuthenticatedUsers;
import RunA2Do.todo.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/courses")
    public List<CourseDto> courses(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = AuthenticatedUsers.resolveUserId(userId, authentication);
        return dashboardService.courses(resolvedUserId);
    }

    @GetMapping("/api/calendar")
    public List<CalendarEventDto> calendar(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = AuthenticatedUsers.resolveUserId(userId, authentication);
        return dashboardService.calendar(resolvedUserId);
    }

    @GetMapping("/api/dashboard")
    public DashboardResponse dashboard(
            @RequestParam(required = false) String userId,
            Authentication authentication
    ) {
        String resolvedUserId = AuthenticatedUsers.resolveUserId(userId, authentication);
        return dashboardService.dashboard(resolvedUserId);
    }

}
