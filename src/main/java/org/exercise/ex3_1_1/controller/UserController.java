package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
// Add imports for repositories if your other methods use them
// import org.exercise.ex3_1_1.repository.DataAccess;
// import org.exercise.ex3_1_1.repository.RoleRepository;
// import org.springframework.beans.factory.annotation.Autowired; // Keep if you use @Autowired elsewhere
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin") // This controller serves admin-related API endpoints
public class UserController {

    // If you have other methods for /api/admin/** (like managing users), keep them here
    // Example:
    // @Autowired
    // private DataAccess userRepository;
    // @Autowired
    // private RoleRepository roleRepository;
    // @GetMapping("/users") // Example: GET /api/admin/users (list all users for admin)
    // // ... method body ...


    // Removed the duplicate @GetMapping("/current-user") method.
    // This endpoint is now the single one for authenticated users to get their own details.
    @GetMapping("/me") // Endpoint: GET /api/admin/me
    public ResponseEntity<User> getAuthenticatedUser(@AuthenticationPrincipal User currentUser) { // Renamed the method to avoid conflict (optional but good practice)
        if (currentUser == null) {
            // This case should ideally not be reached if security is configured correctly
            // to protect this endpoint, but it's a good safety check.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Should not happen if secured
        }

        // IMPORTANT: Ensure password is not sent back
        // Create a copy or nullify the password on the returned object
        // Assuming User is a mutable object and nullifying password is safe for this response
        currentUser.setPassword(null); // Always nullify password before sending to frontend

        return ResponseEntity.ok(currentUser);
    }
}
