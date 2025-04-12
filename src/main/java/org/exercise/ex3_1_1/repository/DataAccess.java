package org.exercise.ex3_1_1.repository;

import org.exercise.ex3_1_1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataAccess extends JpaRepository<User, Long> {

}
