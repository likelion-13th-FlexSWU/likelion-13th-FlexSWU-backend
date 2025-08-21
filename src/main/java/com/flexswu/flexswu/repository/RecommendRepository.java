package com.flexswu.flexswu.repository;

import com.flexswu.flexswu.entity.Recommend;
import com.flexswu.flexswu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {
    Recommend findTopByUserOrderByCreatedAtDesc(User user);
}
