// src/main/java/org/exercise/ex3_1_1/controller/AuthController.java
package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.dto.LoginRequest; // Import the DTO
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController // Mark this class as a REST controller
public class AuthController {

    // Inject the AuthenticationManager bean provided by Spring Security
    private final AuthenticationManager authenticationManager;

    // Constructor for dependency injection
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/api/login") // <--- This maps POST requests to /api/login
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // 1. Create an Authentication object with the provided credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken( // Uses the standard token for username/password
                            loginRequest.getUsername(), // Get username from the request body DTO
                            loginRequest.getPassword()    // Get password from the request body DTO
                    )
            );

            // 2. If authentication is successful, set the Authentication object in the SecurityContextHolder
            // This establishes the security context for the current request/session
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Return a success response
            // For a simple API, you can just return a 200 OK response.
            // If you were using tokens (like JWT), you would generate and return the token here.
            return ResponseEntity.ok("User authenticated successfully"); // You could return user details or a token instead

        } catch (Exception e) {
            // 4. If authentication fails (e.g., incorrect username/password)
            // Return an Unauthorized status (401)
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }

    // You might also add an API endpoint for logout if needed, e.g., a POST to /logout
    // Spring Security's default LogoutFilter handles /logout by clearing the security context/session.
    // If you need a custom API logout response, you could define a @PostMapping("/api/logout")
    // and potentially trigger the logout programmatically, though often the default /logout endpoint is sufficient for APIs relying on sessions.
}