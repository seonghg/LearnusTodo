package RunA2Do.todo.service;

import RunA2Do.todo.repository.CalendarRepository;
import RunA2Do.todo.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final CourseRepository courseRepository;
    private final CalendarRepository calendarRepository;

    public DashboardService(CourseRepository courseRepository, CalendarRepository calendarRepository) {
        this.courseRepository = courseRepository;
        this.calendarRepository = calendarRepository;
    }

    public List<Map<String, Object>> courses(String userId) {
        return courseRepository.findActiveCoursesByUserId(userId);
    }

    public List<Map<String, Object>> calendar(String userId) {
        return calendarRepository.findEventsByUserId(userId);
    }

    public Map<String, Object> dashboard(String userId) {
        List<Map<String, Object>> courses = courses(userId);
        List<Map<String, Object>> events = calendar(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("courseCount", courses.size());
        result.put("eventCount", events.size());
        result.put("courses", courses);
        result.put("events", events);
        return result;
    }
}
