package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.RoleService;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    ServiceProv userService;
    RoleService roleService;
    PasswordEncoder passwordEncoder;

    UserController(ServiceProv userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "")
    public String redirectToIndex() {
        return "redirect:/admin/index";
    }

    @GetMapping(value = "/index")
    public String index(
            @AuthenticationPrincipal User userHomeAdm,
            ModelMap model,
            @ModelAttribute("user") User user // Add this to have an empty user object for the form
    ) {
        model.addAttribute("userHomeAdm", userHomeAdm);

        List<User> users = userService.index();
        model.addAttribute("users", users);

        // Add data needed for the 'New User' tab
        model.addAttribute("allRoles", roleService.findAll());
        // An empty User object is already added via @ModelAttribute above

        return "index"; // Make sure your main template is named index.html
    }

    // This method handles the form submission from the 'New User' tab
    @PostMapping(value = "/index") // Keep the action pointing here for simplicity
    public String saveUser(@ModelAttribute("user") User user, ModelMap model) {
        Set<Role> fullRoles = user.getRoles().stream()
                .map(role -> roleService.findById(role.getId()))
                .collect(Collectors.toSet());

        user.setRoles(fullRoles);

        // Only encode if a password was actually provided (e.g., not empty)
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Handle case where password might be required but wasn't provided
            // Or assume password isn't mandatory during creation here if logic allows
        }


        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMessage",
                    "Username already exists.");
            // Re-add data needed for both tabs when returning the same page
            model.addAttribute("users", userService.index()); // Reload user list
            model.addAttribute("allRoles", roleService.findAll());
            // The 'user' object with entered data is already in the model via @ModelAttribute

            return "index"; // Return the main template name on error
        }

        userService.addUser(user);

        // Optional: Add a success message after redirect
        // RedirectAttributes redirectAttributes
        // redirectAttributes.addFlashAttribute("successMessage", "User added successfully!");

        return "redirect:/admin/index"; // Redirect on success
    }

    // You likely don't need a separate @GetMapping("/admin/add") anymore
    // if the form is always part of the main index page via tabs.
    /*
    @GetMapping(value = "/add")
    public String newUser(@ModelAttribute("user") User user, ModelMap model) {
        model.addAttribute("allRoles", roleService.findAll());
        return "add"; // This view is now obsolete if using tabs
    }
    */

    @GetMapping(value = "/edit")
    public String editUser(@RequestParam(name = "id") int id, ModelMap model) {
        User userToEdit = userService.getUserById(id);
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("userToEdit", userToEdit);
        return "edit"; // Assuming you still have a separate edit.html page
    }

    @PostMapping(value = "/edit")
    public String updateUser(@ModelAttribute("userToEdit") User user, ModelMap model) {
        Set<Role> fullRoles = user.getRoles().stream()
                .map(role -> roleService.findById(role.getId()))
                .collect(Collectors.toSet());
        user.setRoles(fullRoles);
        User existingUser = userService.getUserById(user.getId());

        // Only encode if the password field was modified (not empty and different from existing)
        if (user.getPassword() != null && !user.getPassword().isEmpty() && !passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // If password field is empty or unchanged, keep the existing password
            user.setPassword(existingUser.getPassword());
        }

        Optional<User> userWithSameUsername = userService.findByUsername(user.getUsername());

        // Check for username existence excluding the current user being edited
        if (userWithSameUsername.isPresent() && !(userWithSameUsername.get().getId() == (user.getId()))) { // Use .equals for Integer comparison
            model.addAttribute("errorMessage", "Username already exists.");
            model.addAttribute("allRoles", roleService.findAll());
            // The 'userToEdit' object with entered data is already in the model via @ModelAttribute
            return "edit"; // Return the edit template on error
        }

        userService.updateUser(user);
        return "redirect:/admin/index";
    }


    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") int id) {
        userService.deleteUser(id);
        // Optional: Add a success message after redirect
        // redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        return "redirect:/admin/index"; // Redirect back to the index page after deletion
    }
}