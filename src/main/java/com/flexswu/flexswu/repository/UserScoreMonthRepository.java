package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.UserScoreMonth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserScoreMonthRepository extends JpaRepository<UserScoreMonth, Long> {
    Optional<UserScoreMonth> findByUserIdAndSidoAndGugunAndMonth(Long userId, String sido, String gugun, int month);
}
