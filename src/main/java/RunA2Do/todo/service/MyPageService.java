package RunA2Do.todo.service;

import RunA2Do.todo.repository.CalendarRepository;
import RunA2Do.todo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final CalendarRepository calendarRepository;
    private final PasswordEncoder passwordEncoder;

    public MyPageService(
            UserRepository userRepository,
            CalendarRepository calendarRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.calendarRepository = calendarRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Map<String, Object>> profile(String userId) {
        List<Map<String, Object>> rows = userRepository.findProfile(userId);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public boolean updateProfile(
            String userId,
            String userName,
            String department,
            String idNumber,
            String grade,
            String emailAddress,
            String newPassword
    ) {
        String normalizedEmail = blankToNull(emailAddress);
        int updated;

        if (newPassword != null && !newPassword.isBlank()) {
            updated = userRepository.updateProfileWithPassword(
                    userId,
                    userName,
                    department,
                    idNumber,
                    grade,
                    normalizedEmail,
                    passwordEncoder.encode(newPassword)
            );
        } else {
            updated = userRepository.updateProfile(
                    userId,
                    userName,
                    department,
                    idNumber,
                    grade,
                    normalizedEmail
            );
        }

        return updated > 0;
    }

    public Map<String, Object> scheduleSummary(String userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEvents", calendarRepository.countEvents(userId));
        result.put("todayEvents", calendarRepository.countTodayEvents(userId));
        result.put("upcomingEvents", calendarRepository.countUpcomingEvents(userId));
        result.put("nextEvents", calendarRepository.findNextEvents(userId, 5));
        return result;
    }

    @Transactional
    public boolean deleteProfile(String userId) {
        userRepository.deleteAuthorities(userId);
        calendarRepository.deleteEventsByUserId(userId);
        userRepository.deleteCourseLinks(userId);
        return userRepository.deleteUser(userId) > 0;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
