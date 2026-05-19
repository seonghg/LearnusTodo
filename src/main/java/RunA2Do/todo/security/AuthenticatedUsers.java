package RunA2Do.todo.security;

import org.springframework.security.core.Authentication;

public final class AuthenticatedUsers {

    private AuthenticatedUsers() {
    }

    public static String requireUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required.");
        }
        return authentication.getName();
    }

    public static String resolveUserId(String requestedUserId, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return requestedUserId;
    }
}
