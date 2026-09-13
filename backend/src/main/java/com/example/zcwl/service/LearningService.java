package com.example.zcwl.service;

import com.example.zcwl.entity.LearningRecord;
import com.example.zcwl.entity.UserLearningStats;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LearningService {
    
    LearningRecord startLearning(String userId, Integer docId, Integer chapterId);
    
    LearningRecord updateProgress(String userId, Integer docId, Integer chapterId, Integer progress, Integer position);
    
    void endLearning(String userId, Integer docId, Integer chapterId);
    
    Optional<LearningRecord> getLearningRecord(String userId, Integer docId, Integer chapterId);
    
    List<LearningRecord> getRecentLearning(String userId);
    
    UserLearningStats getUserStats(String userId);
    
    List<LearningRecord> getDocChapterProgress(String userId, Integer docId);
    
    List<Map<String, Object>> getAllDocProgress(String userId);

    List<Map<String, Object>> getHeatmap(String userId);
}
