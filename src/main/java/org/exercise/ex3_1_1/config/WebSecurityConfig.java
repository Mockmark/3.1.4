package org.exercise.ex3_1_1.config;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.exercise.ex3_1_1.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Needed for @PreAuthorize in UserController
public class WebSecurityConfig {

    private final CustomLoginSuccessHandler successHandler;

    public WebSecurityConfig(CustomLoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }
    // 1. Define the PasswordEncoder Bean (remains the same)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Define the SecurityFilterChain Bean (replaces configure(HttpSecurity http))
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure authorization rules (matched from original)
                .authorizeHttpRequests(authorize -> authorize
                        // Permit access to static resources, home, registration, error and the login page itself
                        .requestMatchers("/", "/login", "/error").permitAll()
                        // Secure the /admin/** path, requiring the ADMIN role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                // Configure form-based login (matched from original)
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // Specify the custom login page URL
                        // Note: The login form uses name="username", which matches the UserDetailsServiceImpl's
                        // expectation and the User entity's field.
                        // No need for .usernameParameter("email") unless you change the login field name.
                        // Optional: Specify the URL to redirect to upon successful login
                        // .defaultSuccessUrl("/admin/index", true) // Good practice for role-based landing pages
                        .successHandler(successHandler)
                        .permitAll() // Allow access to the login page for everyone
                )
                // Configure logout (matched from original)
                .logout(logout -> logout
                        .logoutUrl("/logout") // Default URL that triggers logout
                        .logoutSuccessUrl("/login?logout") // Redirect after successful logout to login page with a param
                        .permitAll() // Allow access to the logout functionality for everyone
                );
        // CSRF protection is enabled by default, which is recommended.

        return http.build();
    }

    // No longer need to inject UserDetailsService or override configure(AuthenticationManagerBuilder)
    // Spring Boot automatically wires the UserDetailsServiceImpl and PasswordEncoder beans.
    @Bean
    public CommandLineRunner init(DataAccess userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        return args -> {
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