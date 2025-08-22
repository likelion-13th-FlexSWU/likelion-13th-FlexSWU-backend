package com.flexswu.flexswu.service;

import com.flexswu.flexswu.dto.reviewDTO.*;
import com.flexswu.flexswu.entity.Review;
import com.flexswu.flexswu.entity.User;
import com.flexswu.flexswu.jwt.SecurityUtil;
import com.flexswu.flexswu.repository.ReviewRepository;
import com.flexswu.flexswu.repository.UserRepository;
import com.flexswu.flexswu.dto.reviewDTO.TagCodeDTO;
import com.flexswu.flexswu.dto.reviewDTO.TagCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MissionReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private User getLoginUser() {
        Long userId = SecurityUtil.getLoginUserIdOrThrow();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 실패"));
    }

    @Transactional
    public Long create(ReviewCreateRequest req) {
        User user = getLoginUser();

        // === 명세 반영: tags 1~4개 필수, 각 값은 1..20, 중복 금지 ===
        if (req.getTags() == null || req.getTags().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body 최소 길이 미만"); // 명세 문구
        }
        if (req.getTags().size() < 1 || req.getTags().size() > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tags는 1~4개를 선택해야 합니다.");
        }
        // 범위 검사(1..20)
        for (Integer code : req.getTags()) {
            if (code == null || !TagCatalog.VALID_CODES.contains(code)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "허용되지 않은 tag code: " + code);
            }
        }
        // 중복 금지
        Set<Integer> distinct = new HashSet<>(req.getTags());
        if (distinct.size() != req.getTags().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tags에 중복 값이 있습니다.");
        }

        LocalDate visited = null;
        if (req.getVisitedAt() != null && !req.getVisitedAt().isBlank()) {
            visited = LocalDate.parse(req.getVisitedAt());
        }

        Review review = Review.builder()
                .missionId(req.getMission_id())
                .user(user)
                .placeName(req.getPlaceName())
                .content(req.getContent()) // content는 null 가능
                .visitedAt(visited)
                .build();

        review.getTagCodes().addAll(req.getTags());
        reviewRepository.save(review);
        return review.getId();
    }

    @Transactional(readOnly = true)
    public ReviewListResponse listMine() {
        User user = getLoginUser();
        var list = reviewRepository.findAllByUserOrderByIdDesc(user);
        ZoneId seoul = ZoneId.of("Asia/Seoul");

        var items = list.stream().map(r ->
                ReviewItemDTO.builder()
                        .reviewid(String.valueOf(r.getId()))
                        .placeName(r.getPlaceName())
                        .content(r.getContent())
                        // 응답: [{"code":"1"},{"code":"2"}] 형식
                        .tags(r.getTagCodes().stream()
                                .map(code -> new TagCodeDTO(String.valueOf(code)))
                                .toList())
                        .visitedAt(r.getVisitedAt() == null ? null : r.getVisitedAt().toString())
                        // LocalDateTime -> OffsetDateTime(+09:00)
                        .createdAt(r.getCreatedAt() == null
                                ? null
                                : r.getCreatedAt().atZone(seoul).toOffsetDateTime())
                        .build()
        ).toList();

        return new ReviewListResponse(items);
    }

    @Transactional
    public void delete(Long reviewId) {
        User user = getLoginUser();
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰 없음"));
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음");
        }
        reviewRepository.delete(review);
    }
}
