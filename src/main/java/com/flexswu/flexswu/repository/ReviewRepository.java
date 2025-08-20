package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.Review;
import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserOrderByIdDesc(User user);
    boolean existsByMissionIdAndUser(Long missionId, User user);
}