package org.exercise.ex3_1_1.service;

import org.exercise.ex3_1_1.model.User;

import java.util.List;

public interface ServiceProv {
    void addUser(User user);
    List<User> index();
    User getUserById(long id);
    void updateUser(User user);
    void deleteUser(long id);
}
