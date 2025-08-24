package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.Recommend;
import com.flexswu.flexswu.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
    Optional<Recommend> findTopByUserOrderByCreatedAtDesc(User user);
    List<Recommend> findAllByUserAndCreatedAt(User user, LocalDateTime createdAt);

    // 특정 사용자 및 특정 시간 이후의 모든 추천 기록을 조회 (오늘의 추천)
    List<Recommend> findAllByUserAndCreatedAtAfter(User user, LocalDateTime dateTime);

    // 가장 최근의 과거 추천 기록 1개를 찾는 데 사용 -> 이후 동일한 시간인 장소 5개 조회
    Optional<Recommend> findTopByUserAndCreatedAtBeforeOrderByCreatedAtDesc(User user, LocalDateTime dateTime);
}
