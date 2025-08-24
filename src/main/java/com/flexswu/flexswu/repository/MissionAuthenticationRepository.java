package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionAuthenticationRepository extends JpaRepository<MissionAuthentication, Long> {
    Optional<MissionAuthentication> findByMissionAndUser(Mission mission, User user);
    Optional<MissionAuthentication> findByUserAndMission(User user, Mission mission);
    boolean existsByUserAndRecommend(User user, Recommend recommend);
}
