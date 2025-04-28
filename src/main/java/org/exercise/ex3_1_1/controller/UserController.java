package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.RoleService;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class UserController {
    private final ServiceProv userService;
    private final RoleService roleService;

    UserController(ServiceProv userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "")
    public String redirectToIndex() {
        return "redirect:/admin/index";
    }

    @GetMapping(value = "/index")
    public String mainPage(
            @AuthenticationPrincipal User userHomeAdm,
            ModelMap model,
            @ModelAttribute("newUser") User newUser
    ) {
        // Data for the top navbar
        model.addAttribute("userHomeAdm", userHomeAdm);

        // Data for the Admin tab (Users table)
        List<User> users = userService.index();
        model.addAttribute("users", users);

        // Data for the Admin tab (New User form)
        model.addAttribute("allRoles", roleService.findAll());

        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }

        return "main-page";
    }

    @PostMapping(value = "/save")
    public String saveUser(@ModelAttribute("newUser") User user, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
                return "redirect:/admin/index"; // Redirect back
            }

            userService.addUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User added successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding user: " + e.getMessage());
        }
        return "redirect:/admin/index"; // Redirect on success or general error
    }

    @PostMapping(value = "/edit")
    public String updateUser(@ModelAttribute("userToEdit") User user, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userWithSameUsername = userService.findByUsername(user.getUsername());
            if (userWithSameUsername.isPresent() && !(userWithSameUsername.get().getId() == (user.getId()))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
                return "redirect:/admin/index"; // Or back to index
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/index";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") int id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/index";
    }
}