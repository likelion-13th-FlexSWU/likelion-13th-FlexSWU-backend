package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdentify(String identify);
}
