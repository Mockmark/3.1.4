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
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import RedirectAttributes

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
// If ONLY Admins should see this combined page, keep this.
// If Users should also see it (but only the User tab), you'd need a more general mapping
// and potentially move the @PreAuthorize inside methods or use method-level security.
// For now, assuming only Admins access this combined view:
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    ServiceProv userService;
    RoleService roleService;
    PasswordEncoder passwordEncoder;

    // Constructor injection
    UserController(ServiceProv userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "")
    public String redirectToIndex() {
        return "redirect:/admin/index"; // Or redirect to the new path if you change it
    }

    // This single endpoint serves the combined page
    @GetMapping(value = "/index") // Or "/main", "/dashboard", etc. if you prefer
    public String mainPage(
            @AuthenticationPrincipal User userHomeAdm, // Gets the currently logged-in user
            ModelMap model,
            @ModelAttribute("newUser") User newUser // Use a distinct name for the new user form object
    ) {
        // Data for the top navbar
        model.addAttribute("userHomeAdm", userHomeAdm);

        // Data for the Admin tab (Users table)
        List<User> users = userService.index();
        model.addAttribute("users", users);

        // Data for the Admin tab (New User form)
        model.addAttribute("allRoles", roleService.findAll());
        // The 'newUser' object is already added via @ModelAttribute

        // Data for the User tab (uses 'userHomeAdm' which is already added)

        // Pass any flash attributes (like success/error messages after redirect)
        // This assumes you added RedirectAttributes to methods like saveUser, updateUser, deleteUser
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }


        return "main-page"; // Return the new combined template name
    }

    // --- Methods for handling Admin Tab actions ---

    @PostMapping(value = "/save") // Changed endpoint to avoid conflict with GET /index
    public String saveUser(@ModelAttribute("newUser") User user, RedirectAttributes redirectAttributes) { // Use RedirectAttributes
        try {
            Set<Role> fullRoles = user.getRoles().stream()
                    .map(role -> roleService.findById(role.getId()))
                    .collect(Collectors.toSet());
            user.setRoles(fullRoles);

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // You might want to enforce password setting here depending on requirements
                // For now, we assume it might be set later or is optional initially
                // If password is required, add validation and error handling
            }

            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
                // Redirect back to the index page, the form state won't be preserved easily this way
                // A better approach for validation errors is often returning the view directly
                // but for simplicity with tabs, redirect is shown here.
                return "redirect:/admin/index"; // Redirect back
            }

            userService.addUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User added successfully!"); // Add success message

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding user: " + e.getMessage());
        }
        return "redirect:/admin/index"; // Redirect on success or general error
    }


    @GetMapping(value = "/edit")
    public String editUser(@RequestParam(name = "id") int id, ModelMap model, @AuthenticationPrincipal User userHomeAdm) {
        User userToEdit = userService.getUserById(id);
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("userToEdit", userToEdit);
        model.addAttribute("userHomeAdm", userHomeAdm); // Add user for navbar in edit page
        return "edit"; // Keep separate edit page for simplicity unless you want modal editing
    }

    @PostMapping(value = "/edit")
    public String updateUser(@ModelAttribute("userToEdit") User user, RedirectAttributes redirectAttributes) { // Use RedirectAttributes
        try {
            Set<Role> fullRoles = user.getRoles().stream()
                    .map(role -> roleService.findById(role.getId()))
                    .collect(Collectors.toSet());
            user.setRoles(fullRoles);

            User existingUser = userService.getUserById(user.getId());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // Only encode if the password field was modified (not empty)
                if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                } else {
                    // Password provided but matches the old one (likely unintentional re-submit)
                    user.setPassword(existingUser.getPassword()); // Keep existing encoded password
                }
            } else {
                // If password field is empty, keep the existing password
                user.setPassword(existingUser.getPassword());
            }

            Optional<User> userWithSameUsername = userService.findByUsername(user.getUsername());

            if (userWithSameUsername.isPresent() && !(userWithSameUsername.get().getId() == (user.getId()))) {
                // Cannot easily redirect attributes AND keep form data AND show error on edit page without more complex handling
                // Consider returning the view directly with error message
                // For now, redirecting with a general error message
                redirectAttributes.addFlashAttribute("errorMessage", "Username already exists.");
                // Optionally redirect back to edit page: return "redirect:/admin/edit?id=" + user.getId();
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
    public String deleteUser(@RequestParam("id") int id, RedirectAttributes redirectAttributes) { // Use RedirectAttributes
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/index"; // Redirect back to the main page after deletion
    }
}