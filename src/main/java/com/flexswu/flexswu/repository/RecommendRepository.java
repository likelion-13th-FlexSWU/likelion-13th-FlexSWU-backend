package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.Recommend;
import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
    Optional<Recommend> findTopByUserOrderByCreatedAtDesc(User user);
    List<Recommend> findAllByUserAndCreatedAt(User user, LocalDateTime createdAt);
}
