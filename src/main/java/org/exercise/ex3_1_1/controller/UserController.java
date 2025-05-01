package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class UserController {

    @GetMapping(value = "")
    public String redirectToIndex() {
        return "redirect:/admin/index";
    }

    @GetMapping(value = "/index")
    public String mainPage(
            @AuthenticationPrincipal User userHomeAdm,
            ModelMap model
    ) {
        model.addAttribute("userHomeAdm", userHomeAdm);
        return "main-page";
    }

}