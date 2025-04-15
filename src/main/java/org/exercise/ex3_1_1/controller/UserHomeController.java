package org.exercise.ex3_1_1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserHomeController {

    @GetMapping("/home")
    public String userHome() {
        return "home";
    }
}
