package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserHomeController {
    private final ServiceProv userService;

    public UserHomeController(ServiceProv userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String userHome(@AuthenticationPrincipal User user, ModelMap model) {
        model.addAttribute("userHome", user);
        return "home";
    }

}
