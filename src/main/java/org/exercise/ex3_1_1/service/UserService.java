package org.exercise.ex3_1_1.service;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements ServiceProv {
    private final DataAccess userDao;

    public UserService(DataAccess userDao) {
        this.userDao = userDao;
    }

    @Override
    public void addUser(User user) {
        userDao.save(user);
    }

    @Override
    public List<User> index() {
        return userDao.findAll();
    }

    @Override
    public User getUserById(long id) {
        return userDao.findById(id).orElse(null);
    }

    @Override
    public void updateUser(User user) {
        userDao.save(user);
    }

    @Override
    public void deleteUser(long id) {
        userDao.deleteById(id);
    }
}
