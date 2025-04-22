package org.exercise.ex3_1_1.service;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements ServiceProv {
    private final DataAccess userDao;

    public UserService(DataAccess userDao) {
        this.userDao = userDao;
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

}