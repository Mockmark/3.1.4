package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api") // Or your API base path
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User loginUser) {
        try {
            // Authenticate the user using the provided credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword())
            );

            // Set the authenticated Authentication object in the SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get the authenticated user principal from the Authentication object
            User authenticatedUser = (User) authentication.getPrincipal();

            // Nullify password before returning to the frontend for security
            authenticatedUser.setPassword(null);

            // Return the authenticated user object (including roles) in the response body with HttpStatus.OK
            return ResponseEntity.ok(authenticatedUser);

        } catch (Exception e) {
            // Log the authentication failure on the server side for debugging
            e.printStackTrace(); // Or use a proper logger (e.g., LoggerFactory.getLogger(AuthController.class))
            // Return an Unauthorized status and a simple error message in the response body
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
}
    