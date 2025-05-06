package org.exercise.ex3_1_1.config;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.exercise.ex3_1_1.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // <--- Import AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // <--- Import AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.context.SecurityContextRepository;

// Remove CustomLoginSuccessHandler import if you deleted it
// import org.exercise.ex3_1_1.config.CustomLoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // Removed CustomLoginSuccessHandler dependency if no longer using it
    // private final CustomLoginSuccessHandler successHandler;

    // Removed constructor if you removed successHandler dependency
    // public WebSecurityConfig(CustomLoginSuccessHandler successHandler) {
    //     this.successHandler = successHandler;
    // }

    @Bean // <--- Expose PasswordEncoder as a bean (already there, just confirming)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // <--- ADD THIS NEW BEAN FOR AUTHENTICATIONMANAGER
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    // <--- END NEW BEAN

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityContextPersistenceFilter securityContextPersistenceFilter(SecurityContextRepository securityContextRepository) {
        return new SecurityContextPersistenceFilter(securityContextRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is disabled
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Permit access to static frontend files and the /login URL
                        .requestMatchers("/", "/login", "/index.html", "/login.html", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        // Permit access to the API login endpoint
                        .requestMatchers("/api/login").permitAll()
                        // Permit access to the static admin page URL for authenticated users
                        .requestMatchers("/admin/index").authenticated() // Ensure this is authenticated() or hasRole('ADMIN')

                        // Require ADMIN role for API endpoints under /api/admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Require USER or ADMIN role for other API endpoints (if any)
                        // .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // All other requests require authentication (this rule comes last)
                        .anyRequest().authenticated()
                )
                // Explicit Session Management Configuration
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Create a session if needed (default, but good to be explicit)
                        .sessionFixation().migrateSession() // Protect against session fixation attacks (standard practice)
                        .maximumSessions(1) // Optional: Restrict to one session per user
                        .maxSessionsPreventsLogin(true) // Optional: Prevent new login if max sessions reached
                ) // <--- MODIFY THIS BLOCK
                // Removed formLogin block

                // Configure logout for API backend
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                )
                .addFilterBefore(securityContextPersistenceFilter(securityContextRepository()), LogoutFilter.class)
        //.addFilterAfter(new SecurityContextLoggingFilter(), org.springframework.security.web.context.SecurityContextHolderFilter.class)
        ; // End http configuration
        return http.build();
    }

    @Bean
    public CommandLineRunner init(DataAccess userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        return args -> {
            // CommandLineRunner initialization logic is fine
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