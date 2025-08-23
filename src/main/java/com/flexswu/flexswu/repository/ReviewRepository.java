package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.Mission;
import com.flexswu.flexswu.entity.MissionAuthentication;
import com.flexswu.flexswu.entity.Review;
import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserOrderByIdDesc(User user);
    boolean existsByMissionAuthenticationAndUser(MissionAuthentication auth, User user);
    //boolean existsByMissionIdAndUser(Long missionId, User user);
}