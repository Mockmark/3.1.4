package org.exercise.ex3_1_1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin/index").setViewName("forward:/main-page.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/").setViewName("forward:/login.html");
        registry.addViewController("/user/home").setViewName("forward:/user-info.html");
    }

}