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
                .csrf(csrf -> csrf.disable()) // CSRF disabled
                .authorizeHttpRequests(authorize -> authorize
                        // Permit access to static frontend files and the login URL
                        .requestMatchers("/", "/login", "/index.html", "/login.html", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        // Permit access to the API login endpoint
                        .requestMatchers("/api/login").permitAll()

                        // --- NEW: Permit authenticated users (USER or ADMIN) to access their own info endpoint ---
                        .requestMatchers("/api/users/me").authenticated() // Allows any authenticated user (USER or ADMIN by default)
                        // If you need to restrict it only to specific roles:
                        // .requestMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")
                        // -------------------------------------------------------------------------------------

                        // Permit access to the static admin page URL for authenticated users
                        .requestMatchers("/admin/index").authenticated() // Ensure this is authenticated() or hasRole('ADMIN')

                        // Require ADMIN role for API endpoints under /api/admin (including /api/admin/current-user if you reuse it)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Require ADMIN role for other API endpoints under /api/users (if any, like list all users)
                        .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN") // Adjust this if specific /api/users endpoints should be accessible to USER

                        // --- NEW: Permit authenticated users (USER or ADMIN) to access the static user home page ---
                        .requestMatchers("/user/home").authenticated() // Allows any authenticated user (USER or ADMIN by default)
                        // If you need to restrict it only to specific roles:
                        // .requestMatchers("/user/home").hasAnyRole("USER", "ADMIN")
                        // ---------------------------------------------------------------------------------------

                        // All other requests require authentication (this rule comes last)
                        .anyRequest().authenticated() // This catches everything else
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                )
                .addFilterBefore(securityContextPersistenceFilter(securityContextRepository()), LogoutFilter.class)
        ;
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