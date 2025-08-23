package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdentify(String identify);
    // 시/도 단위로 유저 점수 내림차순 정렬
    List<User> findAllBySidoOrderByTotalScoreDesc(String sido);
}
