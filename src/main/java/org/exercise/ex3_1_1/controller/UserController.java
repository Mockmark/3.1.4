package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin")
public class UserController {
    @GetMapping("/current-user")
    public User getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return currentUser;
    }
}