package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
// You can use Spring Security's annotations or just rely on HttpSecurity config
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
// Ensure this path is permitted for ROLE_USER in WebSecurityConfig (which it is)
public class UserHomeController {

    @GetMapping(value = "")
    public String redirectToUserHome() {
        // Redirect base /user request to the specific home page
        return "redirect:/user/home";
    }

    @GetMapping("/home")
    public String userHome(@AuthenticationPrincipal User currentUser, ModelMap model) {
        // Inject the currently authenticated user
        model.addAttribute("userHome", currentUser); // Pass user details to the template
        return "user-info"; // Name of the Thymeleaf template for the user page
    }
}