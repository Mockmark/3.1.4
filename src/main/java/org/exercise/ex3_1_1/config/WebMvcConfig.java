package org.exercise.ex3_1_1.config; // Or wherever you keep your config classes

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // Mark this as a Spring configuration class
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map the URL "/admin/index" to forward to the static resource "/main-page.html"
        // Spring Boot will then find this in /static/main-page.html
        registry.addViewController("/admin/index").setViewName("forward:/main-page.html"); // <--- Corrected mapping

        // Map the URL "/login" to forward to the static resource "/login.html"
        // Spring Boot will then find this in /static/login.html
        registry.addViewController("/login").setViewName("forward:/login.html"); // <--- Corrected mapping

        // You might also need to map the root "/" if you want it to go to login.html
        registry.addViewController("/").setViewName("forward:/login.html");
    }

    // You can keep other WebMvcConfigurer methods if you have them (e.g., addResourceHandlers)
}