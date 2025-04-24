package org.exercise.ex3_1_1.service;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements ServiceProv {
    private final DataAccess userDao;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserService(DataAccess userDao, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // Consider making this @Transactional(readOnly = true) as it's a fetch operation
    public void addUser(User user) {
        userDao.save(user);
    }

    @Override
    @Transactional(readOnly = true) // It's good practice for read operations
    public List<User> index() {
        // Call the new method that eagerly fetches roles
        return userDao.findAllWithRoles();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(long id) {
        // This method already eagerly fetches roles, so it's fine
        return userDao.findUserWithRolesById(id);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        userDao.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        userDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        // This method already eagerly fetches roles, so it's fine
        return userDao.findByUsername(username);
    }

    @Override
    @Transactional
    public void roleRehydrate(User user) {
        Set<Role> fullRoles = user.getRoles().stream()
                .map(role -> roleService.findById(role.getId()))
                .collect(Collectors.toSet());
        user.setRoles(fullRoles);
    }

    @Override
    @Transactional
    public void ensurePassword(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    @Override
    @Transactional
    public void ensurePassword(User user, User existingUser) {
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
    }

}