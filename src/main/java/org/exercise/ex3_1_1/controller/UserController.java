package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // <--- Changed to RestController

// @Controller // <--- Remove or comment out this annotation

@RestController // <--- Add this annotation
@RequestMapping("/api/admin") // <--- Consider changing the request mapping to '/api/admin' or a different API prefix
public class UserController {

    // Remove the redirectToIndex method as the backend won't handle the root '/admin' HTML route

    // Remove the mainPage method as the backend won't serve the 'main-page' HTML view

    // Create a new endpoint to provide the authenticated user data as JSON
    @GetMapping("/current-user") // <--- New API endpoint for current user data
    public User getCurrentUser(@AuthenticationPrincipal User currentUser) {
        // The @AuthenticationPrincipal annotation directly injects the authenticated user principal
        return currentUser; // Spring will automatically convert the User object to JSON
    }

    // You might move other user-related API endpoints here if they were in other controllers,
    // or keep this controller focused just on administrative user operations and current user data.

    /*
     * Example of where your existing API endpoints for user CRUD might go (if they were in a different controller)
     *
     * @GetMapping("/users")
     * public List<User> getAllUsers() { ... }
     *
     * @GetMapping("/users/{id}")
     * public User getUserById(@PathVariable Long id) { ... }
     *
     * @PostMapping("/users")
     * public ResponseEntity<User> createUser(@RequestBody User user) { ... }
     *
     * @PutMapping("/users/{id}")
     * public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) { ... }
     *
     * @DeleteMapping("/users/{id}")
     * public ResponseEntity<Void> deleteUser(@PathVariable Long id) { ... }
     */
}