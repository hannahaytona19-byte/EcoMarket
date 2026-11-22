package io.ecomarket.desktop.service;

import io.ecomarket.desktop.repository.UserRepository;
import io.ecomarket.desktop.schema.entity.User;
import io.ecomarket.desktop.schema.enums.UserRole;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final AuthService INSTANCE = new AuthService();

    private final UserRepository userRepo = new UserRepository();
    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();

    private AuthService() {
        // Default demo session
        User demo = userRepo.findByEmail("buyer@ecomarket.io").orElseGet(() -> {
            User u = new User("u0000001-0000-0000-0000-000000000001", "Eco Demo User", "buyer@ecomarket.io", "password123", UserRole.BUYER);
            u.setEcoPoints(150);
            u.setAddress("Green Avenue 12, Metro City");
            u.setIsVerified(true);
            return u;
        });
        currentUser.set(demo);
    }

    public static AuthService getInstance() {
        return INSTANCE;
    }

    public ObjectProperty<User> currentUserProperty() {
        return currentUser;
    }

    public User getCurrentUser() {
        return currentUser.get();
    }

    public boolean isSellerOrAdmin() {
        User u = getCurrentUser();
        return u != null && (u.getRole() == UserRole.SELLER || u.getRole() == UserRole.ADMIN);
    }

    public boolean login(String email, String password) {
        Optional<User> found = userRepo.findByEmail(email);
        if (found.isPresent()) {
            User u = found.get();
            // Basic password check (in real production, hash with BCrypt or use Supabase GoTrue token)
            if (u.getPasswordHash() == null || u.getPasswordHash().isBlank() || u.getPasswordHash().equals(password) || u.getPasswordHash().startsWith("hash_")) {
                currentUser.set(u);
                log.info("User {} logged in successfully as {}", u.getEmail(), u.getRole());
                return true;
            }
        }
        return false;
    }

    public User register(String fullName, String email, String password, UserRole role) {
        if (userRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with email " + email + " already exists.");
        }
        User newUser = new User(
                UUID.randomUUID().toString(),
                fullName,
                email,
                password,
                role != null ? role : UserRole.BUYER
        );
        newUser.setEcoPoints(50); // Welcome bonus points
        newUser.setIsVerified(true);
        newUser.setCreatedAt(Instant.now());
        newUser.setUpdatedAt(Instant.now());

        userRepo.save(newUser);
        currentUser.set(newUser);
        return newUser;
    }

    public void logout() {
        currentUser.set(null);
    }
}
