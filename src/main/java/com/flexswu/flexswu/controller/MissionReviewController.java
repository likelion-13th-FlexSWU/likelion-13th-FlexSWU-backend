package com.flexswu.flexswu.controller;

import com.flexswu.flexswu.dto.reviewDTO.ReviewCreateRequest;
import com.flexswu.flexswu.dto.reviewDTO.ReviewCreateResponse;
import com.flexswu.flexswu.dto.reviewDTO.ReviewListResponse;
import com.flexswu.flexswu.service.MissionReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MissionReviewController {

    private final MissionReviewService missionReviewService;

    @PostMapping("/mission/review")
    public ResponseEntity<?> create(@Valid @RequestBody ReviewCreateRequest request) {
        Long id = missionReviewService.create(request);
        return ResponseEntity.ok(new ReviewCreateResponse(id));
    }

    @GetMapping("/mission/review")
    public ResponseEntity<ReviewListResponse> listMine() {
        return ResponseEntity.ok(missionReviewService.listMine());
    }

    @DeleteMapping("/mission/review/delete/{reviewid}")
    public ResponseEntity<String> delete(@PathVariable("reviewid") Long reviewId) {
        missionReviewService.delete(reviewId);
        return ResponseEntity.ok("ok");
    }
}