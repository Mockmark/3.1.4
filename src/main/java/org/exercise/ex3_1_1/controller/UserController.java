package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class UserController {
    ServiceProv userService;

    UserController(ServiceProv userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/index")
    public String index(ModelMap model) {
        List<User> users = userService.index();
        model.addAttribute("users", users);
        return "index";
    }

    @PostMapping(value = "/index")
    public String saveUser(@ModelAttribute("user") User user) {
        userService.addUser(user);
        return "redirect:/index";
    }

    @GetMapping(value = "/add")
    public String newUser(@ModelAttribute("user") User user) {
        return "add";
    }

    @GetMapping(value = "/edit")
    public String editUser(@RequestParam(name = "id") int id, ModelMap model) {
        User userToEdit = userService.getUserById(id);
        model.addAttribute("userToEdit", userToEdit);
        return "edit";
    }

    @PostMapping(value = "/edit")
    public String updateUser(@ModelAttribute("userToEdit") User user) {
        userService.updateUser(user);
        return "redirect:/index";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") int id) {
        userService.deleteUser(id);
        return "redirect:/index";
    }
}
