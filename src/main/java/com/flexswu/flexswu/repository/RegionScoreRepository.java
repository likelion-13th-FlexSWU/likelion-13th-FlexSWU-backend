package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.RegionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionScoreRepository extends JpaRepository<RegionScore, Long> {
    Optional<RegionScore> findBySidoAndGugun(String sido, String gugun);
}