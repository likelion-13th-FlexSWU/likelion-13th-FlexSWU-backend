package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.Mission;
import com.flexswu.flexswu.entity.MissionAuthentication;
import com.flexswu.flexswu.entity.Review;
import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionAuthenticationRepository extends JpaRepository<MissionAuthentication, Long> {
    Optional<MissionAuthentication> findByMissionAndUser(Mission mission, User user);
    Optional<MissionAuthentication> findByUserAndMission(User user, Mission mission);
}
