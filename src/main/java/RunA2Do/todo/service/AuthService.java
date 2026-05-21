package RunA2Do.todo.service;

import RunA2Do.todo.dto.RegisterRequest;
import RunA2Do.todo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_AUTHORITY = "ROLE_USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        userRepository.insertUser(
                request.userName(),
                request.department(),
                request.idNumber(),
                request.grade(),
                request.emailAddress(),
                request.userId(),
                passwordEncoder.encode(request.password())
        );
        userRepository.insertAuthority(request.userId(), DEFAULT_AUTHORITY);
    }
}
