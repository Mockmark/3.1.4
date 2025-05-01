package org.exercise.ex3_1_1.controller; // Or your preferred package, e.g., org.exercise.ex3_1_1.api

import org.exercise.ex3_1_1.model.Role; // Import Role
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.RoleService; // Import RoleService
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional; // Still useful for checks, though service might return User directly

@RestController
@RequestMapping("/api/users") // Base path for user related endpoints
public class UserRestController {

    private final ServiceProv userService;
    private final RoleService roleService; // Inject RoleService

    public UserRestController(ServiceProv userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // Endpoint to get all users
    // Accessible via GET /api/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.index();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Endpoint to get a user by ID
    // Accessible via GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id); // Assuming service returns User or throws/returns null

        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            // Return 404 Not Found if user doesn't exist
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to create a new user
    // Accessible via POST /api/users
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) { // Using <?> for potential different return types (User or Error)
        // Check for username existence before attempting to save
        if (userService.existsByUsername(user.getUsername())) {
            // Return 409 Conflict if username already exists
            // You could return a custom error object/message here instead of just a string
            return new ResponseEntity<>("Username '" + user.getUsername() + "' already exists.", HttpStatus.CONFLICT);
        }

        try {
            // Assuming userService.addUser handles role rehydration and password encoding internally
            userService.addUser(user);
            // Return the created user with 201 Created status
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (Exception e) {
            // Handle other potential errors during save
            return new ResponseEntity<>("Error creating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to update an existing user
    // Accessible via PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User userDetails) {
        // Ensure the ID in the path matches the ID in the request body (optional but good practice)
        if (userDetails.getId() == 0) { // Simple check if ID was not set in body
            userDetails.setId(id);
        } else if (!(userDetails.getId() == id)) {
            // If IDs don't match, return a Bad Request error
            return new ResponseEntity<>("ID in path does not match ID in request body", HttpStatus.BAD_REQUEST);
        }

        // Check if the user to update exists
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if user doesn't exist
        }

        // Check for username uniqueness conflict if username is being changed
        Optional<User> userWithSameUsername = userService.findByUsername(userDetails.getUsername());
        if (userWithSameUsername.isPresent() && !(userWithSameUsername.get().getId() == id)) {
            // Found another user with the same username and a different ID
            return new ResponseEntity<>("Username '" + userDetails.getUsername() + "' already exists.", HttpStatus.CONFLICT);
        }

        try {
            // Assuming userService.updateUser fetches the existing user,
            // handles property updates, role rehydration, and password logic internally based on userDetails
            // We pass userDetails which contains the updated info, including ID
            userService.updateUser(userDetails);

            // Return the updated user with 200 OK status
            // Fetch the updated user again to ensure roles are eagerly loaded for the response
            User updatedUser = userService.getUserById(userDetails.getId()); // Fetch again for response JSON
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (Exception e) {
            // Handle other potential errors during update
            return new ResponseEntity<>("Error updating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Endpoint to delete a user by ID
    // Accessible via DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) { // Using <?> for potential different return types
        try {
            // You might want to check if the user exists first and return 404 if not
            User userToDelete = userService.getUserById(id); // Check existence
            if (userToDelete == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Return 404 if user doesn't exist
            }

            userService.deleteUser(id); // Assuming deleteUser takes ID
            // Return 204 No Content on successful deletion (conventionally, DELETE returns 204 or 200 with body)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            // Handle potential errors during deletion (e.g., constraints)
            return new ResponseEntity<>("Error deleting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Endpoint to get all roles (useful for frontend forms)
    // Accessible via GET /api/roles
    @GetMapping("/roles") // Map roles endpoint relative to base, or use a new controller
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.findAll();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    // Add more endpoints for roles if needed (e.g., POST, PUT, DELETE roles)
}