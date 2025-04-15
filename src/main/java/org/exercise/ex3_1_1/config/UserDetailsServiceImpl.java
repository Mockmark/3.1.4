package org.exercise.ex3_1_1.config;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DataAccess userRepository;

    public UserDetailsServiceImpl(DataAccess userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        System.out.println("Loaded user: " + user.getUsername());
        System.out.println("Hashed password: " + user.getPassword());
        System.out.println("Authorities: " + user.getAuthorities());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities()
        );
    }

}
