package RunA2Do.todo.service;

import RunA2Do.todo.dto.CalendarEventDto;
import RunA2Do.todo.dto.CourseDto;
import RunA2Do.todo.dto.DashboardResponse;
import RunA2Do.todo.repository.CalendarRepository;
import RunA2Do.todo.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final CourseRepository courseRepository;
    private final CalendarRepository calendarRepository;

    public DashboardService(CourseRepository courseRepository, CalendarRepository calendarRepository) {
        this.courseRepository = courseRepository;
        this.calendarRepository = calendarRepository;
    }

    public List<CourseDto> courses(String userId) {
        return courseRepository.findActiveCoursesByUserId(userId);
    }

    public List<CalendarEventDto> calendar(String userId) {
        return calendarRepository.findEventsByUserId(userId);
    }

    public DashboardResponse dashboard(String userId) {
        List<CourseDto> courses = courses(userId);
        List<CalendarEventDto> events = calendar(userId);

        return new DashboardResponse(
                userId,
                courses.size(),
                events.size(),
                courses,
                events
        );
    }
}
