package org.exercise.ex3_1_1.repository;

import org.exercise.ex3_1_1.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String admin);
}
