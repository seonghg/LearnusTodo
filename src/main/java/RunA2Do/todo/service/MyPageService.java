package RunA2Do.todo.service;

import RunA2Do.todo.dto.ProfileDto;
import RunA2Do.todo.dto.ScheduleSummaryResponse;
import RunA2Do.todo.dto.UpdateProfileRequest;
import RunA2Do.todo.repository.CalendarRepository;
import RunA2Do.todo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public Optional<ProfileDto> profile(String userId) {
        List<ProfileDto> rows = userRepository.findProfile(userId);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(rows.get(0));
    }

    public boolean updateProfile(String userId, UpdateProfileRequest request) {
        String normalizedEmail = blankToNull(request.emailAddress());
        int updated;

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            updated = userRepository.updateProfileWithPassword(
                    userId,
                    request.userName(),
                    request.department(),
                    request.idNumber(),
                    request.grade(),
                    normalizedEmail,
                    passwordEncoder.encode(request.newPassword())
            );
        } else {
            updated = userRepository.updateProfile(
                    userId,
                    request.userName(),
                    request.department(),
                    request.idNumber(),
                    request.grade(),
                    normalizedEmail
            );
        }

        return updated > 0;
    }

    public ScheduleSummaryResponse scheduleSummary(String userId) {
        return new ScheduleSummaryResponse(
                calendarRepository.countEvents(userId),
                calendarRepository.countTodayEvents(userId),
                calendarRepository.countUpcomingEvents(userId),
                calendarRepository.findNextEvents(userId, 5)
        );
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
