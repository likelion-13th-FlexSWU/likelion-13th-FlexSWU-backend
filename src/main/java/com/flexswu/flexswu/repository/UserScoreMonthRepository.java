package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.entity.UserScoreMonth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserScoreMonthRepository extends JpaRepository<UserScoreMonth, Long> {
    Optional<UserScoreMonth> findByUserIdAndSidoAndGugunAndMonth(Long userId, String sido, String gugun, int month);
    List<UserScoreMonth> findByUserAndSidoAndGugunAndMonthGreaterThanEqualOrderByMonthDesc(User user, String sido, String gugun, int fromMonth);
}
