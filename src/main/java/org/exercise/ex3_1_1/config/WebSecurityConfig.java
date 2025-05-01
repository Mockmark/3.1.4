package org.exercise.ex3_1_1.config;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.exercise.ex3_1_1.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomLoginSuccessHandler successHandler;

    public WebSecurityConfig(CustomLoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/error").permitAll()
                        // === Add explicit rules for API paths before anyRequest().authenticated() ===
                        // Restrict the main user management API endpoints to ADMIN users
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // Restrict the roles endpoint (used by admin page forms) to ADMIN users
                        // If non-admins needed the role list, use .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/roles").hasRole("ADMIN")
                        // =========================================================================
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Existing admin path rule
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // Existing user path rule
                        .anyRequest().authenticated() // All other authenticated requests
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        // If you are using Fetch API for POST/PUT/DELETE, you need to handle CSRF.
        // Spring Security enables CSRF by default.
        // You must include the CSRF token in your Fetch requests for non-GET methods.
        // The default for the WebSecurityConfigurerAdapter (older way) was to disable CSRF for /api paths.
        // With the SecurityFilterChain bean approach, it's enabled by default globally.
        // You have two main options:
        // 1. Disable CSRF for your API paths (less secure if not handled client-side):
        //    .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
        // 2. Implement client-side CSRF token inclusion in your Fetch API calls (recommended).
        //    You would need to get the token from a cookie or a meta tag in your HTML and add it to the headers.
        //
        // For now, let's assume you will implement client-side CSRF handling as it's more secure.
        // The current config has CSRF enabled globally by default.

        return http.build();
    }

    @Bean
    public CommandLineRunner init(DataAccess userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        return args -> {
            // ... Your existing CommandLineRunner code ...
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role role = new Role();
                role.setName("ADMIN");
                return roleRepository.save(role);
            });

            roleRepository.findByName("USER").orElseGet(() -> {
                Role role = new Role();
                role.setName("USER");
                return roleRepository.save(role);
            });

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setName("Admin");
                admin.setEmail("admin@example.com");
                admin.setAge(30);
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
            }
        };
    }
}