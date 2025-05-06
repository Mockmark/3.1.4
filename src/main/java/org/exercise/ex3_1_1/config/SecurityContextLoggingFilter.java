package org.exercise.ex3_1_1.config; // Or wherever you keep your config classes

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Mark this as a Spring component
public class SecurityContextLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        logger.debug("Processing request: {}", requestUri);

        // Check the SecurityContextHolder state at the beginning of the filter chain
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            logger.debug("SecurityContextHolder contains authenticated principal for request {}: {}", requestUri, authentication.getName());
        } else {
            logger.debug("SecurityContextHolder contains anonymous or null principal for request: {}", requestUri);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);

        // You could optionally check the SecurityContextHolder state again here after the request is processed
        // to see if it changed (e.g., after authentication filters)
        // Authentication authAfterChain = SecurityContextHolder.getContext().getAuthentication();
        // if (authAfterChain != null && authAfterChain.isAuthenticated()) {
        //     logger.debug("SecurityContextHolder contains authenticated principal after chain for request {}: {}", requestUri, authAfterChain.getName());
        // } else {
        //     logger.debug("SecurityContextHolder contains anonymous or null principal after chain for request: {}", requestUri);
        // }
    }
}