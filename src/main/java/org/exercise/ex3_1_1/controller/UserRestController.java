package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.RoleService;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final ServiceProv userService;
    private final RoleService roleService;
    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    public UserRestController(ServiceProv userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.index();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return new ResponseEntity<>("Username '" + user.getUsername() + "' already exists.", HttpStatus.CONFLICT);
        }

        try {
            userService.addUser(user);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User userDetails) {
        if (userDetails.getId() == 0) {
            userDetails.setId(id);
        } else if (!(userDetails.getId() == id)) {
            return new ResponseEntity<>("ID in path does not match ID in request body", HttpStatus.BAD_REQUEST);
        }

        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Optional<User> userWithSameUsername = userService.findByUsername(userDetails.getUsername());
        if (userWithSameUsername.isPresent() && !(userWithSameUsername.get().getId() == id)) {
            return new ResponseEntity<>("Username '" + userDetails.getUsername() + "' already exists.", HttpStatus.CONFLICT);
        }

        try {
            userService.updateUser(userDetails);

            User updatedUser = userService.getUserById(userDetails.getId());
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Error updating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            User userToDelete = userService.getUserById(id);
            if (userToDelete == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.findAll();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    // Endpoint for any authenticated user (ADMIN or USER) to get their own details
    // GET /api/users/me
    @GetMapping("/me") // Endpoint: GET /api/users/me
    public ResponseEntity<User> getAuthenticatedUser(@AuthenticationPrincipal User currentUser) { // Renamed method for clarity
        logger.info("Attempting to fetch authenticated user details for /api/users/me"); // Log entry

        if (currentUser == null) {
            logger.warn("Authenticated user principal is null for /api/users/me"); // Log if principal is null
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Should not happen if secured
        }

        logger.info("Authenticated user found: {}", currentUser.getUsername()); // Log the username

        try {
            // IMPORTANT: Ensure password is not sent back
            // Create a copy or nullify the password on the returned object
            // Assuming User is a mutable object and nullifying password is safe for this response
            currentUser.setPassword(null); // Always nullify password before sending to frontend

            logger.debug("Password set to null for user: {}", currentUser.getUsername()); // Log password nullification

            // Attempt to return the user object in the response
            ResponseEntity<User> response = ResponseEntity.ok(currentUser);
            logger.info("Successfully prepared response for user: {}", currentUser.getUsername()); // Log success

            return response;

        } catch (Exception e) {
            // Catch any exceptions during processing (e.g., serialization)
            logger.error("Error processing /api/users/me for user {}: {}", currentUser.getUsername(), e.getMessage(), e); // Log the error with stack trace
            // Return an Internal Server Error (500) if there's a processing issue
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or return an error object/message if preferred
        }
    }

}