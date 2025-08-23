package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.RegionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegionScoreRepository extends JpaRepository<RegionScore, Long> {
    Optional<RegionScore> findBySidoAndGugun(String sido, String gugun);
    // 시/도 단위로 지역 점수 내림차순 정렬
    List<RegionScore> findAllBySidoOrderByScoreDesc(String sido);
}