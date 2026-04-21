package com.example.zcwl.controller;

import com.example.zcwl.entity.LearningRecord;
import com.example.zcwl.entity.UserLearningStats;
import com.example.zcwl.service.LearningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/learning")
public class LearningController {

    private static final Logger logger = LoggerFactory.getLogger(LearningController.class);
    private final LearningService learningService;

    @Autowired
    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startLearning(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            Integer docId = ((Number) request.get("docId")).intValue();
            Integer chapterId = request.get("chapterId") != null ? ((Number) request.get("chapterId")).intValue() : null;

            logger.info("Start learning request from user: {}, docId: {}, chapterId: {}", userId, docId, chapterId);

            LearningRecord record = learningService.startLearning(userId, docId, chapterId);
            return ResponseEntity.ok(buildRecordResponse(record));
        } catch (Exception e) {
            logger.error("Error starting learning: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            Integer docId = ((Number) request.get("docId")).intValue();
            Integer chapterId = request.get("chapterId") != null ? ((Number) request.get("chapterId")).intValue() : null;
            Integer progress = request.get("progress") != null ? ((Number) request.get("progress")).intValue() : null;
            Integer position = request.get("position") != null ? ((Number) request.get("position")).intValue() : null;

            logger.debug("Update progress request from user: {}, docId: {}, chapterId: {}, progress: {}", 
                userId, docId, chapterId, progress);

            LearningRecord record = learningService.updateProgress(userId, docId, chapterId, progress, position);
            if (record != null) {
                return ResponseEntity.ok(buildRecordResponse(record));
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "学习记录不存在");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            logger.error("Error updating progress: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endLearning(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            String userId = authentication.getName();
            Integer docId = ((Number) request.get("docId")).intValue();
            Integer chapterId = request.get("chapterId") != null ? ((Number) request.get("chapterId")).intValue() : null;

            logger.info("End learning request from user: {}, docId: {}, chapterId: {}", 
                userId, docId, chapterId);

            learningService.endLearning(userId, docId, chapterId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error ending learning: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/record/{docId}/{chapterId}")
    public ResponseEntity<Map<String, Object>> getLearningRecord(
            @PathVariable Integer docId,
            @PathVariable Integer chapterId,
            Authentication authentication) {
        String userId = authentication.getName();
        Optional<LearningRecord> record = learningService.getLearningRecord(userId, docId, chapterId);
        
        if (record.isPresent()) {
            return ResponseEntity.ok(buildRecordResponse(record.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/record/{docId}")
    public ResponseEntity<Map<String, Object>> getDocLearningRecord(
            @PathVariable Integer docId,
            Authentication authentication) {
        String userId = authentication.getName();
        Optional<LearningRecord> record = learningService.getLearningRecord(userId, docId, null);
        
        if (record.isPresent()) {
            return ResponseEntity.ok(buildRecordResponse(record.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentLearning(Authentication authentication) {
        String userId = authentication.getName();
        List<LearningRecord> records = learningService.getRecentLearning(userId);
        
        List<Map<String, Object>> recordList = new java.util.ArrayList<>();
        for (LearningRecord record : records) {
            recordList.add(buildRecordResponse(record));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", recordList);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(Authentication authentication) {
        String userId = authentication.getName();
        UserLearningStats stats = learningService.getUserStats(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalDocs", stats.getTotalDocs());
        response.put("completedChapters", stats.getCompletedChapters());
        response.put("streakDays", stats.getStreakDays());
        response.put("totalStudyDays", stats.getTotalStudyDays());
        response.put("lastStudyDate", stats.getLastStudyDate());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/progress/{docId}")
    public ResponseEntity<Map<String, Object>> getDocChapterProgress(
            @PathVariable Integer docId,
            Authentication authentication) {
        String userId = authentication.getName();
        List<LearningRecord> records = learningService.getDocChapterProgress(userId, docId);
        
        List<Map<String, Object>> chapterList = new java.util.ArrayList<>();
        for (LearningRecord record : records) {
            chapterList.add(buildRecordResponse(record));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("chapters", chapterList);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/progress/all")
    public ResponseEntity<Map<String, Object>> getAllDocProgress(Authentication authentication) {
        String userId = authentication.getName();
        List<Map<String, Object>> progress = learningService.getAllDocProgress(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("docs", progress);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildRecordResponse(LearningRecord record) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", record.getId());
        response.put("docId", record.getDocId());
        response.put("chapterId", record.getChapterId());
        response.put("progress", record.getProgress());
        response.put("lastPosition", record.getLastPosition());
        response.put("status", record.getStatus());
        response.put("lastAccessTime", record.getLastAccessTime());
        response.put("startTime", record.getStartTime());
        response.put("completeTime", record.getCompleteTime());
        return response;
    }
}
