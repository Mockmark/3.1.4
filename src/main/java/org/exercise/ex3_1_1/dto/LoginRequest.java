// src/main/java/org/exercise/ex3_1_1/dto/LoginRequest.java
package org.exercise.ex3_1_1.dto; // Or wherever you keep your DTOs

public class LoginRequest {
    private String username;
    private String password;

    // Getters and Setters (required for Spring to bind JSON)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // You might add a no-arg constructor if needed by frameworks
    public LoginRequest() {
    }
}